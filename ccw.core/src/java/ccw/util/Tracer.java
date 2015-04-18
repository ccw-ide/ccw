package ccw.util;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;


/**
 * @author laurentpetit
 */
public class Tracer implements ITracer {

    private final String bundleSymbolicName;

    private final PluginDebugOptionsListener traceOptionsListener = new PluginDebugOptionsListener();

    private class PluginDebugOptionsListener implements DebugOptionsListener {
        @Override
		public void optionsChanged(DebugOptions options) {
            if (options.isDebugEnabled()) {
                debugTrace = options.newDebugTrace(bundleSymbolicName, Tracer.class);
            } else {
                debugTrace = null;
            }
            updateOptions(options);
        }
    }

    private DebugTrace debugTrace = null;

    private final Map<String, Boolean> options = new HashMap<String, Boolean>();

    public Tracer(final BundleContext bundleContext) {
        this.bundleSymbolicName = bundleContext.getBundle().getSymbolicName();
        enableTracing(bundleContext);
    }

    private void enableTracing(BundleContext context) {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(DebugOptions.LISTENER_SYMBOLICNAME, context.getBundle().getSymbolicName());
        context.registerService(
                DebugOptionsListener.class.getName(),
                traceOptionsListener,
                props);
    }

    private void updateOptions(DebugOptions options) {
        for (String option: options.getOptions().keySet()) {
            this.options.put(option, options.getBooleanOption(option, false));
        }
    }

    @Override
	public boolean isEnabled(String traceOption) {
        if (debugTrace == null) {
            return false;
        }

        Boolean res = options.get(traceOption);
        if (res != null) {
            return res;
        } else {
            return false;
        }
    }

    public static String buildMessage(Object[] message) {
        StringBuilder sb = new StringBuilder();
        for (Object m: message) {
            sb.append(m);
        }
        return sb.toString();
    }

    @Override
	public void trace(String traceOption, Object... message) {
        if (isEnabled(traceOption)) {
            debugTrace.trace("/" + traceOption, buildMessage(message));
        }
    }

    @Override
	public void trace(String traceOption, Throwable throwable, Object... message) {
        if (isEnabled(traceOption)) {
            debugTrace.trace("/" + traceOption, buildMessage(message), throwable);
        }
    }

    @Override
	public void traceDumpStack(String traceOption) {
        if (isEnabled(traceOption)) {
            debugTrace.traceDumpStack("/" + traceOption);
        }
    }

    @Override
	public void traceEntry(String traceOption) {
        if (isEnabled(traceOption)) {
            debugTrace.traceEntry("/" + traceOption);
        }
    }

    @Override
	public void traceEntry(String traceOption, Object... arguments) {
        if (isEnabled(traceOption)) {
            debugTrace.traceEntry("/" + traceOption, arguments);
        }
    }

    @Override
	public void traceExit(String traceOption) {
        if (isEnabled(traceOption)) {
            debugTrace.traceExit("/" + traceOption);
        }
    }

    @Override
	public void traceExit(String traceOption, Object returnValue) {
        if (isEnabled(traceOption)) {
            debugTrace.traceExit("/" + traceOption, returnValue);
        }
    }

}
