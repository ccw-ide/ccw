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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
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
import ccw.repl.REPLView;
import ccw.util.DisplayUtil;
import clojure.lang.RT;
import clojure.lang.Var;
import clojure.tools.nrepl.SafeFn;

public class ClojureLaunchDelegate extends JavaLaunchDelegate {

    private static Var currentLaunch = Var.create();
    private static IConsole lastConsoleOpened;

    static {
        ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(new IConsoleListener() {
            public void consolesRemoved(IConsole[] consoles) {}
            public void consolesAdded(IConsole[] consoles) {
                lastConsoleOpened = consoles.length > 0 ? consoles[0] : null;
            }
        });
    }

    private class REPLViewLaunchMonitor extends ProgressMonitorWrapper {
        private ILaunch launch;

        private REPLViewLaunchMonitor (IProgressMonitor m, ILaunch launch) {
            super(m);
            this.launch = launch;
        }

        public void done() {
            super.done();
            Job ackJob = new Job("Waiting for new REPL process ack") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					final int STEPS_BEFORE_GIVING_UP = 600;
					final int MILLIS_BETWEEN_STEPS = 50;
					monitor.beginTask("Waiting for new REPL process ack", STEPS_BEFORE_GIVING_UP);
					Integer maybePort = null;
					for (int i = 0; i < STEPS_BEFORE_GIVING_UP; i++) {
						maybePort = (Integer)SafeFn.find("clojure.tools.nrepl", "wait-for-ack").sInvoke(MILLIS_BETWEEN_STEPS);
						monitor.worked(1);
						if (maybePort != null) {
							break;
						}
					}

		            if (maybePort == null) {
		                CCWPlugin.logError("Waiting for new REPL process ack timed out");
		                return new Status(IStatus.ERROR, CCWPlugin.PLUGIN_ID, "Waiting for new REPL process ack timed out");
		            }
		            final Integer port = maybePort;
		            DisplayUtil.asyncExec(new Runnable() {
		                public void run() {
	                    	if (isAutoReloadEnabled(launch) && getProject() != null) {
                    			try {
	                    			getProject().touch(new NullProgressMonitor() {
	                    				public void done() {
	                    					connectRepl();
	                    				}
	                    			});
                    			} catch (CoreException e) {
                    				final String MSG = "unexpected exception during project refresh for auto-load on startup";
                    				ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    						"REPL Connexion failure", MSG, e.getStatus());
	                    		}
	                    	} else {
	                    		connectRepl();
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
		                private void connectRepl() {
		                    try {
		                        REPLView replView = REPLView.connect("localhost", port, lastConsoleOpened, launch);
		                        String startingNamespace = REPLViewLaunchMonitor.this.launch.getLaunchConfiguration().getAttribute(LaunchUtils.ATTR_NS_TO_START_IN, "user");
		                        try {
		                        	replView.setCurrentNamespace(startingNamespace);
		                        } catch (Exception e) {
		                        	CCWPlugin.logError("Could not start REPL in namespace " + startingNamespace, e);
		                        }

		                    } catch (Exception e) {
		                        CCWPlugin.logError("Could not connect REPL to local launch", e);
		                    }
		                }
		            });
		            monitor.done();
		            return Status.OK_STATUS;
				}
            };
            ackJob.setUser(true);
            ackJob.schedule();
        }
    }


    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        launch.setAttribute(LaunchUtils.ATTR_PROJECT_NAME, configuration.getAttribute(LaunchUtils.ATTR_PROJECT_NAME, (String) null));
        launch.setAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED, Boolean.toString(configuration.getAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED, false)));
        SafeFn.find("clojure.tools.nrepl", "reset-ack-port!").sInvoke();
        try {
            Var.pushThreadBindings(RT.map(currentLaunch, launch));
            super.launch(configuration, mode, launch, (monitor == null || !isLaunchREPL(configuration)) ?
                    monitor : new REPLViewLaunchMonitor(monitor, launch));
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
		String userProgramArguments = super.getProgramArguments(configuration);

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

			String nREPLInit = "(require 'clojure.tools.nrepl)" +
			    // don't want start-server return value printed
			    String.format("(do (clojure.tools.nrepl/start-server 0 %s) nil)", CCWPlugin.getDefault().getREPLServerPort());

			return String.format("-i \"%s\" -e \"%s\" %s %s", toolingFile, nREPLInit,
			        filesToLaunchArguments, userProgramArguments);
		} else {
			String filesToLaunchArguments = LaunchUtils.getFilesToLaunchAsCommandLineList(configuration, true);

	    	return filesToLaunchArguments + " " + userProgramArguments;
		}
	}

	private static boolean isLaunchREPL(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(LaunchUtils.ATTR_CLOJURE_START_REPL, true);
    }

	public static boolean isAutoReloadEnabled (ILaunch launch) {
	    return Boolean.valueOf(launch.getAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED));
	}

    @Override
	public String getMainTypeName(ILaunchConfiguration configuration)
			throws CoreException {
	    String main = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
	    return main == null ? clojure.main.class.getName() : main;
	}

    @Override
    public String[] getClasspath(ILaunchConfiguration configuration)
            throws CoreException {

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
                File repllib = FileLocator.getBundleFile(Platform.getBundle("org.clojure.tools.nrepl"));
                classpath.add(repllib.getAbsolutePath());
            } catch (IOException e) {
                throw new WorkbenchException("Failed to find nrepl library", e);
            }
        }

        return classpath.toArray(new String[classpath.size()]);
    }
}
