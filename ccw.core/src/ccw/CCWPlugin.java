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
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ccw.debug.ClojureClient;
import ccw.editors.antlrbased.AntlrBasedClojureEditor;
import ccw.launching.LaunchUtils;
import ccw.lexers.ClojureLexer;
import ccw.preferences.PreferenceConstants;
import ccw.preferences.SyntaxColoringPreferencePage;
import ccw.util.DisplayUtil;
import ccw.utils.editors.antlrbased.IScanContext;
import clojure.lang.Compiler;

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
        startLaunchListener();
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

    private void loadPluginClojureCode() throws Exception {
		URL clientReplBundleUrl = CCWPlugin.getDefault().getBundle().getResource("ccw/debug/clientrepl.clj");
		URL clientReplFileUrl = FileLocator.toFileURL(clientReplBundleUrl);
		String clientRepl = clientReplFileUrl.getFile(); 

		Compiler.loadFile(clientRepl);
    }
    
    public void stop(BundleContext context) throws Exception {
    	disposeParenRainbowColors();
    	stopLaunchListener();
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
    }
    public static final String NS = "icon.namespace";
    public static final String PUBLIC_FUNCTION = "icon.function.public";
    public static final String PRIVATE_FUNCTION = "icon.function.private";
	public static final String CLASS = "class_obj.gif";


    private List<ILaunch> launches = new ArrayList<ILaunch>();
    private ILaunchListener launchListener = new ILaunchListener() {
		public void launchAdded(ILaunch launch) {
			updateLaunchList(launch);
		}
		public void launchChanged(ILaunch launch) {
			updateLaunchList(launch);
		}
		private void updateLaunchList(ILaunch launch) {
			if (isClojureEnabledLaunch(launch)) {
				launches.add(launch);
			} else {
				launches.remove(launch);
			}
		}
		public void launchRemoved(ILaunch launch) {
			launches.remove(launch);
		}
	};
	public static boolean isClojureEnabledLaunch(ILaunch launch) {
		return (launch.getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN) != null);
	}
	
	public static int findClojurePort(ILaunch launch) {
		String portAttr = launch.getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN);
		if (portAttr != null) {
			return Integer.valueOf(portAttr);
		} else {
			return -1;
		}
	}
	
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
				return new ClojureClient(findClojurePort(launch));
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
	
	private static RGB getElementColor(IPreferenceStore store, String preferenceKey, RGB defaultColor) {
	    return
    	    store.getBoolean(SyntaxColoringPreferencePage.getEnabledPreferenceKey(preferenceKey))
                ? PreferenceConverter.getColor(store, preferenceKey)
                : defaultColor;
	}
	
    public static void registerEditorColors(IPreferenceStore store) {
        final ColorRegistry colorRegistry = getDefault().getColorRegistry();
        
        // TODO: define separate preferences for the tokens that use black and gray?
        final RGB black = new RGB(0,0,0);
        final RGB gray = new RGB(128,128,128);
        
        final RGB literalColor = getElementColor(store, PreferenceConstants.EDITOR_LITERAL_COLOR, black);
        final RGB specialFormColor = getElementColor(store, PreferenceConstants.EDITOR_SPECIAL_FORM_COLOR, black);
        final RGB functionColor = getElementColor(store, PreferenceConstants.EDITOR_FUNCTION_COLOR, black);
        final RGB commentColor = getElementColor(store, PreferenceConstants.EDITOR_COMMENT_COLOR, black);
        final RGB globalVarColor = getElementColor(store, PreferenceConstants.EDITOR_GLOBAL_VAR_COLOR, black);
        final RGB keywordColor = getElementColor(store, PreferenceConstants.EDITOR_KEYWORD_COLOR, black);
        final RGB metadataTypehintColor = getElementColor(store, PreferenceConstants.EDITOR_METADATA_TYPEHINT_COLOR, black);
        final RGB macroColor = getElementColor(store, PreferenceConstants.EDITOR_MACRO_COLOR, black);
        
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.STRING, literalColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.NUMBER, literalColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.CHARACTER, literalColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.NIL, literalColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.BOOLEAN, literalColor); //$NON-NLS-1$

        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.OPEN_PAREN, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.CLOSE_PAREN, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SPECIAL_FORM, specialFormColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SYMBOL, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.FUNCTION, functionColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.GLOBAL_VAR, globalVarColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.MACRO, macroColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.SPECIAL_FORM, specialFormColor); //$NON-NLS-1$
        
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.JAVA_CLASS, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.JAVA_STATIC_METHOD, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.JAVA_INSTANCE_METHOD, black); //$NON-NLS-1$
        
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.KEYWORD, keywordColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SYNTAX_QUOTE, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.UNQUOTE_SPLICING, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.UNQUOTE, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.COMMENT, commentColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SPACE, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.LAMBDA_ARG, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.METADATA_TYPEHINT, metadataTypehintColor); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.AMPERSAND, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.LEFT_SQUARE_BRACKET, gray); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.RIGHT_SQUARE_BRACKET, gray); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.LEFT_CURLY_BRACKET, gray); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.RIGHT_CURLY_BRACKET, gray); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.BACKSLASH, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.CIRCUMFLEX, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.COMMERCIAL_AT, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.NUMBER_SIGN, black); //$NON-NLS-1$
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
