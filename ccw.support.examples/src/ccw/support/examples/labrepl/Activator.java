/*******************************************************************************
 * Copyright (c) 2010 Stephan Muehlstrasser.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Stephan Muehlstrasser - initial API and implementation
 *    Laurent Petit         - adaptation to clojure.osgi 
 *******************************************************************************/

package ccw.support.examples.labrepl;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import clojure.osgi.ClojureOSGi;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ccw.support.examples";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		startClojureCode(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	private void startClojureCode(BundleContext bundleContext) throws Exception {
		ClojureOSGi.loadAOTClass(bundleContext, "ccw.support.examples.labrepl.wizards.LabreplAntLogger");
		ClojureOSGi.loadAOTClass(bundleContext, "ccw.support.examples.labrepl.wizards.LabreplCreateProjectPage");
		ClojureOSGi.loadAOTClass(bundleContext, "ccw.support.examples.labrepl.wizards.LabreplCreationOperation");
		ClojureOSGi.loadAOTClass(bundleContext, "ccw.support.examples.labrepl.wizards.LabreplCreationWizard");
	}

}
