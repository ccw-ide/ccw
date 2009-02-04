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
 *******************************************************************************/
package clojuredev;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import clojure.lang.Compiler;
import clojuredev.debug.ClojureClient;
import clojuredev.launching.LaunchUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class ClojuredevPlugin extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "clojuredev";

    /** 
     * @param swtKey a key from SWT.COLOR_xxx
     * @return a system managed color (callers must not dispose 
     *         the color themselves)
     */
	public static Color getSystemColor(int swtKey) { 
		return Display.getDefault().getSystemColor(swtKey); 
	}
	
	/**
	 * @param index the index in an internal table of ClojuredevPlugin
	 * @return the color corresponding to the index (callers must no
	 *         dispose the color themselves)
	 */
	public static Color getClojuredevColor(int index) {
		return ClojuredevPlugin.getDefault().allColors[index];
	}
	
    /** The shared instance */
    private static ClojuredevPlugin plugin;

    /** "Read-only" table, do not alter */
    public Color[] allColors;
    
    public ClojuredevPlugin() {
    }
    
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        plugin = this;
        loadPluginClojureCode();
        initializeParenRainbowColors();
        
    }

    private void loadPluginClojureCode() throws Exception {
		URL clientReplBundleUrl = ClojuredevPlugin.getDefault().getBundle().getResource("clojuredev/debug/clientrepl.clj");
		URL clientReplFileUrl = FileLocator.toFileURL(clientReplBundleUrl);
		String clientRepl = clientReplFileUrl.getFile(); 

		Compiler.loadFile(clientRepl);
    }
    
    public void stop(BundleContext context) throws Exception {
    	disposeParenRainbowColors();
        plugin = null;
        super.stop(context);
    }
    
    private void initializeParenRainbowColors() {
        allColors = new Color[] {
                new Color(Display.getDefault(), 0x00, 0xCC, 0x00),
                new Color(Display.getDefault(), 0x00, 0x88, 0xAA),
                new Color(Display.getDefault(), 0x66, 0x00, 0xAA),
                new Color(Display.getDefault(), 0x00, 0x77, 0x00),
                new Color(Display.getDefault(), 0x77, 0xEE, 0x00),
                new Color(Display.getDefault(), 0xFF, 0x88, 0x00)
            };
    }
    
    private void disposeParenRainbowColors() {
    	if (allColors != null) {
        	for(Color c : allColors) {
        		if (c!=null && !c.isDisposed()) {
        			c.dispose();
        		}
            }
    	}
    }

    /**
     * @return the shared instance
     */
    public static ClojuredevPlugin getDefault() {
        return plugin;
    }

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

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
//    	ClojuredevPluginImages.initializeImageRegistry(reg);
    	// TODO Auto-generated method stub
//    	super.initializeImageRegistry(reg);
    	reg.put(NS, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jdt/package_obj.gif")));
    	reg.put(PUBLIC_FUNCTION, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jdt/methpub_obj.gif")));
        reg.put(PRIVATE_FUNCTION, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jdt/methpri_obj.gif")));
    }
    public static final String NS = "icon.namespace";
    public static final String PUBLIC_FUNCTION = "icon.function.public";
    public static final String PRIVATE_FUNCTION = "icon.function.private";

    private List<ILaunch> launches = new ArrayList<ILaunch>(); 
	/**
	 * @param launch
	 */
	public void addLaunch(ILaunch launch) {
		if (!launch.isTerminated()) {
			launches.add(launch);
		}
	}
	
	// TODO see if synchronized is mandatory ?
	public synchronized ClojureClient getProjectClojureClient(IProject project) {
		Iterator<ILaunch> it = launches.iterator();
		while (it.hasNext()) {
			ILaunch launch = it.next();
			if (launch.isTerminated()) {
				it.remove();
				continue;
			}
			String launchProject = launch.getAttribute(LaunchUtils.ATTR_PROJECT_NAME);
			if (launchProject != null && launchProject.equals(project.getName())) {
				String portAttr = launch.getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN);
				if (portAttr != null) {
					Integer port = Integer.valueOf(portAttr);
					if (port != -1) {
						return new ClojureClient(port);
					}
				}
			}
		}
		return null;
	}
}
