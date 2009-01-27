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
package clojuredev.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public final class LaunchUtils implements IJavaLaunchConfigurationConstants {

    private LaunchUtils(){}
    
    static public final String LAUNCH_ID = "clojuredev.launching.clojure";
    
    static public final String MAIN_CLASSNAME = "clojure.lang.Repl";
    
    static public final int DEFAULT_SERVER_PORT = 8503;
    
	/** Launch attribute that will be of type String, the files will be listed separated by newlines */
    static public final String ATTR_FILES_LAUNCHED_AT_STARTUP = "CLOJUREDEV_ATTR_FILES_LAUNCHED_AT_STARTUP";

	public static final String ATTR_CLOJURE_SERVER_LISTEN = "CLOJUREDEV_ATTR_CLOJURE_SERVER_LISTEN";

    static public String getProgramArguments(IFile[] files) {
        StringBuilder args = new StringBuilder();
        for (IFile srcFile : files) {
            if (args.length() > 0) {
                args.append(" ");
            }
            args.append("\"" + srcFile.getLocation().toString() + "\"");
        }
        return args.toString();
    }
    
    static public String getProgramArguments(List<IFile> files) {
        return getProgramArguments(files.toArray(new IFile[]{}));
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
    
    static public String getFilesToLaunchAsCommandLineList(ILaunchConfiguration config) throws CoreException {
    	List<IFile> filesToLaunch = LaunchUtils.getFilesToLaunchList(config);
    	return LaunchUtils.getProgramArguments(filesToLaunch);

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
