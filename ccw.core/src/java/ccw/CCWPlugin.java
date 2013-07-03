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
package ccw;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import ccw.editors.clojure.IScanContext;
import ccw.launching.LaunchUtils;
import ccw.nature.AutomaticNatureAdder;
import ccw.preferences.PreferenceConstants;
import ccw.preferences.SyntaxColoringHelper;
import ccw.repl.REPLView;
import ccw.util.BundleUtils;
import ccw.util.DisplayUtil;
import ccw.util.ITracer;
import ccw.util.NullTracer;
import ccw.util.Tracer;
import clojure.lang.Keyword;
import clojure.lang.Var;
import clojure.tools.nrepl.Connection;

/**
 * The activator class controls the plug-in life cycle
 */
public class CCWPlugin extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "ccw.core";

    /** 
     * @param swtKey a key from SWT.COLOR_xxx
     * @return a system managed color (callers must not dispose 
     *         the color themselves)
     */
	public static Color getSystemColor(int swtKey) { 
		return Display.getDefault().getSystemColor(swtKey); 
	}
	
	
	//SHOULD LOG INFO / WARN / ERROR WHEN THE APPROPRIATE FLAGS ARE SET SO THAT ONE DOES NOT HAVE
	//TO GO FROM ONE FILE TO ANOTHER
	//ALSO CONSIDER VARIANTS FOR STACKTRACE, 
	//RAW STRING (no format), etc.
	
    /** The shared instance */
    private static CCWPlugin plugin;

    private ColorRegistry colorCache;
    
    private FontRegistry fontRegistry;
    
    private ServerSocket ackREPLServer;
    
	private AutomaticNatureAdder synchronizedNatureAdapter;

	private ITracer tracer = NullTracer.INSTANCE;
	
	public static ITracer getTracer() {
		CCWPlugin plugin = getDefault();
		if (plugin != null && plugin.tracer != null)
			return plugin.tracer;
		else
			return NullTracer.INSTANCE;
	}
    
    public synchronized void startREPLServer() throws CoreException {
    	if (ackREPLServer == null) {
	        try {
	        	// TODO use ClojureOSGi.withBundle instead
	        	Var startServer = BundleUtils.requireAndGetVar(getBundle().getSymbolicName(), "clojure.tools.nrepl.server/start-server");
	        	Object defaultHandler = BundleUtils.requireAndGetVar(
	        	        getBundle().getSymbolicName(),
	        	        "clojure.tools.nrepl.server/default-handler").invoke();
	        	Object handler = BundleUtils.requireAndGetVar(
	        	        getBundle().getSymbolicName(),
	        	        "clojure.tools.nrepl.ack/handle-ack").invoke(defaultHandler);
	            ackREPLServer = (ServerSocket)((Map)startServer.invoke(Keyword.intern("handler"), handler)).get(Keyword.intern("server-socket"));
	            CCWPlugin.log("Started ccw nREPL server: nrepl://localhost:" + ackREPLServer.getLocalPort());
	        } catch (Exception e) {
	            CCWPlugin.logError("Could not start plugin-hosted REPL server", e);
	            throw new CoreException(createErrorStatus("Could not start plugin-hosted REPL server", e));
	        }
    	}
    }
    
    public int getREPLServerPort() throws CoreException {
    	if (ackREPLServer == null) {
    		startREPLServer();
    	}
    	return ackREPLServer.getLocalPort();
    }

    public CCWPlugin() {
    	System.out.println("CCWPlugin instanciated");
    }
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
        System.out.println("CCWPlugin.start: ENTER");
        plugin = this;
        
        context.addBundleListener(new BundleListener() {
			
			@Override
			public void bundleChanged(BundleEvent evt) {
				if (evt.getBundle() == CCWPlugin.this.getBundle()
					&&	evt.getType() == BundleEvent.STARTED) {
					
					tracer = new Tracer(evt.getBundle().getBundleContext(), TraceOptions.getTraceOptions());
					
			        if (System.getProperty("ccw.autostartnrepl") != null) {
			        	try {
							startREPLServer();
						} catch (CoreException e) {
							e.printStackTrace();
						}
			        }
			        
			        getNatureAdapter().start();
			        
				}
				
			}
		});
        

        
        System.out.println("CCWPlugin.start: EXIT");
    }
    
    private synchronized AutomaticNatureAdder getNatureAdapter() {
    	if (synchronizedNatureAdapter == null) {
    		synchronizedNatureAdapter = new AutomaticNatureAdder();
    	}
    	return synchronizedNatureAdapter;
    }
    
    private synchronized void createColorCache() {
    	if (colorCache == null) {
    		colorCache = new ColorRegistry(getWorkbench().getDisplay());
    		colorCache.put("ccw.repl.expressionBackground", new RGB(0xf0, 0xf0, 0xf0));
    	}
    }
    
    /**
     * Must be called from the UI thread only
     */
    public ColorRegistry getColorCache() {
    	if (colorCache == null) {
    		createColorCache();
    	}
    	return colorCache;
    }
    
    private synchronized void createFontRegistry() {
    	if (fontRegistry == null) {
    		DisplayUtil.syncExec(new Runnable() {
    			public void run() {
    				fontRegistry = new FontRegistry(getWorkbench().getDisplay());
    				
                    // Forces initializations
                    fontRegistry.getItalic(""); // readOnlyFont
                    fontRegistry.getBold("");   // abstractFont
    			}
    		});
    	}
    }

    private FontRegistry getFontRegistry() {
    	if (fontRegistry == null) {
    		createFontRegistry();
    	}
    	return fontRegistry;
    }

    public final Font getJavaSymbolFont() {
        return getFontRegistry().getItalic("");
    }

    private IPreferenceStore prefs;
    
    /**
     * Create a preference store combined from the Clojure, the EditorsUI and
     * the PlatformUI preference stores to inherit all the default text editor
     * settings from the Eclipse preferences.
     * 
     * @return the combined preference store.
     */
    public IPreferenceStore getCombinedPreferenceStore() {
        if (prefs == null) {
            prefs = new ChainedPreferenceStore(new IPreferenceStore[] {
                    CCWPlugin.getDefault().getPreferenceStore(),
                    EditorsUI.getPreferenceStore(),
                    PlatformUI.getPreferenceStore()});
        }
        return prefs;
    }

    public void stop(BundleContext context) throws Exception {
    	
    	// We don't remove colors when deregistered, because, well, we don't have a
    	// corresponding method on the ColorRegistry instance!
    	// We also don't remove fonts when deregistered
    	stopREPLServer();
    	
    	this.getNatureAdapter().stop();
    	
        plugin = null;
        super.stop(context);
    }
    
    private void stopREPLServer() {
    	if (ackREPLServer != null) {
    		try {
				ackREPLServer.close();
			} catch (IOException e) {
				logError("Error while trying to close ccw internal nrepl server", e);
			}
    	}
    }
    
    /**
     * @return the shared instance
     */
    public static CCWPlugin getDefault() {
        return plugin;
    }

    public static void logError(String msg) {
    	getTracer().trace(TraceOptions.LOG_ERROR, "ERROR  - " + msg);
        plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, msg));
    }

    public static void logError(String msg, Throwable e) {
    	getTracer().trace(TraceOptions.LOG_ERROR, e, "ERROR  - " + msg);
        plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, msg, e));
    }

    public static void logError(Throwable e) {
    	getTracer().trace(TraceOptions.LOG_ERROR, e, "ERROR  - ");
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
    	getTracer().trace(TraceOptions.LOG_WARNING, "WARNING - " + msg);
        plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, msg));
    }

    public static void logWarning(String msg, Throwable e) {
    	getTracer().trace(TraceOptions.LOG_WARNING, e, "WARNING - " + msg);
        plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, msg, e));
    }

    public static void logWarning(Throwable e) {
    	getTracer().trace(TraceOptions.LOG_WARNING, e);
        plugin.getLog().log(
                new Status(IStatus.WARNING, PLUGIN_ID, e.getMessage(), e));
    }

    public static void log (String msg) {
    	getTracer().trace(TraceOptions.LOG_INFO, "INFO   - " + msg);
        plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, msg));
    }
    
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
    	reg.put(NS, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jdt/package_obj.gif")));
    	reg.put(PUBLIC_FUNCTION, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jdt/methpub_obj.gif")));
        reg.put(PRIVATE_FUNCTION, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jdt/methpri_obj.gif")));
        reg.put(CLASS, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jdt/class_obj.gif")));
        reg.put(SORT, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jdt/alphab_sort_co.gif")));
    }
    public static final String NS = "icon.namespace";
    public static final String PUBLIC_FUNCTION = "icon.function.public";
    public static final String PRIVATE_FUNCTION = "icon.function.private";
	public static final String CLASS = "class_obj.gif";
	public static final String SORT = "alphab_sort_co.gif";

	public static boolean isAutoReloadOnStartupSaveEnabled() {
		return CCWPlugin.getDefault().getCombinedPreferenceStore().getBoolean(PreferenceConstants.CCW_GENERAL_AUTO_RELOAD_ON_STARTUP_SAVE);
	}
	
	public static boolean isAutoReloadOnStartupSaveEnabled(ILaunch launch) {
		return (Boolean.parseBoolean(launch.getAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED)));
	}
    
    public REPLView getProjectREPL (final IProject project) {
    	final REPLView[] ret = new REPLView[1];
    	
    	DisplayUtil.syncExec(new Runnable() {
			public void run() {
		        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		        if (window != null) {
		            IWorkbenchPage page = window.getActivePage();
		            if (page != null) {
		                for (IViewReference r : page.getViewReferences()) {
		                    IViewPart v = r.getView(false);
		                    if (REPLView.class.isInstance(v)) {
		                        REPLView replView = (REPLView)v;
		                        ILaunch launch = replView.getLaunch();
		                        if (launch!=null && !launch.isTerminated()) {
		                            String launchProject = LaunchUtils.getProjectName(launch);
		                            if (launchProject != null && launchProject.equals(project.getName())) {
		                            	ret[0] = replView;
		                                return;
		                            }
		                        }
		                    }
		                }
		            }
		        }
			}
		});
        
        return ret[0];
    }
    
    public Connection getProjectREPLConnection (IProject project) {
        REPLView repl = getProjectREPL(project);
        return repl == null ? null : repl.getToolingConnection();
    }
	
	private IScanContext scanContext;

	public synchronized IScanContext getDefaultScanContext() {
		if (scanContext == null) {
			scanContext = new StaticScanContext();
		}
		return scanContext;
	}
	
	public static RGB getPreferenceRGB(IPreferenceStore store, String preferenceKey, RGB defaultColor) {
	    return
    	    store.getBoolean(SyntaxColoringHelper.getEnabledPreferenceKey(preferenceKey))
                ? PreferenceConverter.getColor(store, preferenceKey)
                : defaultColor;
	}
	
	/** 
	 * Not thread safe, but should only be called from the UI Thread, so it's
	 * not really a problem.
	 * @param rgb
	 * @return The <code>Color</code> instance cached for this rgb value, creating
	 *         it along the way if required.
	 */
	public static Color getColor(RGB rgb) {
		ColorRegistry r = getDefault().getColorCache();
		String rgbString = StringConverter.asString(rgb);
		if (!r.hasValueFor(rgbString)) {
			r.put(rgbString, rgb);
		}
		return r.get(rgbString);
	}
	
	public static Color getPreferenceColor(IPreferenceStore store, String preferenceKey, RGB defaultColor) {
		return getColor(getPreferenceRGB(store, preferenceKey, defaultColor));
	}
	
    public static void registerEditorColors(IPreferenceStore store, RGB foregroundColor) {
        final ColorRegistry colorCache = getDefault().getColorCache();
        
        for (Keyword token: PreferenceConstants.colorizableTokens) {
        	PreferenceConstants.ColorizableToken tokenStyle = PreferenceConstants.getColorizableToken(store, token, foregroundColor);
        	colorCache.put(tokenStyle.rgb.toString(), tokenStyle.rgb);
        }
        
    }
    
	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

    // copied from JavaPlugin
	private IWorkbenchPage internalGetActivePage() {
		IWorkbenchWindow window= getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return window.getActivePage();
	}
}
