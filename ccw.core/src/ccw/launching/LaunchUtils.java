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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import ccw.util.IOUtils;
import ccw.util.StringUtils;

public final class LaunchUtils implements IJavaLaunchConfigurationConstants {

    private LaunchUtils(){}
    
    static public final String LAUNCH_ID = "ccw.launching.clojure";
    
    static public final String MAIN_CLASSNAME = "clojure.main";
    static public final String MAIN_CLASSNAME_FOR_REPL = "clojure.contrib.repl_ln";
    
    static public final int DEFAULT_SERVER_PORT = -1;
    
	/** Launch attribute that will be of type String, the files will be listed separated by newlines */
    static public final String ATTR_FILES_LAUNCHED_AT_STARTUP = "CCW_ATTR_FILES_LAUNCHED_AT_STARTUP";

	public static final String ATTR_CLOJURE_SERVER_LISTEN = "CCW_ATTR_CLOJURE_SERVER_LISTEN";

	public static final String ATTR_CLOJURE_SERVER_FILE_PORT = "ccw.debug.serverrepl.file.port";

	public static final String SERVER_FILE_PORT_PREFIX = "ccw.debug.serverrepl.port-";

	public static final String SERVER_FILE_PORT_SUFFFIX = ".port";

	public static final String ATTR_CLOJURE_INSTALL_REPL = "CCW_ATTR_CLOJURE_INSTALL_REPL";

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
    	String projectName = configuration.getAttribute(LaunchUtils.ATTR_PROJECT_NAME, (String) null);
    	if (projectName == null) {
    		return null;
    	} else {
    		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    	}
    }
    
    private static String fileArg(IProject project, IFile file) {
    	String FILE_ARG_ERROR_PREFIX = 	"When trying to create clojure.main "
    			+ "file arg to launch, was " + "unable to "; 
		    	
    	IPath filePath = file.getLocation();
    	
    	IJavaProject javaProject = ClojureCore.getJavaProject(project);
    	try {
    		IPackageFragmentRoot filePFR = findPackageFragmentRoot(javaProject, filePath);
	    	if (filePFR != null) {
	    		IPath pfrPath = filePFR.getResource().getLocation();// getResource();
    			String classpathRelativeArg = filePath.makeRelativeTo(pfrPath).toString();
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

    static public int getLaunchServerReplPort(ILaunch launch) {
		String portAttr = launch.getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN);
		int port;
		if (portAttr == null || (port = Integer.valueOf(portAttr)) == -1) {
			port = tryFindPort(launch);
			if (port != -1) {
				launch.setAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, Integer.toString(port));
			}
		}
		return port;
    }
    static private int tryFindPort(ILaunch launch) {
    	FileReader fr = null;
    	BufferedReader br = null;
    	try {
    		String filename = launch.getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_FILE_PORT);
    		if (filename != null) {
		    	File f = new File(filename);
		    	fr = new FileReader(f);
		    	br = new BufferedReader(fr);
		    	return Integer.valueOf(br.readLine());
    		}
    	} catch (IOException e) {
    		// maybe do not catch exception to not pollute log with false positives?
    	} catch (NumberFormatException e) {
    		// maybe do not catch exception to not pollute log with false positives?
    	} finally {
    		IOUtils.safeClose(br);
    		IOUtils.safeClose(fr);
    	}
		return -1;
    }
}
