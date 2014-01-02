package ccw.repl;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ccw.CCWPlugin;
import clojure.tools.nrepl.Connection;

/**
 * Wraps an nrepl connection and protects connections with timeouts.
 * 
 * @author laurentpetit
 */
public class SafeConnection {
	
	/** Executor Thread pool for Safe Connections */
	private static final ExecutorService toolConnectionExecutor = Executors.newCachedThreadPool();

	private final Connection connection;
	
	public SafeConnection(Connection connection) {
		this.connection = connection;
	}
	
    public <T> T withConnection(final IConnectionClient client, long timeoutMillis)
    		throws InterruptedException, java.util.concurrent.ExecutionException, TimeoutException {
        Future<T> future = toolConnectionExecutor.submit(new Callable<T>() {
			@Override public T call() throws Exception {
				return client.withConnection(connection);
			}
		});
        return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
    }
    
    public Connection.Response send(long timeoutMillis, final String... args) 
    		throws InterruptedException, ExecutionException, TimeoutException {
    	return withConnection(new IConnectionClient() {
			@Override public Connection.Response withConnection(Connection c) {
				return c.send(args);
			}
		}, timeoutMillis);
    }
    
	public void close() {
		toolConnectionExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					connection.close();
				} catch (IOException e) {
					CCWPlugin.logError("Error while closing nrepl connection", e);
				}
			}});
	}
	
	/**
	 * Use with extreme care !!
	 * 
	 * @return the unsafe wrapped nrepl connection
	 */
	public Connection getUnsafeConnection() {
		return connection;
	}
}
