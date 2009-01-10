package clojuredev.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;


public class ClojureLaunchDelegate extends
        JavaLaunchDelegate {
	
	@Override
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		String userProgramArguments = super.getProgramArguments(configuration);

		String filesToLaunchArguments = LaunchUtils.getFilesToLaunchAsCommandLineList(configuration);
		
		if (needsSeparator(userProgramArguments)) {
			userProgramArguments = " -- " + userProgramArguments;
		}
    	
    	return filesToLaunchArguments + userProgramArguments;
	}
	
	private boolean needsSeparator(String userArgs) {
		return ! userArgs.contains("-- ");
	}
}
