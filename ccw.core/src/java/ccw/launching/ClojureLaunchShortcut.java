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

import static ccw.launching.LaunchUtils.findRunningLaunchesFor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import ccw.repl.Actions;
import ccw.util.DisplayUtil;

public class ClojureLaunchShortcut implements ILaunchShortcut, IJavaLaunchConfigurationConstants {
    private static final HashMap<String, Long> tempLaunchCounters = new HashMap();
    
    private static int incTempLaunchCount (String projectName) {
        synchronized (tempLaunchCounters) {
            Long cnt = tempLaunchCounters.get(projectName);
            cnt = cnt == null ? 1 : cnt + 1;
            tempLaunchCounters.put(projectName, cnt);
            return cnt.intValue();
        }
    }
    
    public void launch(IEditorPart editor, String mode) {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            FileEditorInput fei = (FileEditorInput) input;
            launchProject(fei.getFile().getProject(), new IFile[] { fei
                    .getFile() }, mode);
        } else {
        	return;
        }
    }

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
                launchProject(proj, files.toArray(new IFile[] {}), mode);
            }
        }
    }
    
    public void launchProject(IProject project, String mode) {
    	launchProject(project, new IFile[] {}, mode);
    }
    
    protected void launchProject(IProject project, IFile[] filesToLaunch, String mode) {

    	String projectName = project.getName();
    	List<ILaunch> running = findRunningLaunchesFor(projectName);
    	
    	if (running.size() == 0) {
    		launchProject2(project, filesToLaunch, mode);
    	} else {
    		if (userConfirmsNewLaunch(project, running.size())) {
    			launchProject2(project, filesToLaunch, mode);
    		} else {
    			IViewPart replView = CCWPlugin.getDefault().getProjectREPL(project);
    			replView.getViewSite().getPage().activate(replView);
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
    
    protected void launchProject2(IProject project, IFile[] filesToLaunch, String mode) {

    	Boolean activateAutoReload = CCWPlugin.isAutoReloadEnabled();
        try {
            ILaunchConfiguration config = findLaunchConfiguration(project);
            if (config == null) {
                config = createConfiguration(project, null);
            }
            if (config != null) {
            	ILaunchConfigurationWorkingCopy runnableConfiguration =
            	    config.copy(config.getName() + " #" + incTempLaunchCount(project.getName()));;
            	try {
            		LaunchUtils.setFilesToLaunchString(runnableConfiguration, Arrays.asList(filesToLaunch));
	            	runnableConfiguration.setAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED, activateAutoReload);
	            	if (filesToLaunch.length > 0) {
	            		runnableConfiguration.setAttribute(LaunchUtils.ATTR_NS_TO_START_IN, ClojureCore.findMaybeLibNamespace(filesToLaunch[0]));
	            	}
	            	ILaunch launch = runnableConfiguration.launch(mode, null);
	            	return;
            	} finally {
            		runnableConfiguration.delete();
            	}
            } else {
            	return;
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

	private ILaunchConfiguration findLaunchConfiguration(IProject project) {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type =
            lm.getLaunchConfigurationType(LaunchUtils.LAUNCH_CONFIG_ID);
        
        List candidateConfigs = Collections.EMPTY_LIST;
        
        try {
            ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
            candidateConfigs = new ArrayList(configs.length);
            for (ILaunchConfiguration config:  configs) {
                if (config.getAttribute(ATTR_MAIN_TYPE_NAME, "").startsWith("clojure.")
                		&& config.getAttribute(ATTR_PROJECT_NAME, "").equals(project.getName())) {
                    candidateConfigs.add(config);
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
        ILaunchConfiguration config = null;
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
            
            config = wc.doSave();
        }
        catch (CoreException ce) {
            throw new RuntimeException(ce);
        }
        return config;
    }
    
    protected ILaunchConfiguration chooseConfiguration(List configList) {
        IDebugModelPresentation labelProvider = null;
    	try {
    		labelProvider = DebugUITools.newDebugModelPresentation();
	        ElementListSelectionDialog dialog= new ElementListSelectionDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), labelProvider);
	        dialog.setElements(configList.toArray());
	        dialog.setTitle("Choose a Clojure launch configuration");  
	        dialog.setMessage(LauncherMessages.JavaLaunchShortcut_2);
	        dialog.setMultipleSelection(false);
	        int result = dialog.open();
	        if (result == Window.OK) {
	            return (ILaunchConfiguration) dialog.getFirstResult();
	        }
	        return null;
    	} finally {
    		if (labelProvider!= null) {
    			labelProvider.dispose();
    		}
    	}
    }
    
}
