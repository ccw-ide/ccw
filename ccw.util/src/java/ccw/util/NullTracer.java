package ccw.util;

/**
 * @author laurentpetit
 */
public final class NullTracer implements ITracer {
    public static final NullTracer INSTANCE = new NullTracer();
    
    private NullTracer() {}
    
    public boolean isEnabled(String traceOption) { return false; }

    public void trace(String traceOption, Object... message) {}

    public void trace(String traceOption, Throwable throwable, Object... message) {}

    public void traceDumpStack(String traceOption) {}

    public void traceEntry(String traceOption) {}

    public void traceEntry(String traceOption, Object... arguments) {}

    public void traceExit(String traceOption) {}

    public void traceExit(String traceOption, Object returnValue) {}
    
}
