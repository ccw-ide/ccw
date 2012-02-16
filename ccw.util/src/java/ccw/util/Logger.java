package ccw.util;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class Logger {
    
    public enum Severity {
    	OK(IStatus.OK),
    	ERROR(IStatus.ERROR),
    	CANCEL(IStatus.CANCEL),
    	INFO(IStatus.INFO),
    	WARNING(IStatus.WARNING);
    	
    	public final int severityCode;
    	Severity(int severityCode) {
    		this.severityCode = severityCode;
    	}
    }
    
    private final String pluginId;
    private final ILog log;
    
    public Logger(String pluginId) {
    	this.pluginId = pluginId;
    	log = Platform.getLog(Platform.getBundle(this.pluginId));    
    }
    
    /**
     * Prints stack trace to Eclipse error log 
     */
    public void log(int severity, Throwable e) {
        Status s = new Status(severity, pluginId, IStatus.OK, e.getMessage(), e);
        log.log(s);
    }
    
    /**
     * Prints a message to the Eclipse error log
     */
    public void log(Severity severity, String msg) {
        Status s = new Status(severity.severityCode, pluginId, IStatus.OK, msg, null);
        log.log(s);
    }

}
