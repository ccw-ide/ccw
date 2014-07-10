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
	private final IConnectionLostListener connListener;
	
	public interface IConnectionLostListener {
		void connectionLost();
	}
	
	public SafeConnection(Connection connection, IConnectionLostListener connListener) {
		this.connection = connection;
		this.connListener = connListener;
	}
	
    public <T> T withConnection(final IConnectionClient client, long timeoutMillis)
    		throws Exception {
        Future<T> future = toolConnectionExecutor.submit(new Callable<T>() {
			@Override public T call() throws Exception {
				return client.withConnection(connection);
			}
		});
        try {
        	T t = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        	return t;
        } catch (Exception e) {
       		connListener.connectionLost();
        	throw e;
        }
    }
    
    public Connection.Response send(long timeoutMillis, final String... args) 
    		throws Exception {
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
	
	public void connectionLost() {
		connListener.connectionLost();
	}
	
	public static String safeNewSession(final Connection connection, final long timeoutMillis) throws InterruptedException, ExecutionException, TimeoutException {
        Future<String> future = toolConnectionExecutor.submit(new Callable<String>() {
			@Override public String call() throws Exception {
				return connection.newSession(null);
			}
		});
        return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
	}
}
