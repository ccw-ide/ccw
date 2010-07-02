/*******************************************************************************
 * Copyright (c) 2009 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *    Gorsal - patch for correcting namespace browser initialisation issue
 *******************************************************************************/
package ccw.console;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.IPageBookViewPage;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.builder.ClojureBuilder;
import ccw.debug.ClojureClient;
import ccw.editors.antlrbased.EvaluateTextAction;
import ccw.launching.LaunchUtils;
import ccw.outline.NamespaceBrowser;
import ccw.preferences.PreferenceConstants;

public class ConsolePageParticipant implements IConsolePageParticipant {
    private IOConsole console;
    private ClojureClient clojureClient;
    private IContextActivation contextActivation;
    private Thread initializationThread;

    public void init(IPageBookViewPage page, IConsole console) {
        assert org.eclipse.debug.ui.console.IConsole.class.isInstance(console);
        assert TextConsole.class.isInstance(console);
        this.console = (IOConsole) console;
        this.initializationThread = new Thread(new Runnable() {
            public void run() {
                initNamespaceBrowser();
            }
        });
        initializationThread.start();
    }

    public void activated() {
        activateContext("ccw.ui.clojureEditorScope"); //$NON-NLS-1$
    }

    public void deactivated() {
        deactivateContext();
    }

    private static IContextService contextService() {
        return (IContextService) PlatformUI.getWorkbench().getAdapter(IContextService.class);
    }

    private void activateContext(String contextId) {
        contextActivation = contextService().activateContext(contextId);
        if (contextActivation == null) {
            throw new IllegalStateException("");
        }
    }

    private void deactivateContext() {
        if (contextActivation != null) {
            contextService().deactivateContext(contextActivation);
            contextActivation = null;
        }
    }

    private synchronized void initNamespaceBrowser() {
        if (clojureClient == null) {
            bindConsoleToClojureEnvironment();
        }
        if (clojureClient != null) {
            addPatternMatchListener(this.console);
            if (CCWPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SWITCH_TO_NS_ON_REPL_STARTUP)) {
                try {
                    org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
                    List<IFile> files = LaunchUtils.getFilesToLaunchList(processConsole.getProcess().getLaunch().getLaunchConfiguration());
                    if (files.size() > 0) {
                        String namespace = ClojureCore.getDeclaredNamespace(files.get(0));
                        if (namespace != null) {
                            EvaluateTextAction.evaluateText(this.console, "(in-ns '" + namespace + ")", false);
                        }
                    }
                } catch (CoreException e) {
                    CCWPlugin.logError("error while trying to guess the ns to which make the REPL console switch", e);
                }
            }
            NamespaceBrowser.setClojureClient(clojureClient);
            org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
            // TODO add safeguards: the launch configuration must enable a REPL, and explicit flag (default value from global params) to allow this behaviour
            // and explicit flag (true by default) to determine the default value for the explicit flag :-)
            try {
	            IProject project = LaunchUtils.getProject(processConsole.getProcess().getLaunch().getLaunchConfiguration());
	            try {
		            ClojureBuilder.fullBuild(project, new NullProgressMonitor());
	            } catch (CoreException e) {
	            	CCWPlugin.logError("Unable to auto-compile project " + project.getName() 
	            			+ " after having launched a configuration", e);
	            }
            } catch (CoreException e) {
            	CCWPlugin.logError("Unable to auto-compile a project after having launched a configuration "
            			+ "because the project cannot be retrieved!", e);
            }
        }
    }

    private void bindConsoleToClojureEnvironment() {
        org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
        boolean stop = false;
        int selfTimeout = 60000; // 60 seconds
        while (!stop && selfTimeout > 0) {
            if (Thread.interrupted()) {
                stop = true;
            } else {
                int clojureVMPort = LaunchUtils.getLaunchServerReplPort(processConsole.getProcess().getLaunch());
                if (clojureVMPort != -1) {
                    clojureClient = new ClojureClient(clojureVMPort);
                    stop = true;
                } else {
                    try {
                        Thread.sleep(100);
                        selfTimeout -= 100;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        stop = true;
                    }
                }
            }
        }
    }

    public void dispose() {
        deactivateContext();
        if (initializationThread.isAlive()) {
            initializationThread.interrupt();
        }
    }

    public Object getAdapter(Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    private void addPatternMatchListener(TextConsole console) {
        console.addPatternMatchListener(new IPatternMatchListener() {
            public int getCompilerFlags() {
                return 0;
            }

            public String getLineQualifier() {
                return null;
            }

            public String getPattern() {
                return ".*\n";
            }

            public void connect(TextConsole console) {
                // Nothing
            }

            public void disconnect() {
                // Nothing
            }

            public void matchFound(PatternMatchEvent event) {
                if (clojureClient != null) {
                    NamespaceBrowser.setClojureClient(clojureClient);
                }
            }
        });
    }
}
