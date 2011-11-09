package ccw.util;

import java.util.concurrent.ConcurrentHashMap;

import clojure.lang.RT;
import clojure.lang.Var;

public final class ClojureUtils {
	private static ConcurrentHashMap<String, Var> cachedVars = new ConcurrentHashMap<String, Var>();
	
	private static Var var(String ns, String name) {
		String varName = ns + "/" + name;
		Var v = cachedVars.get(varName);
		if (v != null) {
			return v;
		} else {
			v = RT.var(ns, name);
			if (v == null) {
				throw new RuntimeException("Not possible to find var " + varName);
			} else {
				return cachedVars.putIfAbsent(varName, v);
			}
		}
	}
	public static Object invoke(String ns, String name) {
		try {
			return var(ns, name).invoke();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static Object invoke(String ns, String name, Object arg1) {
		try {
			return var(ns, name).invoke(arg1);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static Object invoke(String ns, String name, Object arg1, Object arg2) {
		try {
			return var(ns, name).invoke(arg1, arg2);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static Object invoke(String ns, String name, Object arg1, Object arg2, Object arg3) {
		try {
			return var(ns, name).invoke(arg1, arg2, arg3);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static Object invoke(String ns, String name, Object arg1, Object arg2, Object arg3, Object arg4) {
		try {
			return var(ns, name).invoke(arg1, arg2, arg3, arg4);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static Object invoke(String ns, String name, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		try {
			return var(ns, name).invoke(arg1, arg2, arg3, arg4, arg5);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
