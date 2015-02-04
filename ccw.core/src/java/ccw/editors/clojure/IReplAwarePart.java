package ccw.editors.clojure;

import ccw.repl.REPLView;

/**
 * Interface of Repl aware parts.
 * @author Andrea Richiardi
 *
 */
public interface IReplAwarePart {

    /**
     * Can be null...
     */
    REPLView getCorrespondingREPL();
}
