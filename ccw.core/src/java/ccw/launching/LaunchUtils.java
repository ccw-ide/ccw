/*******************************************************************************
 * Copyright (c) 2009 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.util.StringUtils;

public final class LaunchUtils implements IJavaLaunchConfigurationConstants {
    private LaunchUtils(){}
    
    static public final String LAUNCH_CONFIG_ID = "ccw.launching.clojure";
    
    static public final String CLOJURE_MAIN = "clojure.main";
    
	/** Launch attribute that will be of type String, the files will be listed separated by newlines */
    static public final String ATTR_FILES_LAUNCHED_AT_STARTUP = "CCW_ATTR_FILES_LAUNCHED_AT_STARTUP";

    public static final String ATTR_TOOL_REPL_CONNECTION = "ccw.repl.launchedToolingConnectionId";

	public static final String ATTR_CLOJURE_START_REPL = "ccw.repl.startOnLaunch";

	public static final String ATTR_IS_AUTO_RELOAD_ENABLED = "CCW_ATTR_IS_AUTO_RELOAD_ENABLED";
	public static final String ATTR_NS_TO_START_IN = "CCW_ATTR_NS_TO_START_IN";
	

    public static final String SYSPROP_LAUNCH_ID = "ccw.repl.launchid";

	/**
	 * @param files
	 * @param lastFileAsScript if true, does not install the last arg as a resource to load, but as
	 *        a script to launch
	 * @return
	 */
    static public String getProgramArguments(IProject project, IFile[] files, boolean lastFileAsScript) {
        StringBuilder args = new StringBuilder();
        
        int lastIndex = lastFileAsScript ? files.length - 1 : files.length;
        
        for (int i = 0; i < lastIndex; i++) {
        	String fileArg = fileArg(project, files[i]);
        	if (!StringUtils.isEmpty(fileArg)) {
        		args.append(" -i" + fileArg);
        	}
        }
        if (lastFileAsScript) {
        	args.append(fileArg(project, files[lastIndex]));
        }
        return args.toString();
    }
    
    public static IProject getProject(ILaunchConfiguration configuration) throws CoreException {
        return getProject(configuration.getAttribute(LaunchUtils.ATTR_PROJECT_NAME, (String) null));
    }
    
    // TODO total duplication of above; begs to be in Clojure
    public static IProject getProject (ILaunch launch) throws CoreException {
        return getProject(getProjectName(launch));
    }
    
    public static String getProjectName (ILaunch launch) {
        return launch.getAttribute(LaunchUtils.ATTR_PROJECT_NAME);
    }

    public static IProject getProject (String projectName) {
        // here fundamentally to simplify things for repl cmd history
        return projectName == null ? null : ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }
    
    private static String fileArg(IProject project, IFile file) {
    	String FILE_ARG_ERROR_PREFIX = 	"When trying to create clojure.main "
    			+ "file arg to launch, was " + "unable to "; 
		    	
    	IPath filePath = file.getLocation();
    	
    	IJavaProject javaProject = ClojureCore.getJavaProject(project);
    	try {
    		IPackageFragmentRoot filePFR = findPackageFragmentRoot(javaProject, filePath);
	    	if (filePFR != null) {
	    		IPath pfrPath = filePFR.getResource().getLocation();
	    		// TODO we can use .makeRelativeTo again when we decide not to support Eclipse 3.4 anymore
//    			String classpathRelativeArg = filePath.makeRelativeTo(pfrPath).toString();
	    		String classpathRelativeArg = filePath.toString().substring(pfrPath.toString().length()); 
	    		if (classpathRelativeArg.startsWith("/")) {
	    			classpathRelativeArg = classpathRelativeArg.substring(1);
	    		}
            	return " \"@/" + classpathRelativeArg + "\"";
	    	} else {
	    		CCWPlugin.logError(FILE_ARG_ERROR_PREFIX + 
	    				" find package fragment root for file " 
	    				+ file + " in project " + project);
	    		return "";
	    	}
    	} catch (JavaModelException jme) {
    		CCWPlugin.logError(FILE_ARG_ERROR_PREFIX + 
    				" complete due to a JavaModelException finding package fragment root for file " 
    				+ file + " in project " + project, jme);
    		return "";
    	}
    }
    
    private static IPackageFragmentRoot findPackageFragmentRoot(IJavaProject javaProject, IPath filePath) throws JavaModelException {
    	if (filePath.isEmpty() || filePath.isRoot()) {
    		return null;
    	} else {
    		IResource possibleFragmentResource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
    		if (possibleFragmentResource != null) {
    			filePath = possibleFragmentResource.getFullPath();
    		}
        	IPackageFragmentRoot fragment = javaProject.findPackageFragmentRoot(filePath);
        	if (fragment != null) {
        		return fragment;
        	} else {
        		return findPackageFragmentRoot(javaProject, filePath.removeLastSegments(1));
        	}
    	}
    }
    
    
    static public String getProgramArguments(IProject project, List<IFile> files, boolean lastFileAsScript) {
        return getProgramArguments(project, files.toArray(new IFile[]{}), lastFileAsScript);
    }
    
    static public List<IFile> getFilesToLaunchList(ILaunchConfiguration config) throws CoreException {
        List<IFile> selectedFiles = new ArrayList<IFile>();
        for (String path : config.getAttribute(LaunchUtils.ATTR_FILES_LAUNCHED_AT_STARTUP, "").split("\n")) {
            IResource rc = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
            if (rc instanceof IFile) {
                selectedFiles.add((IFile) rc);
            }
        }
        return selectedFiles;
    }
    
    static public String getFilesToLaunchAsCommandLineList(ILaunchConfiguration config, boolean lastFileAsScript) throws CoreException {
    	List<IFile> filesToLaunch = LaunchUtils.getFilesToLaunchList(config);
    	return LaunchUtils.getProgramArguments(getProject(config), filesToLaunch, lastFileAsScript);

    }
    
    static public void setFilesToLaunchString(ILaunchConfigurationWorkingCopy config, List<IFile> selectedFiles) {
        StringBuilder filesAsString = new StringBuilder();
        if (selectedFiles != null) {
        	for (int i = 0; i < selectedFiles.size(); i++) {
        		if (i != 0) {
        			filesAsString.append('\n');
        		}
        		filesAsString.append(selectedFiles.get(i).getFullPath());
        	}
        }
        config.setAttribute(LaunchUtils.ATTR_FILES_LAUNCHED_AT_STARTUP, filesAsString.toString());
    }    
}
