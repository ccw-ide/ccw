package ccw.util;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import clojure.lang.RT;
import clojure.lang.Var;

public class ClojureHandlerProxy extends AbstractHandler {
	private final Var execute;
	public ClojureHandlerProxy(final String handlerFn) {
		final String[] nsFn = handlerFn.split("/");
		execute = RT.var(nsFn[0], nsFn[1]);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			execute.invoke(this, event);
		} catch (Exception e) {
			throw new ExecutionException("clojure handler fn " + execute.ns + "/" + execute.sym + " threw an exception", e);
		}
		return null;
	}

}
