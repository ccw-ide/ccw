/*******************************************************************************
 * Copyright (c) 2009 Casey Marshall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshall  - initial API and implementation
 *    Thomas Ettinger - paren matching generic code
 *    Stephan Muehlstrasser - preference support for syntax coloring
 *******************************************************************************/
package ccw.util.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import clojure.osgi.ClojureOSGi;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "ccw.util";

    /** The shared instance */
    private static Activator plugin;

    public Activator() {}
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        startClojureCode(plugin.getBundle().getBundleContext());
    }
    
    private void startClojureCode(BundleContext bundleContext) throws Exception {
    	ClojureOSGi.require(bundleContext, "ccw.util.eclipse.repl");
    	ClojureOSGi.require(bundleContext, "ccw.util.eclipse");
    	ClojureOSGi.require(bundleContext, "ccw.util.doc-utils");
    	ClojureOSGi.require(bundleContext, "ccw.util.bundle");
    	ClojureOSGi.require(bundleContext, "ccw.util.factories");
    	ClojureOSGi.require(bundleContext, "ccw.util.string");
    }
    
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
    
    /**
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public static IStatus createErrorStatus(String message, Throwable e) {
    	return new Status(IStatus.ERROR, PLUGIN_ID, message, e);
    }

    public static IStatus createErrorStatus(String message) {
    	return new Status(IStatus.ERROR, PLUGIN_ID, message);
    }
    

}
