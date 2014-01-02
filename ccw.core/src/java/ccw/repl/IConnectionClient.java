package ccw.repl;

import clojure.tools.nrepl.Connection;

/**
 * Interface to implement for being able to interact with an nrepl
 * connection
 * 
 * @author laurentpetit
 */
public interface IConnectionClient {
	<T> T withConnection(Connection c);
}