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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jdt.annotation.NonNull;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import ccw.core.StaticStrings;
import ccw.editors.clojure.ClojureEditor;
import ccw.editors.clojure.IClojureEditor;
import ccw.editors.clojure.scanners.IScanContext;
import ccw.launching.LaunchUtils;
import ccw.nature.AutomaticNatureAdder;
import ccw.preferences.PreferenceConstants;
import ccw.repl.REPLView;
import ccw.repl.SafeConnection;
import ccw.util.ClojureInvoker;
import ccw.util.DisplayUtil;
import ccw.util.ITracer;
import ccw.util.NullTracer;
import ccw.util.Tracer;
import clojure.lang.Keyword;

/**
 * The activator class controls the plug-in life cycle
 */
public class CCWPlugin extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "ccw.core";

    /** Leiningen project nature */
    public static final String LEININGEN_NATURE_ID = "ccw.leiningen.nature";

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

	private AutomaticNatureAdder synchronizedNatureAdapter;

	private ITracer tracer = NullTracer.INSTANCE;

	public static ITracer getTracer() {
		CCWPlugin plugin = getDefault();
		if (plugin != null && plugin.tracer != null)
			return plugin.tracer;
		else
			return NullTracer.INSTANCE;
	}

    public void startREPLServer() {
    	try {
    	    ClojureInvoker.newInvoker(this, "ccw.core.launch").__("ccw-nrepl-start-if-necessary");
    	} catch (Exception e) {
    		CCWPlugin.logError("Could not start plugin-hosted REPL server", e);
    		throw new RuntimeException("Could not start plugin-hosted REPL server", e);
    	}
    }

    public int getREPLServerPort() {
    	startREPLServer();
    	return (Integer) ClojureInvoker.newInvoker(this, "ccw.core.launch").__("ccw-nrepl-port");
    }
    
    public void startEventHandlers() {
    	ClojureInvoker.newInvoker(this, "ccw.core.event-bus").__("start");
    }
    
    public void startEventSubscription() {
    	ClojureInvoker.newInvoker(this, "ccw.repl.visible-in-all-perspectives").__("start");
    }
    
    public void startNamespaces() {
//    	ClojureInvoker.newInvoker(this, "ccw.editors.clojure.code-content-assist").__("start");
    	ClojureInvoker.newInvoker(this, "ccw.editors.clojure.code-context-information").__("start");
    	ClojureInvoker.newInvoker(this, "ccw.editors.clojure.code-completion-proposal").__("start");
    }

    /**
     * Record of a found CCW dependency. Only used at plugin startup to
     * log found dependencies.
     * @see logDependenciesInformation(final BundleContext context)
     */
    private static class CCWDependency {
    	public final String groupId;
    	public final String artifactId;
    	public final String version;
    	public CCWDependency(String g, String a, String v) {
    		groupId = g; artifactId = a; version = v;
    	}
    }
    /**
     * Record of a found CCW dependency in error. Only used at plugin startup to
     * log found dependencies.
     * @see logDependenciesInformation(final BundleContext context)
     */
    private static class CCWDependencyErr {
    	public final URL url;
    	public final Exception exception;
    	public CCWDependencyErr(URL u, Exception e) {
    		url = u; exception = e;
    	}
    }

    /**
     * Method implemented in java so we don't trigger clojure accidentally,
     * because this method is called really early in the plugin initialization
     * process
     */
    private final void logDependenciesInformation(final BundleContext context) {

    	// Collect bundle embedded external libraries
    	Enumeration<URL> entries = context.getBundle().findEntries("lib", "pom.properties", true);

    	// Initialize colls for dependencies and dependencies in error
    	// dependencies will be sorted first via groupId, then artifactId
    	TreeSet<CCWDependency> deps = new TreeSet<CCWDependency>(new Comparator<CCWDependency>() {
			@Override public int compare(CCWDependency d1, CCWDependency d2) {
				if (d1.groupId.equals(d2.groupId)) {
					return d1.artifactId.compareTo(d2.artifactId);
				} else {
					return d1.groupId.compareTo(d2.groupId);
				}
			}
		});
    	// errors are collected without specific ordering
		List<CCWDependencyErr> errs = new ArrayList<CCWDependencyErr>();

		// Collect dependencies and dependencies in error, gathering as much
		// info as possible
    	if (entries != null) {
    		while (entries.hasMoreElements()) {
    			URL entry = entries.nextElement();
    			Reader r = null;
    			try {
	    			r = new InputStreamReader(entry.openStream(), Charset.forName("UTF-8"));
	    			Properties p = new Properties();
	    			p.load(r);
	    			String groupId = p.getProperty("groupId");
	    			String artifactId = p.getProperty("artifactId");
	    			String version = p.getProperty("version");
	    			CCWDependency d = new CCWDependency(groupId, artifactId, version);
	    			deps.add(d);
    			} catch (IOException e) {
    				errs.add(new CCWDependencyErr(entry, e));
    			} finally {
    				if (r != null)
    					try { r.close(); } catch (IOException e) { /* silently ignore */ }
    			}
    		}
    	}

    	// Create the dependencies report String and log it
		StringBuilder sb = new StringBuilder();
		sb.append("Counterclockwise dependencies:");
		final String indent = "        ";
    	if (entries != null) {
    		for (CCWDependencyErr err: errs) {
    			logWarning("Exception while trying to find maven information for Counterclockwise dependency " + err.url.toExternalForm(), err.exception);
    			sb.append("\n" + indent);
				sb.append(err.url.toExternalForm() + ": " + err.exception.getMessage());
    		}
    		for (CCWDependency d: deps) {
    			String name = (d.groupId.equals(d.artifactId)) ? d.groupId : d.groupId + "/" + d.artifactId;
    			sb.append("\n" + indent);
    			sb.append(String.format("[%s \"%s\"]", name, d.version));
    		}
    	} else {
    		sb.append(indent + "no entries found in bundle");
    	}
    	// We do not call CCWPlugin.log() because it would duplicate the log
    	// in .metadata/.log because at the time it's called, it would not find
    	// the trace() machinery already installed
    	plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, sb.toString()));
    }

    public CCWPlugin() { }

    private volatile int bundleState = Bundle.UNINSTALLED;

    /**
     * Can code be loaded in the current bundle?
     * @return True or false.
     */
    public static boolean canLoadCodeInBundle() {
        CCWPlugin plugin = CCWPlugin.getDefault();
        return (plugin != null && plugin.bundleState == Bundle.ACTIVE);
    }
    
    @Override
	public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        log("CCWPlugin.start(): ENTER");

		tracer = new Tracer(context);

        logDependenciesInformation(context);

        initInjections(context);
        
        context.addBundleListener(new BundleListener() {

			@Override
			public void bundleChanged(BundleEvent evt) {
				if (evt.getBundle() == CCWPlugin.this.getBundle()) {
					bundleState = evt.getType();

					if (evt.getType() == BundleEvent.STARTED) {

						// We immediately give control back to the OSGi framework application
						// by starting the code in a new thread
						new Thread(new Runnable() {
							@Override public void run() {
								// Some Eclipse plugins, such as LaunchingResourceManager
								// call PlatformUI.getWorbench() and checking for null,
								// even though null is not a valid return value
								// (instead, an exception is thrown), resulting
								// in the whole Eclipse to collapse.
								// Let's protect this code once and for all by ensuring
								// That the Workbench has been initialized before calling
								// the initialization code
								while(!PlatformUI.isWorkbenchRunning()) {
									try {
										if (CCWPlugin.this.getBundle().getState()!=Bundle.ACTIVE)
											return;
										Thread.sleep(200);
									} catch (InterruptedException e) {
										logError("Error while querying for the active bundle", e);
									}
								}

								// The Workbench may not be initialized, causing weird issues e.g. in Kepler
								// WorkbenchThemeManager.getInstance() in Kepler for instance does
								// not ensure the instance is created in the UI Thread
								// Once the Workbench is initialized, WorkbenchThemeManager & all
								// are ensured to be created, so this removes a bunch of plugin startup
								// race conditions
								final IWorkbench workbench = PlatformUI.getWorkbench();
								final AtomicBoolean isWorkbenchInitialized = new AtomicBoolean();
								while (true) {
									workbench.getDisplay().syncExec(new Runnable() {
										@Override public void run() {
											if (workbench.getActiveWorkbenchWindow() != null) {
												// we've got an active window, so workbench is initialized!
												isWorkbenchInitialized.set(true);
											}
										}});
									if (isWorkbenchInitialized.get()) {
										break;
									} else {
										try {
											Thread.sleep(50);
										} catch (InterruptedException e) {
											logError("Error while trying to Thread.sleep.", e);
										}
									}
								}

								// Here, the workbench is initialized
								if (System.getProperty(StaticStrings.CCW_PROPERTY_NREPL_AUTOSTART) != null) {
									try {
										startNamespaces();
										startREPLServer();
										startEventHandlers();
										startEventSubscription();
									} catch (Exception e) {
										logError("Error while querying for property: " + StaticStrings.CCW_PROPERTY_NREPL_AUTOSTART, e);
									}
								}

								getNatureAdapter().start();
							}

						}).start();
					}
				}
			}
		});

		// Adding hover extension listener
		ClojureInvoker invoker = ClojureInvoker.newInvoker(this, "ccw.editors.clojure.hover-support");
		invoker.__("add-registry-listener");
		invoker.__("add-preference-listener");

		log("CCWPlugin.start(): EXIT");
	}
    
    private synchronized AutomaticNatureAdder getNatureAdapter() {
    	if (synchronizedNatureAdapter == null) {
    		synchronizedNatureAdapter = new AutomaticNatureAdder();
    	}
    	return synchronizedNatureAdapter;
    }
    
    private synchronized void createColorCache() {
    	if (colorCache == null) {
    	    colorCache = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
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
     * <p>Beware, the combined preference store can only be instanciated from the
     *    UI Thread.</p>
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
    	try {
    	    ClojureInvoker.newInvoker(this, "ccw.core.launch").__("ccw-nrepl-stop");
    	} catch (Exception e) {
    		logError("Error while trying to close ccw internal nrepl server", e);
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
		return CCWPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CCW_GENERAL_AUTO_RELOAD_ON_STARTUP_SAVE);
	}
	
	public static boolean isAutoReloadOnStartupSaveEnabled(ILaunch launch) {
		return (Boolean.parseBoolean(launch.getAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED)));
	}
    
    public static REPLView[] getREPLViews() {
    	final ArrayList<REPLView> ret = new ArrayList<REPLView>(5);
    	
    	DisplayUtil.syncExec(new Runnable() {
			public void run() {
		        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		        if (window != null) {
		            IWorkbenchPage[] pages = window.getPages();
		            for (int i = 0; i < pages.length; i++) {
		            	IWorkbenchPage page = pages[i];
			            if (page != null) {
			                for (IViewReference r : page.getViewReferences()) {
			                    IViewPart v = r.getView(false);
			                    if (REPLView.class.isInstance(v)) {
			                        ret.add((REPLView) v);
			                    }
			                }
			            }
		            }
		        }
			}
		});
        
        return ret.toArray(new REPLView[ret.size()]);
    }

    public static REPLView getProjectREPL (final IProject project) {
    	REPLView[] repls = getREPLViews();
    	
    	for (REPLView replView : repls) {
            ILaunch launch = replView.getLaunch();
            if (launch!=null && !launch.isTerminated()) {
                String launchProject = LaunchUtils.getProjectName(launch);
                if (launchProject != null && launchProject.equals(project.getName())) {
                	return replView;
                }
            }
        }
        
        return null;
    }
    
    public SafeConnection getProjectREPLSafeConnection (IProject project) {
        REPLView repl = getProjectREPL(project);
        return repl == null ? null : repl.getSafeToolingConnection();
    }
	
	private IScanContext scanContext;

	public synchronized IScanContext getDefaultScanContext() {
		if (scanContext == null) {
			scanContext = new StaticScanContext();
		}
		return scanContext;
	}
	
	/**
	 * @return null if a default color must be used (e.g. System color)
	 */
	public static RGB getPreferenceRGB(IPreferenceStore store, String preferenceKey) {
		String enabledKey = PreferenceConstants.getEnabledPreferenceKey(preferenceKey);
		if (store.contains(enabledKey)) {
		    return store.getBoolean(PreferenceConstants.getEnabledPreferenceKey(preferenceKey))
	                ? PreferenceConverter.getColor(store, preferenceKey)
	                : PreferenceConverter.getDefaultColor(store, preferenceKey);
		} else if (store.contains(preferenceKey)) {
			if (store.isDefault(preferenceKey)) {
				return PreferenceConverter.getDefaultColor(store, preferenceKey);
			} else {
				return PreferenceConverter.getColor(store, preferenceKey);
			}
		} else {
			return null;
		}
	}
	
	private static void ensureColorInCache(ColorRegistry registry, String id, RGB rgb) {
	    if (!registry.hasValueFor(id)) {
	        registry.put(id, rgb);
        }
	}
	
	/** 
     * Not thread safe, but should only be called from the UI Thread, so it's
     * not really a problem.
     * @param rgbString The rgb string
     * @return The <code>Color</code> instance cached for this rgb value, creating
     *         it along the way if required.
     */
	public static Color getColor(String rgbString) {
	    ColorRegistry r = getDefault().getColorCache();
	    RGB rgb = StringConverter.asRGB(rgbString);
	    ensureColorInCache(r, rgbString, rgb);
        return r.get(rgbString);
	}
	
	/** 
	 * Not thread safe, but should only be called from the UI Thread, so it's
	 * not really a problem.
	 * @param rgb The RGB istance
	 * @return The <code>Color</code> instance cached for this rgb value, creating
	 *         it along the way if required.
	 */
	public static Color getColor(RGB rgb) {
		ColorRegistry r = getDefault().getColorCache();
		String rgbString = StringConverter.asString(rgb);
		ensureColorInCache(r, rgbString, rgb);
        return r.get(rgbString);
	}
	
	public static Color getPreferenceColor(IPreferenceStore store, String preferenceKey) {
		RGB rgb = getPreferenceRGB(store, preferenceKey);
		return rgb!=null ? getColor(rgb) : null;
	}
	
	/**
	 * Registers all the editor colors in the ColorRegistry, except the ones declared in plugin.xml,
	 * which are already in the registry.
	 * @param store
	 */
    public static void registerEditorColors(IPreferenceStore store) {
        final ColorRegistry colorCache = getDefault().getColorCache();

        for (Keyword token: PreferenceConstants.colorizableTokens) {
        	PreferenceConstants.ColorizableToken tokenStyle = PreferenceConstants.getColorizableToken(store, token);
        	String rgbString = StringConverter.asString(tokenStyle.rgb);
        	ensureColorInCache(colorCache, rgbString, tokenStyle.rgb);
        }
    }

    /**
	 * Return the Active editor or null if there is no focus.
	 * 
	 * @return An IEditorPart.
	 */
	public static IEditorPart getActiveEditor() {
		IEditorPart clojureEditor = null;
		
		IWorkbenchPage activePage = getDefault().internalGetActivePage();
		if (activePage != null) {
			clojureEditor = activePage.getActiveEditor();
		}
		
		
		return clojureEditor;
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

	/**
	 * Gets an instance of ccw.clojureeditor. 
	 * @return
	 */
	public static IClojureEditor getClojureEditor() {
	    ClojureEditor clojureEditor = null;
	    
	    // Dirty hack to get the Active Editor if out of focus (while stepping for instance).
	    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
	    if (windows != null) {

	        for (IWorkbenchWindow window : windows) {
	            IWorkbenchPage[] pages = window.getPages();

	            for (IWorkbenchPage page : pages) {
	                for (IEditorReference editorRef : page.getEditorReferences()) {
	                    if (editorRef.getId().equals(ClojureEditor.ID)) {
	                        clojureEditor = (ClojureEditor) editorRef.getEditor(false);
	                        if (clojureEditor != null) {
	                            break;
	                        }
	                    }
	                }
	                if (clojureEditor != null) {
	                    break;
	                }
	            }
	        }
	    }
        return clojureEditor;
	}
	
	/**
     * Called in AbstractUIPlugin in order to initialize application-context instances.
     */
	private void initInjections(BundleContext bundleContext) {
	    IEclipseContext c = EclipseContextFactory.getServiceContext(bundleContext);
	    
	    ClojureInvoker.newInvoker(this, "ccw.editors.clojure.hover-support").__("init-injections", c);
	}
	
    private void cleanInjections() {
        // Empty for now
    }
    
    public static IEclipseContext getEclipseContext() {
       return EclipseContextFactory.getServiceContext(CCWPlugin.getDefault().getBundle().getBundleContext());
    }
}
