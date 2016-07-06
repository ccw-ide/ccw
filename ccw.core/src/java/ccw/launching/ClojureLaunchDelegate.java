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
import ccw.TraceOptions;
import ccw.launching.ClojureLaunchShortcut.IWithREPLView;
import ccw.preferences.PreferenceConstants;
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
            	CCWPlugin.log("reading in launchNameREPLURLPromiseAndWithREPLView the key: " + launchName);

	            if (o==null || o.e1==null) {
	            	CCWPlugin.log("No REPL required for launch " + launchName + " o = " + o);
	            	return Status.OK_STATUS;
	            } else {
	            	final Object replURLPromise = o.e1;
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
		            		coreLaunch.__("on-nrepl-server-instanciated", url, LaunchUtils.getProjectName(launch));
		            		
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

    	final String name = configuration.getName();
    	if (!ClojureLaunchShortcut.launchNameREPLURLPromiseAndWithREPLView.containsKey(name)) {
    		// Set a key since it's used both for getting an instance of IWithREPLView and dataflow via the promise
    		ClojureLaunchShortcut.launchNameREPLURLPromiseAndWithREPLView.put(name, new Pair<Object,IWithREPLView>(promise(), null));
    		CCWPlugin.log("putting in launchNameREPLURLPromiseAndWithREPLView the key: " + name);
    	}
        
        
        BundleUtils.requireAndGetVar(CCWPlugin.getDefault().getBundle().getSymbolicName(), "clojure.tools.nrepl.ack/reset-ack-port!").invoke();
        try {
            Var.pushThreadBindings(RT.map(currentLaunch, launch));
            
            super.launch(configuration, mode, launch, monitor);
            
            for(IProcess p: launch.getProcesses()) {
            	CCWPlugin.log("Launched process with command line: " + p.getAttribute(IProcess.ATTR_CMDLINE));
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

	    	String injectCCWServer = " update-in :dependencies conj \"[ccw/ccw.server \\\"0.2.0\\\"]\" "
		             + "-- update-in :injections conj \"(require 'ccw.debug.serverrepl)\" ";
	    	String injectCiderNrepl = " update-in :plugins conj \"[cider/cider-nrepl \\\"0.9.0\\\"]\" "
       					// we force nrepl 0.2.10 because cider 0.9.0 requires 0.2.7 at least but leiningen forces 0.2.6
						// the cider-nrepl plugin will automatically register the cider-nrepl handler with leiningen
                     + "-- update-in :dependencies conj \"[org.clojure/tools.nrepl \\\"0.2.10\\\"]\" ";

	    	// Addind systematically ccw/ccw.server since e.g. NamespaceBrowser uses it currently
	    	String command = injectCCWServer;
	    	
	    	// Conditionally adding ider/cider-nrepl for enabling ccw custom code completion, etc.
	    	if (isUseCiderNrepl()) {
	    		command += " -- " + injectCiderNrepl;
	    	}
	    	
	    	if (isInstallClojure1_6_print_object_hack()) {
	    		// Add code for getting back human-readable prints of e.g. namespaces
	    		command += " -- update-in :injections conj " + alterPrintObject;
	    	}
	    	
	    	// Add code for pretty printing vars correctly. Can be removed once clojure 1.8 is out and oldier not used
	    	command += " -- update-in :injections conj " + pprintVarsCorrectly;
	    	
			int headlessReplOffset = superProgramArguments.indexOf("repl :headless");
			command = superProgramArguments.substring(0, headlessReplOffset) +
					command + " -- " + superProgramArguments.substring(headlessReplOffset);
			
			if (filesToLaunch.size() > 0) {
				headlessReplOffset = command.indexOf("repl :headless");
				String arguments = command.substring(0, headlessReplOffset) +
						" " + createFileLoadInjections(filesToLaunch) +
						" -- " + command.substring(headlessReplOffset);
				return arguments;
			} else {
				return command;
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
			String nREPLInit = "\"(require 'clojure.tools.nrepl.server)" + 
					getNreplHandlerRequire() +
			"(let [server (clojure.tools.nrepl.server/start-server " + getNreplHandlerKeywordOption() + ")] " + 
				  "(println (str \\\"nREPL server started on port \\\" (:port server) \\\" on host 127.0.0.1 - nrepl://127.0.0.1:\\\" (:port server))))\"";
			String args = String.format("-i \"%s\" -e %s -e %s -e %s %s %s",
					toolingFile,
			    	// Add code for getting back human-readable prints of e.g. namespaces
					isInstallClojure1_6_print_object_hack() ? alterPrintObject : "nil",
			        // Add code for pretty-printing vars correctly
			        pprintVarsCorrectly,
					nREPLInit,
			        filesToLaunchArguments,
			        userProgramArguments);
			
			CCWPlugin.log("Starting REPL with program args: " + args);
			return args;
		} else {
			String filesToLaunchArguments = LaunchUtils.getFilesToLaunchAsCommandLineList(configuration, true);
			
	    	return filesToLaunchArguments + " " + userProgramArguments;
		}
	}
	
	private String getNreplHandlerRequire() {
		return isUseCiderNrepl() ? "(require 'cider.nrepl)" : "";
	}
	private String getNreplHandlerKeywordOption() {
		return isUseCiderNrepl() ? ":handler cider.nrepl/cider-nrepl-handler" : "" /* default handler */;
	}
	
	private boolean isUseCiderNrepl() {
		return CCWPlugin.getDefault().getCombinedPreferenceStore().getBoolean(PreferenceConstants.CCW_GENERAL_USE_CIDER_NREPL);
	}
	
	private boolean isInstallClojure1_6_print_object_hack() {
		return CCWPlugin.getDefault().getCombinedPreferenceStore().getBoolean(PreferenceConstants.CCW_GENERAL_USE_CLOJURE_1_6_PRINT_OBJECT_HACK);
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
	
	/** Code for altering clojure.core/print-object for getting back human-readable prints of e.g. namespaces */
	private static final String alterPrintObject = "\"(alter-var-root #'clojure.core/print-object (fn [old-print-object] (fn [o, ^java.io.Writer w] (when (instance? clojure.lang.IMeta o)      (#'clojure.core/print-meta o w))         (.write w \\\"#<\\\")         (let [name (.getSimpleName (class o))]           (when (seq name)             (.write w name)             (.write w \\\" \\\")))         (.write w (str o))         (.write w \\\">\\\"))))\""; 

	/** Code for correct handling of Var objects in pprint - can be removed after clojure 1.8 is released */
	private static final String pprintVarsCorrectly = "\"(do (require 'clojure.pprint) (@(find-var 'clojure.pprint/use-method) @(find-var 'clojure.pprint/simple-dispatch) clojure.lang.Var @(find-var 'clojure.pprint/pprint-simple-default)))\"";
	
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
    	
    	// Here we know the project is not a leiningen project, will trying launching differently
       
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
        
        // Add nrepl to the classpath if not already on the project classpath
        if (clojureProject.getJavaProject().findElement(new Path("clojure/tools/nrepl")) == null) {
            try {
                File ccwPluginDir = getCCWPluginDirectory();
                ArrayList<String> replAdditions = new ArrayList<String>();
            	File nreplFile = new File(ccwPluginDir, "lib/tools.nrepl-jar");
				String nreplPath = nreplFile.getAbsolutePath();
				if (!nreplFile.exists()) {
					throw new WorkbenchException("nreplFile not found: " + nreplFile);
				}
            	replAdditions.add(nreplPath);
                CCWPlugin.log("Adding to project's classpath to support nREPL: " + replAdditions);
                classpath.addAll(replAdditions);
            } catch (IOException e) {
                throw new WorkbenchException("Failed to find nrepl library", e);
            }
        } else {
        	CCWPlugin.log("Found package clojure.tools.nrepl in the project classpath, won't try to add ccw's nrepl to it then");
        }
        
        if (isUseCiderNrepl()) {
	        // Add cider-nrepl to the classpath if not already on the project classpath
	        if (clojureProject.getJavaProject().findElement(new Path("cider/nrepl/middleware")) == null) {
	            try {
	                File ccwPluginDir = getCCWPluginDirectory();
	                ArrayList<String> replAdditions = new ArrayList<String>();
                	File ciderNreplFile = new File(ccwPluginDir, "lib/cider-nrepl-jar");
					if (!ciderNreplFile.exists()) {
						throw new WorkbenchException("cider-nrepl not found: " + ciderNreplFile);
					}
                	replAdditions.add(ciderNreplFile.getAbsolutePath());
                	File dynapathFile = new File(ccwPluginDir,"lib/dynapath-jar");
                	if (!dynapathFile.exists()) {
                		throw new WorkbenchException("dynapath, a required dependency of cider-nrepl, not found: " + dynapathFile);
                	}
                	replAdditions.add(dynapathFile.getAbsolutePath());
	                CCWPlugin.log("Adding to project's classpath to support cider-nrepl: " + replAdditions);
	                classpath.addAll(replAdditions);
	            } catch (IOException e) {
	                throw new WorkbenchException("Failed to find cider-nrepl library", e);
	            }
	        } else {
	        	CCWPlugin.log("Found package cider/nrepl/middleware in the project classpath, won't try to add ccw's cider-nrepl to it then");
	        }
    	}

        return classpath.toArray(new String[classpath.size()]);
    }
    
    private File getCCWPluginDirectory() throws IOException, WorkbenchException {
        File ccwPluginDir = FileLocator.getBundleFile(CCWPlugin.getDefault().getBundle());
        CCWPlugin.getTracer().trace(TraceOptions.LAUNCHER, "ccwPluginDir: " + ccwPluginDir);
        if (ccwPluginDir.isFile()) {
        	throw new WorkbenchException("Bundle ccw.core cannot be returned as a file.");
        } else {
        	return ccwPluginDir;
        }
    }

    public static Object promise() {
    	IFn promise = clojure.java.api.Clojure.var("clojure.core", "promise");
    	return promise.invoke();
    }

}
