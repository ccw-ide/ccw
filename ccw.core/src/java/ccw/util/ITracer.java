package ccw.util;


/**
 * @author laurentpetit
 */
public interface ITracer {
    /**
     * Is the trace option enable?
     * 
     * <p>
     * Options are NOT specified in the general form <i>&lt;Bundle-SymbolicName&gt;/&lt;option-path&gt;</i>
     * but with the option path only, preceded by a forward slash: <i>/&lt;option-path&gt;</i>.
     * For example <code>/debug</code> is good.
     * </p>
     * 
     * @param traceOption A trace option, always option path only. For example: <code>/log/info</code>
     * @return
     */
    boolean isEnabled(String traceOption);
    
    /**
     * Trace method
     * 
     * <p>
     * Options are NOT specified in the general form <i>&lt;Bundle-SymbolicName&gt;/&lt;option-path&gt;</i>
     * but with the option path only, preceded by a forward slash: <i>/&lt;option-path&gt;</i>.
     * For example <code>/debug</code> is good.
     * </p>
     * 
     * @param traceOption A trace option, always option path only. For example: <code>/log/info</code>
     * @param message One or more objects to be sent as trace message
     */
    void trace(String traceOption, Object... message);
    
    /**
     * Trace method
     * 
     * <p>
     * Options are NOT specified in the general form <i>&lt;Bundle-SymbolicName&gt;/&lt;option-path&gt;</i>
     * but with the option path only, preceded by a forward slash: <i>/&lt;option-path&gt;</i>.
     * For example <code>/debug</code> is good.
     * </p>
     * 
     * @param traceOption A trace option, always option path only. For example: <code>/log/info</code>
     * @param message One or more objects to be sent as trace message
     * @param throwable A throwable
     */
    void trace(String traceOption, Throwable throwable, Object... message);
    
    /**
     * Trace a dump stack.
     * 
     * <p>
     * Options are NOT specified in the general form <i>&lt;Bundle-SymbolicName&gt;/&lt;option-path&gt;</i>
     * but with the option path only, preceded by a forward slash: <i>/&lt;option-path&gt;</i>.
     * For example <code>/debug</code> is good.
     * </p>
     * 
     * @param traceOption A trace option, always option path only. For example: <code>/log/info</code>
     */
    void traceDumpStack(String traceOption);
    
    /**
     * Trace an entry point.
     * 
     * <p>
     * Options are NOT specified in the general form <i>&lt;Bundle-SymbolicName&gt;/&lt;option-path&gt;</i>
     * but with the option path only, preceded by a forward slash: <i>/&lt;option-path&gt;</i>.
     * For example <code>/debug</code> is good.
     * </p>
     * 
     * @param traceOption A trace option, always option path only. For example: <code>/log/info</code>
     */
    void traceEntry(String traceOption);
    
    /**
     * Trace an entry point.
     * 
     * <p>
     * Options are NOT specified in the general form <i>&lt;Bundle-SymbolicName&gt;/&lt;option-path&gt;</i>
     * but with the option path only, preceded by a forward slash: <i>/&lt;option-path&gt;</i>.
     * For example <code>/debug</code> is good.
     * </p>
     * 
     * @param traceOption A trace option, always option path only. For example: <code>/log/info</code>
     */
    void traceEntry(String traceOption, Object... arguments);

    /**
     * Trace an exit point.
     * 
     * <p>
     * Options are NOT specified in the general form <i>&lt;Bundle-SymbolicName&gt;/&lt;option-path&gt;</i>
     * but with the option path only, preceded by a forward slash: <i>/&lt;option-path&gt;</i>.
     * For example <code>/debug</code> is good.
     * </p>
     * 
     * @param traceOption A trace option, always option path only. For example: <code>/log/info</code>
     */
    void traceExit(String traceOption);

    /**
     * Trace an exit point.
     * 
     * <p>
     * Options are NOT specified in the general form <i>&lt;Bundle-SymbolicName&gt;/&lt;option-path&gt;</i>
     * but with the option path only, preceded by a forward slash: <i>/&lt;option-path&gt;</i>.
     * For example <code>/debug</code> is good.
     * </p>
     * 
     * @param traceOption A trace option, always option path only. For example: <code>/log/info</code>
     */
    void traceExit(String traceOption, Object returnValue);

}
