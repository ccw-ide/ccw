/*******************************************************************************
 * Copyright (c) 2012 Laurent Petit and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent Petit - initial API and implementation
 *******************************************************************************/
package ccw.leiningen;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "ccw.leiningen";

    /** The shared instance */
    private static Activator plugin;

    public Activator() { }
    
    public void start(BundleContext context) throws Exception {
    	System.out.println("Leiningen Plugin start()");
        super.start(context);
        plugin = this;
    }
    
    public void stop(BundleContext context) throws Exception {
    	System.out.println("Leiningen Plugin stop()");
        plugin = null;
        super.stop(context);
    }
    
    /**
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    // TODO export those logError, etc. helper methods into their own class or namespace ...
    
    public static void logError(String msg) {
        plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, msg));
    }

    public static void logError(String msg, Throwable e) {
        plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, msg, e));
    }

    public static void logError(Throwable e) {
        plugin.getLog().log(
                new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
    }
    
    public static IStatus createErrorStatus(String message, Throwable e) {
    	return new Status(IStatus.ERROR, PLUGIN_ID, message, e);
    }

    public static IStatus createErrorStatus(String message) {
    	return new Status(IStatus.ERROR, PLUGIN_ID, message);
    }
    
    public static void logWarning(String msg) {
        plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, msg));
    }

    public static void logWarning(String msg, Throwable e) {
        plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, msg, e));
    }

    public static void logWarning(Throwable e) {
        plugin.getLog().log(
                new Status(IStatus.WARNING, PLUGIN_ID, e.getMessage(), e));
    }

    public static void log (String msg) {
        plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, msg));
    }
    
}
