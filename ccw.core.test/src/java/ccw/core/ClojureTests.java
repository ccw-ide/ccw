package ccw.core;

import java.util.ArrayList;
import java.util.List;

import mikera.cljunit.ClojureRunner;
import mikera.cljunit.ClojureTest;

import org.junit.runner.RunWith;

import ccw.CCWPlugin;
import ccw.util.osgi.ClojureOSGi;

@RunWith(ClojureRunner.class)
public class ClojureTests extends ClojureTest {
    // automatically test all Clojure namespaces in classpath

	private final List<String> namespaces = new ArrayList<String>();
	
	public ClojureTests() {
		
		namespaces.add("ccw.edn-test");
		
		requireNamespaces(namespaces);
	}
	
	private static void requireNamespaces(List<String> namespaces) {
		for (String namespace: namespaces) {
			ClojureOSGi.require(CCWPlugin.getDefault().getBundle(), namespace);
		}
	}
	
	public List<String> namespaces() {
		return namespaces;
	}
}

