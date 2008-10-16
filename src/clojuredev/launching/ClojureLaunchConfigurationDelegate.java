package clojuredev.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class ClojureLaunchConfigurationDelegate extends
        AbstractJavaLaunchConfigurationDelegate {

    public void launch(ILaunchConfiguration configuration, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException {
        String workingDirectory = configuration.getAttribute(
                IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "");
        launch.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
                workingDirectory);

        System.out.println("launch!!!");
    }

}
