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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.FileEditorInput;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.util.ClojureInvoker;
import ccw.util.DisplayUtil;
import clojure.lang.Keyword;

public class ClojureLaunchShortcut implements ILaunchShortcut, IJavaLaunchConfigurationConstants {
    private static final Map<String, Long> tempLaunchCounters = new HashMap<String, Long>();
    
    private ClojureInvoker leiningenConfiguration = ClojureInvoker.newInvoker(CCWPlugin.getDefault(), "ccw.leiningen.launch");
    private ClojureInvoker launch = ClojureInvoker.newInvoker(CCWPlugin.getDefault(), "ccw.util.launch");
    
    private static int incTempLaunchCount (String projectName) {
        synchronized (tempLaunchCounters) {
            Long cnt = tempLaunchCounters.get(projectName);
            cnt = cnt == null ? 1 : cnt + 1;
            tempLaunchCounters.put(projectName, cnt);
            return cnt.intValue();
        }
    }
    
    @Override
    public void launch(IEditorPart editor, String mode) {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            FileEditorInput fei = (FileEditorInput) input;
            launchProjectCheckRunning(fei.getFile().getProject(), new IFile[] { fei
                    .getFile() }, mode);
        }
    }

    @Override
    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection strSel = (IStructuredSelection) selection;
            List<IFile> files = new ArrayList<IFile>();
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
                launchProjectCheckRunning(proj, files.toArray(new IFile[] {}), mode);
            }
        }
    }
    
    public void launchProject(IProject project, String mode) {
    	launchProjectCheckRunning(project, new IFile[] {}, mode);
    }
    
    /**
     * Launches a project, first verifying if a running configuration for the
     * project is live. If so, first asks the user for confirmation that he
     * wants to start a new launch configuration for the project.
     * @param project
     * @param filesToLaunch
     * @param mode
     */
    protected void launchProjectCheckRunning(IProject project, IFile[] filesToLaunch, String mode) {
    	String projectName = project.getName();
    	List<ILaunch> running = findRunningLaunchesForProject(projectName);
    	
    	if (running.size() == 0) {
    		launchProject(project, filesToLaunch, mode);
    	} else {
    		if (userConfirmsNewLaunch(project, running.size())) {
    			launchProject(project, filesToLaunch, mode);
    		} else {
    			IViewPart replView = CCWPlugin.getDefault().getProjectREPL(project);
    			if (replView != null) {
    				replView.getViewSite().getPage().activate(replView);
    			}
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
    
    protected void launchProject(IProject project, IFile[] filesToLaunch, String mode) {
        try {
        	ILaunchConfiguration config = findLaunchConfiguration(project);
    		if (config == null) {
    			if (project.hasNature(CCWPlugin.LEININGEN_NATURE_ID)) {
            		config = createLeiningenLaunchConfiguration(project);
    			} else {
            		config = createConfiguration(project, null);
    			}
        	}
        		
            if (config != null) {
            	ILaunchConfigurationWorkingCopy runnableConfiguration =
            	    config.copy(config.getName() + " #" + incTempLaunchCount(project.getName()));
            	try {
            		if (project.hasNature(CCWPlugin.LEININGEN_NATURE_ID)) {
            			// Nothing special
            		} else {
            			LaunchUtils.setFilesToLaunchString(runnableConfiguration, Arrays.asList(filesToLaunch));
            		}
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
    
    private ILaunchConfiguration createLeiningenLaunchConfiguration(IProject project) {
		clojure.lang.IPersistentMap configMap = 
				(clojure.lang.IPersistentMap) 
				leiningenConfiguration._("lein-launch-configuration",
			    project,	
			    "update-in :dependencies conj \"[ccw/ccw-server \\\"0.1.0\\\"]\" -- update-in :injections conj \"(require 'ccw.debug.serverrepl)\" -- repl :headless");
		configMap = configMap.assoc(Keyword.intern("type-id"), Keyword.intern("ccw"));
		configMap = configMap.assoc(Keyword.intern("name"), project.getName());
		configMap = configMap.assoc(LaunchUtils.ATTR_CLOJURE_START_REPL, true);
		configMap = configMap.assoc(LaunchUtils.ATTR_LEININGEN_CONFIGURATION, true);
		configMap = configMap.assoc(Keyword.intern("private"), false);
		configMap = configMap.assoc(Keyword.intern("launch-in-background"), false);
		
		return (ILaunchConfiguration) 
				launch._("launch-configuration",
				    configMap);
    }

	private ILaunchConfiguration findLaunchConfiguration(IProject project) throws CoreException {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type =
            lm.getLaunchConfigurationType(LaunchUtils.LAUNCH_CONFIG_ID);
        
        List<ILaunchConfiguration> candidateConfigs = Collections.EMPTY_LIST;
        
        boolean isLeinProject = project.hasNature(CCWPlugin.LEININGEN_NATURE_ID);
        
        try {
            ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
            candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
            for (ILaunchConfiguration config : configs) {
                if (config.getAttribute(ATTR_MAIN_TYPE_NAME, "").startsWith("clojure.")
                		&& config.getAttribute(ATTR_PROJECT_NAME, "").equals(project.getName())
                		&& !config.getAttribute(ILaunchManager.ATTR_PRIVATE, false)) {
                	if ( 	(isLeinProject && ClojureLaunchDelegate.isLeiningenConfiguration(config))
                			||
                			(!isLeinProject && !ClojureLaunchDelegate.isLeiningenConfiguration(config)) ) {
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
                        generateUniqueLaunchConfigurationNameFrom(basename));
            
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
}
