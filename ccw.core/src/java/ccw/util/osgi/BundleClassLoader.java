/*******************************************************************************
 * Copyright (c) 2009 Laurent Petit and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/

package ccw.util.osgi;

import java.net.URL;

import org.osgi.framework.Bundle;

public class BundleClassLoader extends ClassLoader {
	private Bundle _bundle;

	public BundleClassLoader(Bundle bundle) {
		_bundle = bundle;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return _bundle.loadClass(name);
	}

	@Override
	public URL getResource(String name) {
		return _bundle.getResource(name);
	}
}