package clojuredev.launching;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public final class LaunchUtils implements IJavaLaunchConfigurationConstants {

    private LaunchUtils(){}
    
    static public final String LAUNCH_ID = "clojuredev.launching.clojure";
    
    static public final String MAIN_CLASSNAME = "clojure.lang.Repl";
    
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
    
}
