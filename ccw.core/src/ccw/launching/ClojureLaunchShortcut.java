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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.FileEditorInput;

import ccw.ClojureCore;

public class ClojureLaunchShortcut implements ILaunchShortcut, IJavaLaunchConfigurationConstants {
    private static final HashMap<String, Integer> tempLaunchCounters = new HashMap();
    
    private static int incTempLaunchCount (String projectName) {
        synchronized (tempLaunchCounters) {
            Integer cnt = tempLaunchCounters.get(projectName);
            cnt = cnt == null ? 1 : cnt + 1;
            tempLaunchCounters.put(projectName, cnt);
            return cnt;
        }
    }

    
    public void launch(IEditorPart editor, String mode) {
    	launchEditorPart(editor, mode, null);
    }
    
    private ILaunch launchEditorPart(IEditorPart editor, String mode, Boolean activateAutoReload) {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            FileEditorInput fei = (FileEditorInput) input;
            return launchProject(fei.getFile().getProject(), new IFile[] { fei
                    .getFile() }, mode, activateAutoReload);
        } else {
        	return null;
        }
    }

    public void launch(ISelection selection, String mode) {
    	launchSelection(selection, mode, null);
    }
    
    /**
     * 
     * @param selection
     * @param mode
     * @param activateAutoReload if null, then will be automatically detected
     * @return
     */
    public ILaunch launchSelection(ISelection selection, String mode, Boolean activateAutoReload) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection strSel = (IStructuredSelection) selection;
            List<IFile> files = new ArrayList<IFile>();
            IProject proj = null;
            for (Object o : strSel.toList()) {
                IFile f = (IFile) Platform.getAdapterManager().getAdapter(o, IFile.class);
                if (f != null) {
                    files.add(f);
                    if (proj == null) {
                        proj = f.getProject();
                    }
                    continue;
                }
                IProject p = (IProject) Platform.getAdapterManager().getAdapter(o, IProject.class);
                if ( p != null  &&  strSel.size() == 1) {
                    return launchProject(p, new IFile[] {}, mode, activateAutoReload);
                }
            }
            if (proj != null && !files.isEmpty()) {
                return launchProject(proj, files.toArray(new IFile[] {}), mode, activateAutoReload);
            }
        }
        return null;
    }
    public ILaunch launchProject(IProject project, String mode, Boolean activateAutoReload) {
    	StructuredSelection sel = new StructuredSelection(project);
    	return launchSelection(sel, mode, activateAutoReload);
    }
    
    protected ILaunch launchProject(IProject project, IFile[] files, String mode, Boolean activateAutoReload) {
    	activateAutoReload = activateAutoReload==null ? files.length==0 : activateAutoReload;
        try {
            ILaunchConfiguration config = findLaunchConfiguration(project, files);
            if (config == null) {
                config = createConfiguration(project, files);
            }
            if (config != null) {
            	ILaunchConfigurationWorkingCopy runnableConfiguration =
            	    config.copy(config.getName() + " #" + incTempLaunchCount(project.getName()));;
            	try {
	            	runnableConfiguration.setAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED, activateAutoReload);
	            	if (files.length > 0) {
	            		runnableConfiguration.setAttribute(LaunchUtils.ATTR_NS_TO_START_IN, ClojureCore.findMaybeLibNamespace(files[0]));
	            	}
	            	ILaunch launch = runnableConfiguration.launch(mode, null);
	            	return launch;
            	} finally {
            		runnableConfiguration.delete();
            	}
            } else {
            	return null;
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private ILaunchConfiguration findLaunchConfiguration(IProject project, IFile[] files) {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type =
            lm.getLaunchConfigurationType(LaunchUtils.LAUNCH_CONFIG_ID);
        
        List candidateConfigs = Collections.EMPTY_LIST;
        
        try {
            ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
            candidateConfigs = new ArrayList(configs.length);
            for (ILaunchConfiguration config:  configs) {
                if (config.getAttribute(ATTR_MAIN_TYPE_NAME, "").startsWith("clojure.")
                		&& config.getAttribute(ATTR_PROJECT_NAME, "").equals(project.getName())
                		&& LaunchUtils.getFilesToLaunchList(config).equals(Arrays.asList(files))) {
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
