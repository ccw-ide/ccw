/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Laurent PETIT - initial implementation
 *******************************************************************************/
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
		namespaces.add("ccw.launch-test");
		namespaces.add("ccw.extensions-test");
		namespaces.add("ccw.util-test");
		namespaces.add("ccw.editors.clojure.hover-support-test");
		
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

