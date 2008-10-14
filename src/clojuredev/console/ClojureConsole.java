package clojuredev.console;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.LispReader;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureConsole extends IOConsole implements Runnable {
	final static Color OUTPUT_COLOUR = new Color(null, 0, 0, 255); // blue
	final static Color ERROR_COLOUR = new Color(null, 255, 0, 0); // red
	final static Color INPUT_COLOUR = new Color(null, 0, 180, 180); // cyan

	static final Symbol USER = Symbol.create("user");
	static final Symbol CLOJURE = Symbol.create("clojure");

	static final Var in_ns = RT.var("clojure", "in-ns");
	static final Var refer = RT.var("clojure", "refer");
	static final Var ns = RT.var("clojure", "*ns*");
	static final Var warn_on_reflection = RT.var("clojure",
			"*warn-on-reflection*");

	private PrintStream out;
	private PrintStream info;
	private PrintStream err;
	
	private BlockingQueue queue = new LinkedBlockingQueue();
	
	public ClojureConsole() {
		super("Clojure REPL", null);
		Thread evalThread = new Thread(this);
		evalThread.start();
	}

	public void evalFile(IFile file) {
		try {
			queue.put(file);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void evalString(String string) {
		try {
			queue.put(string);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		final IOConsoleInputStream in = getInputStream();
		IOConsoleOutputStream ioOut = newOutputStream();
		IOConsoleOutputStream ioInfo = newOutputStream();
		IOConsoleOutputStream ioErr = newOutputStream();
		in.setColor(INPUT_COLOUR);
		ioOut.setColor(OUTPUT_COLOUR);
		ioInfo.setColor(INPUT_COLOUR);
		ioErr.setColor(ERROR_COLOUR);
		out = new PrintStream(ioOut);
		info = new PrintStream(ioInfo);
		err = new PrintStream(ioErr);

		try {
			// *ns* must be thread-bound for in-ns to work
			// thread-bind *warn-on-reflection* so it can be set!
			// must have corresponding popThreadBindings in finally clause
			Var.pushThreadBindings(RT.map(ns, ns.get(), warn_on_reflection,
					warn_on_reflection.get()));

			// create and move into the user namespace
			in_ns.invoke(USER);
			refer.invoke(CLOJURE);

			// repl IO support
			final LineNumberingPushbackReader rdr = new LineNumberingPushbackReader(
					new InputStreamReader(in, RT.UTF8));
			final Object EOF = new Object();
			
			new Thread(new Runnable(){

				@Override
				public void run() {
					for (;;) {
						try {
							Object r = LispReader.read(rdr, false, EOF, false);
							queue.put(r);
						}
						catch (Throwable e) {
							Throwable c = e;
							while (c.getCause() != null) {
								c = c.getCause();
							}
							err.println(c);
							e.printStackTrace(err);
						}
					}					
				}
				
			}).start();
			
			OutputStreamWriter w = new OutputStreamWriter(out);//(OutputStreamWriter) RT.OUT.get();// new
			// OutputStreamWriter(System.out);

			// start the loop
			w.write("Clojure\n");
			for (;;) {
				try {
					w.write("=> ");
					w.flush();
					
					Object r = queue.take();
					Object ret;
					if (r instanceof IFile) {
						ret = loadFile((IFile)r);
					}
					else {
						ret = eval(r);
					}
					
					RT.print(ret, w);
					w.write('\n');
					w.flush();
				}
				catch (Throwable e) {
					Throwable c = e;
					while (c.getCause() != null) {
						c = c.getCause();
					}
					err.println(c);
					e.printStackTrace(err);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(err);
		}
		finally {
			Var.popThreadBindings();
		}
	}

	private Object loadFile(IFile file) throws Exception {
		String fullPath = file.getLocation().toFile().toString();
		info.println("(load-file \"" + fullPath + "\")");
		return clojure.lang.Compiler.loadFile(fullPath);
	}
	
	private Object eval(Object r) throws Exception {
		return clojure.lang.Compiler.eval(r);
	}
	
}
