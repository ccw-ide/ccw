package ccw.editors.clojure;

import org.eclipse.jdt.annotation.Nullable;

import ccw.repl.REPLView;
import ccw.repl.SafeConnection;

/**
 * Interface of Repl aware parts.
 * @author Andrea Richiardi
 *
 */
public interface IReplAwarePart {

    /**
     * Can be null...
     */
    @Nullable REPLView getCorrespondingREPL();
    
    /**
     * Gets the connection.
     * @return The connection, or null if none
     */
    @Nullable SafeConnection getSafeToolingConnection();
}
