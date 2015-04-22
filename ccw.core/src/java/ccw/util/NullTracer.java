package ccw.util;

/**
 * @author laurentpetit
 */
public final class NullTracer implements ITracer {
    public static final NullTracer INSTANCE = new NullTracer();

    private NullTracer() {}

    @Override
	public boolean isEnabled(String traceOption) { return false; }

    @Override
	public void trace(String traceOption, Object... message) {}

    @Override
	public void trace(String traceOption, Throwable throwable, Object... message) {}

    @Override
	public void traceDumpStack(String traceOption) {}

    @Override
	public void traceEntry(String traceOption) {}

    @Override
	public void traceEntry(String traceOption, Object... arguments) {}

    @Override
	public void traceExit(String traceOption) {}

    @Override
	public void traceExit(String traceOption, Object returnValue) {}

}
