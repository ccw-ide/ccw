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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import ccw.CCWPlugin;


public class ClojureLaunchDelegate extends
        JavaLaunchDelegate {
	
	private ILaunch launch;
	
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
    	int port = LaunchUtils.getLaunchServerReplPort(launch); 
        launch.setAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, 
        		Integer.toString(port));
        launch.setAttribute(LaunchUtils.ATTR_PROJECT_NAME, configuration.getAttribute(LaunchUtils.ATTR_PROJECT_NAME, (String) null));
        if (port == -1) {
        	try {
				launch.setAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_FILE_PORT, 
						File.createTempFile(LaunchUtils.SERVER_FILE_PORT_PREFIX, LaunchUtils.SERVER_FILE_PORT_SUFFFIX).getAbsolutePath());
			} catch (IOException e) {
				throw new CoreException(Status.CANCEL_STATUS); // TODO do better than that ?
			}
        }
        this.launch = launch;
        super.launch(configuration, mode, launch, monitor);
    }
    
	@Override
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		int port = LaunchUtils.getLaunchServerReplPort(launch);
		StringBuilder sb = new StringBuilder();
		sb.append(" -D" + "clojure.remote.server.port" + "=" + Integer.toString(port));
		if (port == -1) {
			sb.append(" -D" + LaunchUtils.ATTR_CLOJURE_SERVER_FILE_PORT + "=" + launch.getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_FILE_PORT));
		}
		sb.append(" " + super.getVMArguments(configuration));
		return sb.toString();
	}
	
	@Override
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		String userProgramArguments = super.getProgramArguments(configuration);

		if (configuration.getAttribute(LaunchUtils.ATTR_CLOJURE_INSTALL_REPL, true)) {
			String filesToLaunchArguments = LaunchUtils.getFilesToLaunchAsCommandLineList(configuration, false);
			
			// Add serverrepl as a file to launch to install a remote server
			try {
				URL serverReplBundleUrl = CCWPlugin.getDefault().getBundle().getResource("ccw/debug/serverrepl.clj");
				URL serverReplFileUrl = FileLocator.toFileURL(serverReplBundleUrl);
				String serverRepl = serverReplFileUrl.getFile(); 
				filesToLaunchArguments = 
					"-i " + '\"' + serverRepl + "\" " 
					+ "-e \"(doseq [[v b] {(var *print-length*) 10000, (var *print-level*) 100}] (var-set v b))\" "
					+ filesToLaunchArguments;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	    	return filesToLaunchArguments + " --repl " + userProgramArguments;
		} else {
			String filesToLaunchArguments = LaunchUtils.getFilesToLaunchAsCommandLineList(configuration, true);
			
	    	return filesToLaunchArguments + userProgramArguments;
		}
	}
	
}
