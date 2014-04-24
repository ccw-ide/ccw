/*******************************************************************************
 * Copyright (c) 2009 Casey Marshall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshall - initial API and implementation
 *******************************************************************************/
package ccw.launching;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.ClojureProject;
import ccw.launching.ClojureLaunchShortcut.IWithREPLView;
import ccw.repl.REPLView;
import ccw.util.BundleUtils;
import ccw.util.ClojureInvoker;
import ccw.util.DisplayUtil;
import ccw.util.Pair;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public class ClojureLaunchDelegate extends JavaLaunchDelegate {

    private static Var currentLaunch = Var.create().setDynamic(true);
    private final ClojureInvoker coreLaunch = ClojureInvoker.newInvoker(CCWPlugin.getDefault(), "ccw.core.launch");
    private static IConsole lastConsoleOpened;
    
    static {
        ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(new IConsoleListener() {
            public void consolesRemoved(IConsole[] consoles) {}
            public void consolesAdded(IConsole[] consoles) {
                lastConsoleOpened = consoles.length > 0 ? consoles[0] : null;
            }
        });
    }
    
    private class REPLURLOpener {
        private static final long REPL_START_TIMEOUT_MS = 600000L;
		private ILaunch launch;
        private final boolean makeActiveREPL;
        
        private REPLURLOpener (ILaunch launch, boolean makeActiveREPL) {
            this.launch = launch;
            this.makeActiveREPL = makeActiveREPL;
        }

        public void done() {
            Job ackJob = new Job("Waiting for new REPL process to be ready...") {
            protected org.eclipse.core.runtime.IStatus run(final IProgressMonitor monitor) {
	            final String launchName = launch.getLaunchConfiguration().getName();
				final Pair<Object,IWithREPLView> o = ClojureLaunchShortcut.launchNameREPLURLPromiseAndWithREPLView.get(launchName);
				final Object replURLPromise = o.e1;
	            
	            if (replURLPromise==null) {
	            	CCWPlugin.log("No REPL required for launch " + launchName);
	            	return Status.OK_STATUS;
	            } else {
	            	try {
	            		final Object cancelObject = new Object();
	            		
	            		if (monitor != null) {
		            		// Thread watching user cancellation requests
		            		new Thread(new Runnable() {
								@Override public void run() {
									IFn realized = Clojure.var("clojure.core", "realized?");
									while (true) {
										if ((Boolean) realized.invoke(replURLPromise)) {
											// repl promise has been delivered
											return;
										}
										if (getResult() != null) {
											// Job has finished
											return;
										}
										if (monitor.isCanceled()) {
											IFn deliver = Clojure.var("clojure.core", "deliver");
											deliver.invoke(replURLPromise, cancelObject);
											return;
										}
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											CCWPlugin.logError("Error in the thread monitoring user cancellation for REPL launch of " + launchName, e);
											return;
										}
									}
								}}).start();
	            		}
	            		
	            		IFn deref = Clojure.var("clojure.core", "deref");
	            		Object timeOutObject = new Object();
		            	Object replURL = (Object) deref.invoke(replURLPromise, REPL_START_TIMEOUT_MS, timeOutObject);
		            	
		            	if (replURL == timeOutObject) {
	                        CCWPlugin.logError("Waiting for new REPL process ack timed out");
	                        return CCWPlugin.createErrorStatus("Waiting for new REPL process ack timed out");
		            	} else if (replURL == cancelObject) {
		            		return Status.CANCEL_STATUS;
		            	} else if (replURL == null) {
		            		CCWPlugin.logWarning("REPL url for launch " + launchName + " has not been provided");
		            		return Status.CANCEL_STATUS;
		            	} else {
		            		String url = (String) replURL; 
		            		coreLaunch._("on-nrepl-server-instanciated", url, LaunchUtils.getProjectName(launch));
		            		
		                    // only using a latch because getProject().touch can call done() more than once
		                    final CountDownLatch projectTouchLatch = new CountDownLatch(1);
	                    	if (isAutoReloadEnabled(launch) && getProject() != null) {
	                			try {
	                    			getProject().touch(new NullProgressMonitor() {
	                    				public void done() {
	                    					projectTouchLatch.countDown();
	                    				}
	                    			});
	                			} catch (CoreException e) {
	                				final String MSG = "unexpected exception during project refresh for auto-load on startup";
	                				ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
	                						"REPL Connection failure", MSG, e.getStatus());
	                    		}
	                    	} else {
	                    		projectTouchLatch.countDown();
	                    	}
	                    	try {
	                            projectTouchLatch.await();
	                        } catch (InterruptedException e) {}
	                    	syncConnectRepl(url, o.e2);
	                    	return Status.OK_STATUS;
		            	}
	            	} catch (Exception e) {
	    				CCWPlugin.logError("Exception while launching a Clojure Application", e);
	    				return CCWPlugin.createErrorStatus("Exception while launching a Clojure Application", e);
	            	}
	            }
            }
            };
            ackJob.setUser(true);
            ackJob.schedule();
            try {
				ackJob.join();
			} catch (InterruptedException e) {
				CCWPlugin.logError("Failure to connect to REPL", e);
			}
        }
        
        private IProject getProject() {
    		try {
    			return LaunchUtils.getProject(launch);
    		} catch (CoreException e) {
    			CCWPlugin.logWarning("Unable to get project for launch configuration", e);
    			return null;
    		}
    	}
        private void syncConnectRepl(final String replURL, final IWithREPLView withREPLView) {
        	DisplayUtil.syncExec(new Runnable() {
				@Override public void run() {
					try {
						REPLView replView = REPLView.connect(replURL, lastConsoleOpened, launch, makeActiveREPL);
						String startingNamespace = REPLURLOpener.this.launch.getLaunchConfiguration().getAttribute(LaunchUtils.ATTR_NS_TO_START_IN, "user");
	                	replView.setCurrentNamespace(startingNamespace);
	                	if (withREPLView != null) {
	                		withREPLView.run(replView);
	                	}
	                	replView.setFocus();
					} catch (Exception e) {
						throw new RuntimeException("Could not connect REPL to local launch", e);
	                }
				}
        	});
        }
    }
    
    
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, final ILaunch launch, IProgressMonitor monitor) throws CoreException {
    	LaunchUtils.setProjectName(launch, configuration.getAttribute(LaunchUtils.ATTR_PROJECT_NAME, (String) null));
    	
    	Boolean activateAutoReload = CCWPlugin.isAutoReloadOnStartupSaveEnabled();
        launch.setAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED, Boolean.toString(activateAutoReload));
        
        BundleUtils.requireAndGetVar(CCWPlugin.getDefault().getBundle().getSymbolicName(), "clojure.tools.nrepl.ack/reset-ack-port!").invoke();
        try {
            Var.pushThreadBindings(RT.map(currentLaunch, launch));
            
            super.launch(configuration, mode, launch, monitor);
            
            for(IProcess p: launch.getProcesses()) {
            	System.out.println("Launched process with command line: " + p.getAttribute(IProcess.ATTR_CMDLINE));
            }
            if (isLaunchREPL(configuration)) {
				new REPLURLOpener(launch, true).done();
            }
        } finally {
            Var.popThreadBindings();
        }
    }
	
	@Override
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
	    String launchId = UUID.randomUUID().toString();
	    return String.format(" -D%s=%s %s",
	            LaunchUtils.SYSPROP_LAUNCH_ID,
	            launchId,
	            super.getVMArguments(configuration));
	}
	
	@Override
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		String superProgramArguments = super.getProgramArguments(configuration);
		if (isLeiningenConfiguration(configuration)) {
			List<IFile> filesToLaunch = LaunchUtils.getFilesToLaunchList(configuration);
			if (filesToLaunch.size() > 0) {
				int headlessReplOffset = superProgramArguments.indexOf("repl :headless");
				String arguments = superProgramArguments.substring(0, headlessReplOffset) +
						" " + createFileLoadInjections(filesToLaunch) +
						" -- " + superProgramArguments.substring(headlessReplOffset);
				return arguments;
			} else {
				return superProgramArguments;
			}
		}
		
		String userProgramArguments = superProgramArguments;

		if (isLaunchREPL(configuration)) {
			String filesToLaunchArguments = LaunchUtils.getFilesToLaunchAsCommandLineList(configuration, false);
			
			// TODO why don't we just add the ccw stuff to the classpath as we do for nrepl?
			String toolingFile = null;
			try {
				URL toolingFileURL = CCWPlugin.getDefault().getBundle().getResource("ccw/debug/serverrepl.clj");
				toolingFile = FileLocator.toFileURL(toolingFileURL).getFile();
			} catch (IOException e) {
				throw new WorkbenchException("Could not find ccw.debug.serverrepl source file", e);
			}
			
			// Process will print a line with nRepl URL so that the Console
			// hyperlink listener can automatically open the REPL
			String nREPLInit = "(require 'clojure.tools.nrepl.server)" + 
			"(do (let [server (clojure.tools.nrepl.server/start-server)] " + 
				  "(println (str \\\"nREPL server started on port \\\" (:port server) \\\" on host 127.0.0.1 - nrepl://127.0.0.1:\\\" (:port server)))))";
			String args = String.format("-i \"%s\" -e \"%s\" %s %s", toolingFile, nREPLInit,
			        filesToLaunchArguments, userProgramArguments);
			
			CCWPlugin.log("Starting REPL with program args: " + args);
			return args;
		} else {
			String filesToLaunchArguments = LaunchUtils.getFilesToLaunchAsCommandLineList(configuration, true);
			
	    	return filesToLaunchArguments + " " + userProgramArguments;
		}
	}
	
	private String createFileLoadInjections(List<IFile> filesToLaunch) {
		
		assert filesToLaunch.size() > 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append(" update-in :injections conj \"");
		for (IFile file: filesToLaunch) {
			// We use load so that the right info are compiled for use with breakpoints in a debugger
			String path = ClojureCore.getAsRootClasspathRelativePath(file);
			int offset = path.lastIndexOf(".clj");
			sb.append("(try (load \\\"" + path.substring(0, offset) + "\\\") (catch Exception e (.printStackTrace e)))");
		}
		sb.append("\" ");
		return sb.toString();
	}

	private static boolean isLaunchREPL(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(LaunchUtils.ATTR_CLOJURE_START_REPL, true);
    }
	
	public static boolean isLeiningenConfiguration(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(LaunchUtils.ATTR_LEININGEN_CONFIGURATION, false);
	}
	
	public static boolean isAutoReloadEnabled (ILaunch launch) {
		if (launch == null) {
			return false;
		} else {
			return Boolean.valueOf(launch.getAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED));
		}
	}

    @Override
	public String getMainTypeName(ILaunchConfiguration configuration)
			throws CoreException {
    	if (isLeiningenConfiguration(configuration)) {
    		// Leiningen configuration don't need last minute classpath tweaks (yet)
    		return super.getMainTypeName(configuration);
    	}
    	
	    String main = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null); 
	    return main == null ? clojure.main.class.getName() : main;
	}
	
    @Override
    public String[] getClasspath(ILaunchConfiguration configuration)
            throws CoreException {
    	
    	if (isLeiningenConfiguration(configuration)) {
    		// Leiningen configurations don't need last minute classpath tweaks (yet)
    		return super.getClasspath(configuration);
    	}
       
        List<String> classpath = new ArrayList<String>(Arrays.asList(super.getClasspath(configuration)));
       
        ClojureProject clojureProject = ClojureCore.getClojureProject(LaunchUtils.getProject(configuration));
        for (IFolder f: clojureProject.sourceFolders()) {
            String sourcePath = f.getLocation().toOSString();
           
            while (classpath.contains(sourcePath)) {
                // The sourcePath already exists, remove it first
                classpath.remove(sourcePath);
            }
           
            classpath.add(0, sourcePath);
        }
        
        
        if (clojureProject.getJavaProject().findElement(new Path("clojure/tools/nrepl")) == null) {
            try {
                File ccwPluginDir = FileLocator.getBundleFile(CCWPlugin.getDefault().getBundle());
                System.out.println("ccwPluginDir: " + ccwPluginDir);
                // this should *always* be a file, *unless* the user is getting nREPL from a clone of its
                // project, in which case we need to reach into that project's directory...
                ArrayList replAdditions = new ArrayList();
                if (ccwPluginDir.isFile()) {
                	throw new WorkbenchException("Bundle ccw.core is a file. Cannot install nrepl");
                } else {
                    //replAdditions.add(new File(repllib, "src/main/clojure").getAbsolutePath());
                    //replAdditions.add(new File(repllib, "target/classes").getAbsolutePath());
                	
                	// Hack, until the project is launched via leiningen instead
                	File nreplFile = new File(ccwPluginDir, "lib/tools.nrepl.jar");
					String nreplPath = nreplFile.getAbsolutePath();
					if (!nreplFile.exists()) {
						throw new WorkbenchException("nreplFile not found: " + nreplFile);
					}
                	System.out.println("nreplPath for classpath:" + nreplPath);
                	replAdditions.add(nreplPath);
                }
                
                CCWPlugin.log("Adding to project's classpath to support nREPL: " + replAdditions);
                
                classpath.addAll(replAdditions);
            } catch (IOException e) {
                throw new WorkbenchException("Failed to find nrepl library", e);
            }
        } else {
        	System.out.println("Found package clojure.tools.nrepl in the project classpath, won't try to add ccw's nrepl to it then");
        }
        
        return classpath.toArray(new String[classpath.size()]);
    }
}
