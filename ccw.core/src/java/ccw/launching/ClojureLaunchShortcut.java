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

import static ccw.launching.LaunchUtils.findRunningLaunchesForProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.editors.clojure.ClojureEditor;
import ccw.editors.clojure.LoadFileAction;
import ccw.preferences.PreferenceConstants;
import ccw.repl.REPLView;
import ccw.util.ClojureInvoker;
import ccw.util.DisplayUtil;
import ccw.util.Pair;
import clojure.lang.IFn;
import clojure.lang.Keyword;

public class ClojureLaunchShortcut implements ILaunchShortcut, IJavaLaunchConfigurationConstants {
    private static final Map<String, Long> tempLaunchCounters = new HashMap<String, Long>();

    public interface IWithREPLView {
    	void run(REPLView replView);
    }
    
    /**
     * Map of console/process names to Pair<promise,IWithREPLView> of their associated nREPL URL.
     * The runnable is code to run after the REPL Client is connected.
     * <p>
     * Insertion of promises is done at launch time. <br/>
     * Delivery of promises is done via Consoles PatternMatch listeners. <br/>
     * Removal of promises is done via disconnect of Consoles Pattern Match listeners
     * (approximation of process terminated signal).
     * </p>  
     */
    public static final ConcurrentMap<String, Pair<Object,IWithREPLView>> launchNameREPLURLPromiseAndWithREPLView = new ConcurrentHashMap<String, Pair<Object,IWithREPLView>>();
    
    private ClojureInvoker leiningenConfiguration = ClojureInvoker.newInvoker(CCWPlugin.getDefault(), "ccw.leiningen.launch");
    private ClojureInvoker launch = ClojureInvoker.newInvoker(CCWPlugin.getDefault(), "ccw.launch");
    
    private static int incTempLaunchCount (String projectName) {
        synchronized (tempLaunchCounters) {
            Long cnt = tempLaunchCounters.get(projectName);
            cnt = cnt == null ? 1 : cnt + 1;
            tempLaunchCounters.put(projectName, cnt);
            return cnt.intValue();
        }
    }
    
    @Override
    public void launch(final IEditorPart editor, final String mode) {
    	launch(editor, mode, false);
    }
    
    public void launch(final IEditorPart editor, final String mode, final boolean forceLeinLaunchWhenPossible) {
    	if (editor instanceof ClojureEditor) {
    		// a new thread ensures we're not in the UI thread
    		new Thread(new Runnable() {
				@Override public void run() {
					LoadFileAction.run((ClojureEditor) editor, mode, forceLeinLaunchWhenPossible);
				}}).start();
    	}
    }

    @Override
    public void launch(ISelection selection, final String mode) {
    	launch(selection, mode, false);
    }
    
    public void launch(ISelection selection, final String mode, final boolean forceLeinLaunchWhenPossible) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection strSel = (IStructuredSelection) selection;
            final List<IFile> files = new ArrayList<IFile>();
            IProject proj = null;
            for (Object o : strSel.toList()) {
                IResource r = (IResource) Platform.getAdapterManager().getAdapter(o, IResource.class);
                if (r != null) {
                	if (r.getType() == IResource.FILE) {
                		files.add((IFile) r);
                	}
                    if (proj == null) {
                        proj = r.getProject();
                    }
                }
            }
            if (proj != null) {
            	final IProject theProj = proj;
            	// a new thread ensures we're not in the UI thread
            	new Thread(new Runnable() {
            		@Override public void run() {
            			launchProjectCheckRunning(theProj, files.toArray(new IFile[] {}), mode, forceLeinLaunchWhenPossible, null);
            		}
            	}).start();
            }
        }
    }
    
    /**
     * @param mode if null, then global preferences run mode will be selected
     */
    public void launchProject(final IProject project, final String runMode, final boolean forceLeinLaunchWhenPossible, final IWithREPLView runOnceREPLAvailable) {
    	// a new thread ensures we're not in the UI thread
    	new Thread(new Runnable() {
    		@Override public void run() {
    			launchProjectCheckRunning(project, new IFile[] {}, getRunMode(runMode), forceLeinLaunchWhenPossible, runOnceREPLAvailable);
    		}
    	}).start();
    }
    
    private static String getDefaultRunMode() {
		boolean defaultIsDebugMode = getPreferences().getBoolean(PreferenceConstants.CCW_GENERAL_LAUNCH_REPLS_IN_DEBUG_MODE);
		return defaultIsDebugMode ? ILaunchManager.DEBUG_MODE : ILaunchManager.RUN_MODE;
    }
    
    /**
     * Launches a project, first verifying if a running configuration for the
     * project is live. If so, first asks the user for confirmation that he
     * wants to start a new launch configuration for the project.
     * @param project
     * @param filesToLaunch
     * @param mode
     */
    protected void launchProjectCheckRunning(IProject project, IFile[] filesToLaunch, String mode, boolean forceLeinLaunchWhenPossible, IWithREPLView runOnceREPLAvailable) {
    	assert mode != null;
    	
    	String projectName = project.getName();
    	List<ILaunch> running = findRunningLaunchesForProject(projectName);
    	System.out.println("found " + running.size() + " running launches");
    	
    	if (running.size() == 0 
    			||
    	        userConfirmsNewLaunch(project, running.size())) {
    		launchProject(project, filesToLaunch, mode, forceLeinLaunchWhenPossible, runOnceREPLAvailable);
    	} else {
			IViewPart replView = CCWPlugin.getDefault().getProjectREPL(project);
			if (replView != null) {
				replView.getViewSite().getPage().activate(replView);
			} else {
				System.out.println("Should not be there: because in the normal course of things, a Launch does not survive its REPLView");
			}
    	}
    }    
    
    private boolean userConfirmsNewLaunch(final IProject project, final int nb) {
    	final boolean[] ret = new boolean[1];
    	final String title = "Clojure Application Launcher";
    	final String msg = (nb==1?"A":nb) + " REPL" + (nb==1?" is":"s are") 
    			+ " already running for this project. Changes you made can "
    			+ "be evaluated in an existing REPL (see Clojure menu). "
    			+ "\n\nAre you sure you want to start up another REPL for this project?\n"
    			+ "(Cancel will open existing REPL)";
        DisplayUtil.syncExec(new Runnable() {
            public void run() {
            	Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                ret[0]  = MessageDialog.openConfirm(shell, title, msg);
            }
        });
    	return ret[0];
    }
    
    private boolean useLeiningenLaunchConfiguration(IProject project, 
    		boolean forceLeinLaunchWhenPossible) throws CoreException {
    	return project.hasNature(CCWPlugin.LEININGEN_NATURE_ID) && 
    			(forceLeinLaunchWhenPossible
    			 ||
    			 getPreferences().getBoolean(PreferenceConstants.CCW_GENERAL_USE_LEININGEN_LAUNCHER));
    }
    
    protected void launchProject(IProject project, IFile[] filesToLaunch, String mode, boolean forceLeinLaunchWhenPossible, IWithREPLView runOnceREPLAvailable) {
    	mode = getRunMode(mode);
        try {
        	ILaunchConfiguration config;
			if (useLeiningenLaunchConfiguration(project, forceLeinLaunchWhenPossible)) {
        		config = createLeiningenLaunchConfiguration(project, mode.equals(ILaunchManager.DEBUG_MODE));
			} else {
				config = findLaunchConfiguration(project);
				if (config == null) {
    				System.out.println("creating basic configuration (no lein configuration)");
            		config = createConfiguration(project, null);
    			}
        	}
        		
            if (config != null) {
            	final String name = config.getName() + " #" + incTempLaunchCount(project.getName());
            	launchNameREPLURLPromiseAndWithREPLView.put(name, new Pair<Object,IWithREPLView>(promise(), runOnceREPLAvailable));
				ILaunchConfigurationWorkingCopy runnableConfiguration =
            	    config.copy(name);
            	try {
        			LaunchUtils.setFilesToLaunchString(runnableConfiguration, Arrays.asList(filesToLaunch));
	            	if (filesToLaunch.length > 0) {
	            		runnableConfiguration.setAttribute(LaunchUtils.ATTR_NS_TO_START_IN, ClojureCore.findMaybeLibNamespace(filesToLaunch[0]));
	            	}
	            	runnableConfiguration.launch(mode, null);
	            	return;
            	} finally {
            		runnableConfiguration.delete();
            	}
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Object promise() {
    	IFn promise = clojure.java.api.Clojure.var("clojure.core", "promise");
    	return promise.invoke();
    }

    private ILaunchConfiguration createLeiningenLaunchConfiguration(IProject project, boolean createInDebugMode) {
    	String command = 
    			// Adding ccw/ccw.server for enabling ccw custom code completion, etc.
    			" update-in :dependencies conj \"[ccw/ccw.server \\\"0.1.1\\\"]\" "
    			+ "-- update-in :injections conj \"(require 'ccw.debug.serverrepl)\" "

    			// Starting repl :headless ; removing :main attribute because
    			// it is causing problems: "leiningen repl :headless" defaults
    			// to automatically requiring the namespace symbol found in the
    			// [:main] project.clj key if there's no namespace symbol defined in the
    			// the [:repl-options :init-ns] project.clj key.
    			+ "-- update-in : dissoc :main " // here ':' refers to project.clj's root
    			+ "-- repl :headless ";
    	
        if (createInDebugMode) {
        	command = " update-in :jvm-opts concat \"[\\\"-Xdebug\\\" \\\"-Xrunjdwp:transport=dt_socket,server=y,suspend=n\\\"]\" -- "
        				+ command;
        }
        
    	clojure.lang.IPersistentMap configMap = 
				(clojure.lang.IPersistentMap) 
				leiningenConfiguration._("lein-launch-configuration",
			    project,	
			    command);
		configMap = configMap.assoc(Keyword.intern("type-id"), Keyword.intern("ccw"));
		configMap = configMap.assoc(Keyword.intern("name"), project.getName() + " Leiningen VM");
		configMap = configMap.assoc(LaunchUtils.ATTR_CLOJURE_START_REPL, true);
		configMap = configMap.assoc(LaunchUtils.ATTR_LEININGEN_CONFIGURATION, true);
		configMap = configMap.assoc(Keyword.intern("private"), true);
		
		// Add LEIN_REPL_ACK_PORT as an additional environment variable
		final Map<String, String> additionalEnvVariables = new HashMap<String, String>();
		additionalEnvVariables.put("LEIN_REPL_ACK_PORT", Integer.toString(CCWPlugin.getDefault().getREPLServerPort(), 10));
		configMap = configMap.assoc(Keyword.intern("environment-variables"), additionalEnvVariables);
		configMap = configMap.assoc(Keyword.intern("append-environment-variables"), true);
		
		return (ILaunchConfiguration) 
				launch._("launch-configuration", configMap);
    }

	private ILaunchConfiguration findLaunchConfiguration(IProject project) throws CoreException {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type =
            lm.getLaunchConfigurationType(LaunchUtils.LAUNCH_CONFIG_ID);
        
        List<ILaunchConfiguration> candidateConfigs = Collections.EMPTY_LIST;
        
        //boolean isLeinProject = project.hasNature(CCWPlugin.LEININGEN_NATURE_ID);
        
        try {
            ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
            candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
            for (ILaunchConfiguration config : configs) {
                if (config.getAttribute(ATTR_MAIN_TYPE_NAME, "").startsWith("clojure.")
                		&& config.getAttribute(ATTR_PROJECT_NAME, "").equals(project.getName())
                		&& !config.getAttribute(ILaunchManager.ATTR_PRIVATE, false)) {
                	if ( 	true
                			//(isLeinProject && ClojureLaunchDelegate.isLeiningenConfiguration(config))
                			//||
                			//(!isLeinProject && !ClojureLaunchDelegate.isLeiningenConfiguration(config)) 
                			) {
                		candidateConfigs.add(config);
                	}
                }
            }
        }
        catch (CoreException e) {
            throw new RuntimeException(e);
        }
        int candidateCount = candidateConfigs.size();
        if (candidateCount == 1) {
            return (ILaunchConfiguration) candidateConfigs.get(0);
        } else if (candidateCount > 1) {
            return chooseConfiguration(candidateConfigs);
        }
        return null;
    }

    protected ILaunchConfiguration createConfiguration(IProject project, IFile[] files) {
        if (files == null) files = new IFile[] {};
        try {
            ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfigurationType type =
                lm.getLaunchConfigurationType(LaunchUtils.LAUNCH_CONFIG_ID);
            
            String basename = project.getName() + " REPL";
            if (files.length == 1) {
                basename += " [" + files[0].getName() + "]"; 
            }
            
            ILaunchConfigurationWorkingCopy wc = type.newInstance(
                    null, DebugPlugin.getDefault().getLaunchManager().
                    generateLaunchConfigurationName(basename));
            
            LaunchUtils.setFilesToLaunchString(wc, Arrays.asList(files));
            
            wc.setAttribute(ATTR_PROGRAM_ARGUMENTS, "");
            
            wc.setAttribute(ATTR_MAIN_TYPE_NAME, LaunchUtils.CLOJURE_MAIN);
            
            wc.setAttribute(ATTR_PROJECT_NAME, project.getName());
            
            wc.setMappedResources(new IResource[] {project});
            
            return wc.doSave();
        }
        catch (CoreException ce) {
            throw new RuntimeException(ce);
        }
    }
    
    protected ILaunchConfiguration chooseConfiguration(final List<ILaunchConfiguration> configList) {
    	final AtomicReference<ILaunchConfiguration> ret = new AtomicReference<ILaunchConfiguration>();
    	DisplayUtil.syncExec(new Runnable() {
			@Override
			public void run() {
		        IDebugModelPresentation labelProvider = null;
		    	try {
		    		labelProvider = DebugUITools.newDebugModelPresentation();
			        ElementListSelectionDialog dialog= new ElementListSelectionDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), labelProvider);
			        dialog.setElements(configList.toArray());
			        dialog.setTitle("Choose a Clojure launch configuration");  
			        dialog.setMessage(LauncherMessages.JavaLaunchShortcut_2);
			        dialog.setMultipleSelection(false);
			        dialog.setAllowDuplicates(true);
			        int result = dialog.open();
			        if (result == Window.OK) {
			        	ret.set((ILaunchConfiguration) dialog.getFirstResult());
			        }
		    	} finally {
		    		if (labelProvider != null) {
		    			labelProvider.dispose();
		    		}
		    	}
			}
		});
    	return ret.get();
    }

    /**
     * @return the run mode, getting the default run mode if <code>mode</code>
     *         is null.
     */
	private String getRunMode(String mode) {
		return (mode!=null) ? mode : getDefaultRunMode();
	}

    private static IPreferenceStore getPreferences() {
    	return  CCWPlugin.getDefault().getCombinedPreferenceStore();
    }
}
