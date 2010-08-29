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

import java.net.URL;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.osgi.framework.BundleContext;

import ccw.editors.antlrbased.AntlrBasedClojureEditor;
import ccw.launching.LaunchUtils;
import ccw.lexers.ClojureLexer;
import ccw.preferences.PreferenceConstants;
import ccw.preferences.SyntaxColoringPreferencePage;
import ccw.repl.REPLView;
import ccw.util.DisplayUtil;
import ccw.utils.editors.antlrbased.IScanContext;
import cemerick.nrepl.Connection;
import clojure.lang.Compiler;
import clojure.lang.Symbol;
import clojure.lang.Var;

/**
 * The activator class controls the plug-in life cycle
 */
public class CCWPlugin extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "ccw";

    /** 
     * @param swtKey a key from SWT.COLOR_xxx
     * @return a system managed color (callers must not dispose 
     *         the color themselves)
     */
	public static Color getSystemColor(int swtKey) { 
		return Display.getDefault().getSystemColor(swtKey); 
	}
	
	/**
	 * @param index the index in an internal table of CCWPlugin
	 * @return the color corresponding to the index (callers must no
	 *         dispose the color themselves)
	 */
	public static Color getCCWColor(int index) {
		return CCWPlugin.getDefault().allColors[index];
	}
	
    /** The shared instance */
    private static CCWPlugin plugin;

    /** "Read-only" table, do not alter */
    public Color[] allColors;
    
    private ColorRegistry colorRegistry;
    
    private FontRegistry fontRegistry;
    
    public CCWPlugin() {
    }
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        plugin = this;
        loadPluginClojureCode();
        initializeParenRainbowColors();
        createColorRegistry();
    }
    
    private void createColorRegistry() {
    	if (colorRegistry == null) {
    		DisplayUtil.syncExec(new Runnable() {
				public void run() {
		    		colorRegistry = new ColorRegistry(getWorkbench().getDisplay());
				}});
    	}
    }
    
    public ColorRegistry getColorRegistry() {
    	return colorRegistry;
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

    private void loadPluginClojureCode() throws Exception {
		URL clientReplBundleUrl = CCWPlugin.getDefault().getBundle().getResource("ccw/debug/clientrepl.clj");
		URL clientReplFileUrl = FileLocator.toFileURL(clientReplBundleUrl);
		String clientRepl = clientReplFileUrl.getFile(); 

		Compiler.loadFile(clientRepl);

        try {
            Var.find(Symbol.intern("clojure.core/require")).invoke(Symbol.intern("cemerick.nrepl"));
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize nrepl library.", e);
        }
    }
    
    public void stop(BundleContext context) throws Exception {
    	disposeParenRainbowColors();
    	// We don't remove colors when deregistered, because, well, we don't have a
    	// corresponding method on the ColorRegistry instance!
    	// We also don't remove fonts when deregistered
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
    public static CCWPlugin getDefault() {
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

	public static boolean isAutoReloadEnabled(ILaunch launch) {
		return (Boolean.parseBoolean(launch.getAttribute(LaunchUtils.ATTR_IS_AUTO_RELOAD_ENABLED)));
	}
    
    public REPLView getProjectREPL (IProject project) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                for (IViewReference r : page.getViewReferences()) {
                    IViewPart v = r.getView(false);
                    if (REPLView.class.isInstance(v)) {
                        REPLView replView = (REPLView)v;
                        ILaunch launch = replView.getLaunch();
                        if (!launch.isTerminated()) {
                            String launchProject = launch.getAttribute(LaunchUtils.ATTR_PROJECT_NAME);
                            if (launchProject != null && launchProject.equals(project.getName())) {
                                return replView;
                            }
                        }
                    }
                }
            }
        }
        
        return null;
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
	
	private static RGB getElementColor(IPreferenceStore store, String preferenceKey, RGB defaultColor) {
	    return
    	    store.getBoolean(SyntaxColoringPreferencePage.getEnabledPreferenceKey(preferenceKey))
                ? PreferenceConverter.getColor(store, preferenceKey)
                : defaultColor;
	}
	
    public static void registerEditorColors(IPreferenceStore store, RGB foregroundColor) {
        final ColorRegistry colorRegistry = getDefault().getColorRegistry();
        
        final RGB literalColor = getElementColor(store, PreferenceConstants.EDITOR_LITERAL_COLOR, foregroundColor);
        final RGB specialFormColor = getElementColor(store, PreferenceConstants.EDITOR_SPECIAL_FORM_COLOR, foregroundColor);
        final RGB functionColor = getElementColor(store, PreferenceConstants.EDITOR_FUNCTION_COLOR, foregroundColor);
        final RGB commentColor = getElementColor(store, PreferenceConstants.EDITOR_COMMENT_COLOR, foregroundColor);
        final RGB globalVarColor = getElementColor(store, PreferenceConstants.EDITOR_GLOBAL_VAR_COLOR, foregroundColor);
        final RGB keywordColor = getElementColor(store, PreferenceConstants.EDITOR_KEYWORD_COLOR, foregroundColor);
        final RGB metadataTypehintColor = getElementColor(store, PreferenceConstants.EDITOR_METADATA_TYPEHINT_COLOR, foregroundColor);
        final RGB macroColor = getElementColor(store, PreferenceConstants.EDITOR_MACRO_COLOR, foregroundColor);
        
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.STRING, literalColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.REGEX_LITERAL, literalColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.NUMBER, literalColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.CHARACTER, literalColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.NIL, literalColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.BOOLEAN, literalColor); //$NON-NLS-1$

        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.OPEN_PAREN, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.CLOSE_PAREN, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SPECIAL_FORM, specialFormColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SYMBOL, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.FUNCTION, functionColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.GLOBAL_VAR, globalVarColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.MACRO, macroColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.SPECIAL_FORM, specialFormColor); //$NON-NLS-1$
        
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.JAVA_CLASS, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.JAVA_STATIC_METHOD, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.JAVA_INSTANCE_METHOD, foregroundColor); //$NON-NLS-1$
        
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.KEYWORD, keywordColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SYNTAX_QUOTE, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.UNQUOTE_SPLICING, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.UNQUOTE, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.COMMENT, commentColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SPACE, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.LAMBDA_ARG, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.METADATA_TYPEHINT, metadataTypehintColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.AMPERSAND, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.LEFT_SQUARE_BRACKET, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.RIGHT_SQUARE_BRACKET, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.LEFT_CURLY_BRACKET, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.RIGHT_CURLY_BRACKET, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.BACKSLASH, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.CIRCUMFLEX, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.COMMERCIAL_AT, foregroundColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.NUMBER_SIGN, foregroundColor); //$NON-NLS-1$
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
