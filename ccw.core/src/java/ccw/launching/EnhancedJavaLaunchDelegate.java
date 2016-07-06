/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent Petit - initial API and implementation
 *******************************************************************************/
package ccw.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import ccw.CCWPlugin;
import ccw.util.ClojureInvoker;

/**
 * Small enhancements over <code>JavaLaunchDelegate</code>.
 * <ul>
 * <li>Fix OS X not propagating environment variables: add additional PATHs to the PATH
 * </ul>
 * 
 * @author laurentpetit
 */
public class EnhancedJavaLaunchDelegate extends JavaLaunchDelegate {

	private final ClojureInvoker support = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.launching.enhanced-java-launch-delegate");

	public static final String ID_JAVA_APPLICATION = "ccw.launching.enhanced-java";

	@Override
	public String[] getEnvironment(ILaunchConfiguration configuration) throws CoreException {
		return (String[]) support.__("get-environment", this, configuration);
	}
	
	public String[] superGetEnvironment(ILaunchConfiguration configuration) throws CoreException {
		return super.getEnvironment(configuration);
	}

}
