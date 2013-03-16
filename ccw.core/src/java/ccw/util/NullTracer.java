package ccw.util;

/**
 * @author laurentpetit
 */
public final class NullTracer implements ITracer {
    public static final NullTracer INSTANCE = new NullTracer();
    
    private NullTracer() {}
    
    public boolean isEnabled(String traceOption) { return true; }

    public void trace(String traceOption, Object... message) {
    	System.out.println("trace[traceOption:" + traceOption + ", message:"
    			+ Tracer.buildMessage(message) + "]");
    }

    public void trace(String traceOption, Throwable throwable, Object... message) {
    	System.out.println("trace[traceOption:" + traceOption + ", message:"
    			+ Tracer.buildMessage(message) + "]");
    	throwable.printStackTrace();
    }

    public void traceDumpStack(String traceOption) {
    	try {
    		throw new RuntimeException("traceDumpStack[traceOption:" + traceOption + "]");
    	} catch (RuntimeException e) {
    		e.printStackTrace();
    	}
    }

    public void traceEntry(String traceOption) {
    	try {
    		throw new RuntimeException("traceEntry[traceOption:" + traceOption + "]");
    	} catch (RuntimeException e) {
    		e.printStackTrace();
    	}
    }

    public void traceEntry(String traceOption, Object... arguments) {
    	try {
    		throw new RuntimeException("traceEntry[traceOption:" + traceOption
    				+ ", arguments:" + Tracer.buildMessage(arguments) + "]");
    	} catch (RuntimeException e) {
    		e.printStackTrace();
    	}
    }

    public void traceExit(String traceOption) {
    	try {
    		throw new RuntimeException("traceExit[traceOption:" + traceOption + "]");
    	} catch (RuntimeException e) {
    		e.printStackTrace();
    	}
    }

    public void traceExit(String traceOption, Object returnValue) {
    	try {
    		throw new RuntimeException("traceExit[traceOption:" + traceOption 
    				+ ", returnValue:" + returnValue + "]");
    	} catch (RuntimeException e) {
    		e.printStackTrace();
    	}
    }
    
}
