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
package clojuredev.launching;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import clojuredev.ClojuredevPlugin;


public class ClojureLaunchDelegate extends
        JavaLaunchDelegate {
	
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        launch.setAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, 
                Integer.toString(configuration.getAttribute(
                        LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, 
                        LaunchUtils.DEFAULT_SERVER_PORT)));
        super.launch(configuration, mode, launch, monitor);
    }
    
	@Override
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		return " -D" + "clojure.remote.server.port" + "=" + Integer.toString(configuration.getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, LaunchUtils.DEFAULT_SERVER_PORT))
			+ super.getVMArguments(configuration);
	}
	
	@Override
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		String userProgramArguments = super.getProgramArguments(configuration);

		String filesToLaunchArguments = LaunchUtils.getFilesToLaunchAsCommandLineList(configuration);
		
		// Add serverrepl as a file to launch to install a remote server
		try {
			URL serverReplBundleUrl = ClojuredevPlugin.getDefault().getBundle().getResource("clojuredev/debug/serverrepl.clj");
			URL serverReplFileUrl = FileLocator.toFileURL(serverReplBundleUrl);
			String serverRepl = serverReplFileUrl.getFile(); 
			filesToLaunchArguments = '\"' + serverRepl + "\" " + filesToLaunchArguments;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (needsSeparator(userProgramArguments)) {
			userProgramArguments = " -- " + userProgramArguments;
		}
    	
    	return filesToLaunchArguments + userProgramArguments;
	}
	
	private boolean needsSeparator(String userArgs) {
		return ! userArgs.contains("-- ");
	}
	
}
