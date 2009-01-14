package clojuredev.launching;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import clojuredev.ClojuredevPlugin;


public class ClojureLaunchDelegate extends
        JavaLaunchDelegate {
	
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
