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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import clojuredev.util.IOUtils;

public final class LaunchUtils implements IJavaLaunchConfigurationConstants {

    private LaunchUtils(){}
    
    static public final String LAUNCH_ID = "clojuredev.launching.clojure";
    
    static public final String MAIN_CLASSNAME = "clojure.main";
    static public final String MAIN_CLASSNAME_FOR_REPL = "clojure.contrib.repl_ln";
    
    static public final int DEFAULT_SERVER_PORT = -1;
    
	/** Launch attribute that will be of type String, the files will be listed separated by newlines */
    static public final String ATTR_FILES_LAUNCHED_AT_STARTUP = "CLOJUREDEV_ATTR_FILES_LAUNCHED_AT_STARTUP";

	public static final String ATTR_CLOJURE_SERVER_LISTEN = "CLOJUREDEV_ATTR_CLOJURE_SERVER_LISTEN";

	public static final String ATTR_CLOJURE_SERVER_FILE_PORT = "clojuredev.debug.serverrepl.file.port";

	public static final String SERVER_FILE_PORT_PREFIX = "clojuredev.debug.serverrepl.port-";

	public static final String SERVER_FILE_PORT_SUFFFIX = ".port";

	public static final String ATTR_CLOJURE_INSTALL_REPL = "CLOJUREDEV_ATTR_CLOJURE_INSTALL_REPL";

	/**
	 * @param files
	 * @param lastFileAsScript if true, does not install the last arg as a resource to load, but as
	 *        a script to launch
	 * @return
	 */
    static public String getProgramArguments(IFile[] files, boolean lastFileAsScript) {
        StringBuilder args = new StringBuilder();
        
        int lastIndex = lastFileAsScript ? files.length - 1 : files.length;
        
        for (int i = 0; i < lastIndex; i++) {
            args.append(" -i" + fileArg(files[i]));
        }
        if (lastFileAsScript) {
        	args.append(fileArg(files[lastIndex]));
        }
        return args.toString();
    }
    
    private static String fileArg(IFile file) {
    	return " \"" + file.getLocation().toString() + "\"";
    }
    
    static public String getProgramArguments(List<IFile> files, boolean lastFileAsScript) {
        return getProgramArguments(files.toArray(new IFile[]{}), lastFileAsScript);
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
    	return LaunchUtils.getProgramArguments(filesToLaunch, lastFileAsScript);

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
