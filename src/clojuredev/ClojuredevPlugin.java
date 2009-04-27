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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import clojure.lang.Compiler;
import clojuredev.debug.ClojureClient;
import clojuredev.editors.antlrbased.AntlrBasedClojureEditor;
import clojuredev.launching.LaunchUtils;
import clojuredev.util.DisplayUtil;
import clojuredev.utils.editors.antlrbased.IScanContext;

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
    
    private ColorRegistry colorRegistry;
    
    public ClojuredevPlugin() {
    }
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        plugin = this;
        loadPluginClojureCode();
        initializeParenRainbowColors();
        createColorRegistry();
        startLaunchListener();
    }
    
    private void createColorRegistry() {
    	if (colorRegistry == null) {
    		DisplayUtil.syncExec(new Runnable() {
				public void run() {
		    		colorRegistry = new ColorRegistry(getWorkbench().getDisplay());
		    		AntlrBasedClojureEditor.registerEditorColors(colorRegistry);
				}});
    	}
    }
    
    public ColorRegistry getColorRegistry() {
    	return colorRegistry;
    }

    private void loadPluginClojureCode() throws Exception {
		URL clientReplBundleUrl = ClojuredevPlugin.getDefault().getBundle().getResource("clojuredev/debug/clientrepl.clj");
		URL clientReplFileUrl = FileLocator.toFileURL(clientReplBundleUrl);
		String clientRepl = clientReplFileUrl.getFile(); 

		Compiler.loadFile(clientRepl);
    }
    
    public void stop(BundleContext context) throws Exception {
    	disposeParenRainbowColors();
    	stopLaunchListener();
    	// We don't remove colors when deregistered, because, well, we don't have a
    	// corresponding method on the ColorRegistry instance!
        plugin = null;
        super.stop(context);
    }
    
    private void initializeParenRainbowColors() {
    	DisplayUtil.syncExec(new Runnable() {
			public void run() {
		        allColors = new Color[] {
		                new Color(Display.getDefault(), 0x00, 0xCC, 0x00),
		                new Color(Display.getDefault(), 0x00, 0x88, 0xAA),
		                new Color(Display.getDefault(), 0x66, 0x00, 0xAA),
		                new Color(Display.getDefault(), 0x00, 0x77, 0x00),
		                new Color(Display.getDefault(), 0x77, 0xEE, 0x00),
		                new Color(Display.getDefault(), 0xFF, 0x88, 0x00)
		            };
			}});
    }
    
    private void disposeParenRainbowColors() {
    	if (allColors != null) {
    		DisplayUtil.syncExec(new Runnable() {
				public void run() {
		        	for(Color c : allColors) {
		        		if (c!=null && !c.isDisposed()) {
		        			c.dispose();
		        		}
		            }
				}});
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
    private ILaunchListener launchListener = new ILaunchListener() {
		public void launchAdded(ILaunch launch) {
			updateLaunchList(launch);
		}
		public void launchChanged(ILaunch launch) {
			updateLaunchList(launch);
		}
		private void updateLaunchList(ILaunch launch) {
			if (LaunchUtils.getLaunchServerReplPort(launch) != -1) {
				launches.add(launch);
			} else {
				launches.remove(launch);
			}
		}
		public void launchRemoved(ILaunch launch) {
			launches.remove(launch);
		}
	};
	
    private void startLaunchListener() {
		stopLaunchListener();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(launchListener);
    }
    
    private void stopLaunchListener() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(launchListener);
		launches.clear();
    }
    
	public ClojureClient getProjectClojureClient(IProject project) {
		for (ILaunch launch: launches) {
			if (launch.isTerminated()) {
				continue;
			}
			String launchProject = launch.getAttribute(LaunchUtils.ATTR_PROJECT_NAME);
			if (launchProject != null && launchProject.equals(project.getName())) {
				return new ClojureClient(LaunchUtils.getLaunchServerReplPort(launch));
			}
		}
		return null;
	}

	
	private IScanContext scanContext;

	public synchronized IScanContext getDefaultScanContext() {
		if (scanContext == null) {
			scanContext = new StaticScanContext();
		}
		return scanContext;
	}
}
