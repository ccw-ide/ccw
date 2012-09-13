/*******************************************************************************
 * Copyright (c) 2009 Stephan Muehlstrasser.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Stephan Muehlstrasser - initial implementation, derived from
 * org.eclipse.jdt.internal.ui.preferences.JavaEditorColoringConfigurationBlock
 *    Stephan Muehlstrasser - support for enabling/disabling syntax coloring on
 * per-element basis
 *******************************************************************************/

package ccw.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

import ccw.CCWPlugin;
import ccw.editors.clojure.ClojureColorManager;
import ccw.editors.clojure.ClojurePartitionScanner;
import ccw.editors.clojure.ClojurePartitioner;
import ccw.editors.clojure.ClojureSourceViewer;
import ccw.editors.clojure.ClojureSourceViewerConfiguration;

/**
 * Configures Clojure Editor syntax coloring preferences.
 * Adapted from org.eclipse.jdt.internal.ui.preferences.JavaEditorColoringConfigurationBlock
 */
public class SyntaxColoringPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    /**
     * Item in the highlighting color list.
     */
    private static class HighlightingColorListItem {
        /** Enablement preference key */
        private final String fEnableKey;
        /** Display name */
        private String fDisplayName;
        /** Color preference key */
        private String fColorKey;
        /** Bold preference key */
        private String fBoldKey;
        
        /** Italic preference key */
        private String fItalicKey;
        
        /**
         * Strikethrough preference key.
         *//*
        private String fStrikethroughKey;
        */
        /** Underline preference key.
         *//*
        private String fUnderlineKey;*/
        /**
         * Initialize the item with the given values.
         * @param displayName the display name
         * @param colorKey the color preference key
         * @param boldKey the bold preference key
         * @param italicKey the italic preference key
         * @param strikethroughKey the strikethrough preference key
         * @param underlineKey the underline preference key
         */
        public HighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey, /*String strikethroughKey, String underlineKey*/
                String enableKey) {
            fDisplayName= displayName;
            fColorKey= colorKey;
            fBoldKey= boldKey;
            fItalicKey= italicKey;
            /*fStrikethroughKey= strikethroughKey;
            fUnderlineKey= underlineKey;*/
            fEnableKey = enableKey;
        }
        
        /**
         * @return the bold preference key
         */
        public String getBoldKey() {
            return fBoldKey;
        }
        
        /**
         * @return the bold preference key
         */
        public String getItalicKey() {
            return fItalicKey;
        }
        
        /**
         * @return the strikethrough preference key
         *//*
        public String getStrikethroughKey() {
            return fStrikethroughKey;
        }
        
        *//**
         * @return the underline preference key
         *//*
        public String getUnderlineKey() {
            return fUnderlineKey;
        }
*/        
        /**
         * @return the color preference key
         */
        public String getColorKey() {
            return fColorKey;
        }
        
        /**
         * @return the display name
         */
        public String getDisplayName() {
            return fDisplayName;
        }
        
        /**
         * @return the enablement preference key
         */
        public String getEnableKey() {
            return fEnableKey;
        }
    }

    /**
     * Color list label provider.
     */
    private class ColorListLabelProvider extends LabelProvider {
        /*
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {
            if (element instanceof String)
                return (String) element;
            return ((HighlightingColorListItem)element).getDisplayName();
        }
    }

    /**
     * Color list content provider.
     */
    private class ColorListContentProvider implements IStructuredContentProvider {
    
         /* @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        public Object[] getElements(Object inputElement) {
            return inputElement instanceof List
                ? ((List) inputElement).toArray()
                : null;
        }

        /* @see org.eclipse.jface.viewers.IContentProvider#dispose() */
        public void dispose() {
        }

        /* @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object) */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    /* private static final String COMPILER_TASK_TAGS= JavaCore.COMPILER_TASK_TAGS; */
    
    /**
     * The keys of the overlay store.
     */
    private final String[][] fSyntaxColorListModel= new String[][] {
//          { Messages.SyntaxColoringPreferencePage_literal, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.literalSymbolToken)},
//          { Messages.SyntaxColoringPreferencePage_symbol, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.symbolToken)},

        { Messages.SyntaxColoringPreferencePage_rawSymbol, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.RAW_SYMBOL_Token)},
        { Messages.SyntaxColoringPreferencePage_callableRawSymbol, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callable_RAW_SYMBOL_Token)},

        { Messages.SyntaxColoringPreferencePage_specialForm, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.SPECIAL_FORM_Token)},
        { Messages.SyntaxColoringPreferencePage_callableSpecialForm, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableSPECIAL_FORM_Token)},

        { Messages.SyntaxColoringPreferencePage_macro, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.MACRO_Token)},
        { Messages.SyntaxColoringPreferencePage_callableMacro, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableMACRO_Token)},

        { Messages.SyntaxColoringPreferencePage_function, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.FUNCTION_Token)},
        { Messages.SyntaxColoringPreferencePage_callableFunction, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableFUNCTION_Token)},
        

        { Messages.SyntaxColoringPreferencePage_javaClass, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.JAVA_CLASS_Token)},
        { Messages.SyntaxColoringPreferencePage_callableJavaClass, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableJAVA_CLASS_Token)},
        { Messages.SyntaxColoringPreferencePage_javaInstanceMethod, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.JAVA_INSTANCE_METHOD_Token)},
        { Messages.SyntaxColoringPreferencePage_callableJavaInstanceMethod, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableJAVA_INSTANCE_METHOD_Token)},
        { Messages.SyntaxColoringPreferencePage_javaStaticMethod, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.JAVA_STATIC_METHOD_Token)},
        { Messages.SyntaxColoringPreferencePage_callableJavaStaticMethod, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableJAVA_STATIC_METHOD_Token)},
        
        { Messages.SyntaxColoringPreferencePage_globalVar, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.GLOBAL_VAR_Token)},
        { Messages.SyntaxColoringPreferencePage_callableGlobalVar, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableGLOBAL_VAR_Token)},
        
        { Messages.SyntaxColoringPreferencePage_comment, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.commentToken)},
        { Messages.SyntaxColoringPreferencePage_string, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.stringToken)},
        { Messages.SyntaxColoringPreferencePage_metadataTypehint, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.metaToken)},
        { Messages.SyntaxColoringPreferencePage_readerLiteralTag, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.readerLiteralTag)},
        { Messages.SyntaxColoringPreferencePage_keyword, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.keywordToken)},
        { Messages.SyntaxColoringPreferencePage_regex, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.regexToken)},
        { Messages.SyntaxColoringPreferencePage_int, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.intToken)},
        { Messages.SyntaxColoringPreferencePage_float, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.floatToken)},
        { Messages.SyntaxColoringPreferencePage_char, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.charToken)},
        { Messages.SyntaxColoringPreferencePage_otherLiterals, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.otherLiteralsToken)},
        
                
        { Messages.SyntaxColoringPreferencePage_deactivateRainbowParen, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.deactivatedRainbowParen)},
        { Messages.SyntaxColoringPreferencePage_rainbowParenLevel1, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel1)},
        { Messages.SyntaxColoringPreferencePage_rainbowParenLevel2, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel2)},
        { Messages.SyntaxColoringPreferencePage_rainbowParenLevel3, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel3)},
        { Messages.SyntaxColoringPreferencePage_rainbowParenLevel4, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel4)},
        { Messages.SyntaxColoringPreferencePage_rainbowParenLevel5, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel5)},
        { Messages.SyntaxColoringPreferencePage_rainbowParenLevel6, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel6)},
        { Messages.SyntaxColoringPreferencePage_rainbowParenLevel7, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel7)},
        { Messages.SyntaxColoringPreferencePage_rainbowParenLevel8, PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel8)},
    };
    
    OverlayPreferenceStore fOverlayStore;
    
    private ColorSelector fSyntaxForegroundColorEditor;
    private Label fColorEditorLabel;
    
    private Button fEnableCheckbox;
    
    private Button fBoldCheckBox;
    
    /**
     * Check box for italic preference.
     */
    private Button fItalicCheckBox;
    /**
     * Check box for strikethrough preference.
     */
    // private Button fStrikethroughCheckBox;
    /**
     * Check box for underline preference.
     */
    // private Button fUnderlineCheckBox;
    /**
     * Highlighting color list
     */
    private final List<HighlightingColorListItem> fListModel= new ArrayList<HighlightingColorListItem>();
    
    /**
     * Highlighting color list viewer
     */
    private ListViewer fListViewer;
    /**
     * The previewer.
     */
    private ClojureSourceViewer fPreviewViewer;
    /**
     * The color manager.
     */
    private IColorManager fColorManager;
    /**
     * The font metrics.
     */
    private FontMetrics fFontMetrics;

    public SyntaxColoringPreferencePage() {
        setPreferenceStore(CCWPlugin.getDefault().getPreferenceStore());
        
        fOverlayStore = new OverlayPreferenceStore(getPreferenceStore(), createOverlayStoreKeys());

        fColorManager= new ClojureColorManager(false);
        
        for (String[] modelItem: fSyntaxColorListModel)
            fListModel.add(new HighlightingColorListItem(
                    modelItem[0],
                    modelItem[1], 
                    getBoldPreferenceKey(modelItem[1]),
                    getItalicPreferenceKey(modelItem[1]),
                    /*
                    getStrikethroughPreferenceKey(model[1]),
                    getUnderlinePreferenceKey(model[1]),*/
                    getEnabledPreferenceKey(modelItem[1])));
    }

/*    private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
        
        ArrayList overlayKeys= new ArrayList();
        
        for (int i= 0, n= fListModel.size(); i < n; i++) {
            HighlightingColorListItem item= (HighlightingColorListItem) fListModel.get(i);
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, item.getColorKey()));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getBoldKey()));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getItalicKey()));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getStrikethroughKey()));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getUnderlineKey()));
            
            if (item instanceof SemanticHighlightingColorListItem)
                overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ((SemanticHighlightingColorListItem) item).getEnableKey()));
        }
        
        OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
        overlayKeys.toArray(keys);
        return keys;
    }*/

    /**
     * 
     * 
     * @param parent the parent composite
     * @return the control for the preference page
     */
    public Control createContents(Composite parent) {
        initializeDialogUnits(parent);

        ScrolledPageContent scrolled= new ScrolledPageContent(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);
        
        Control control= createSyntaxPage(scrolled);
        
        scrolled.setContent(control);
        final Point size= control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        scrolled.setMinSize(size.x, size.y);
        
        return scrolled;
    }

    /**
     * Returns the number of pixels corresponding to the width of the given
     * number of characters.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param chars
     *            the number of characters
     * @return the number of pixels
     */
    protected int convertWidthInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fFontMetrics == null)
            return 0;
        return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
    }

    /**
     * Returns the number of pixels corresponding to the height of the given
     * number of characters.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param chars
     *            the number of characters
     * @return the number of pixels
     */
    protected int convertHeightInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fFontMetrics == null)
            return 0;
        return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
    }
    
/*    public void initialize() {
        super.initialize();
        
        fTreeViewer.setInput(fListModel);
        fTreeViewer.setSelection(new StructuredSelection(fJavaCategory));
    }*/

    public void performDefaults() {
        super.performDefaults();
        
        handleSyntaxColorListSelection();

        fOverlayStore.loadDefaults();
        
        fPreviewViewer.invalidateTextPresentation();
    }

    public boolean performOk() {
        fOverlayStore.propagate();
        
        boolean result = true;
        try {
			Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE).node(CCWPlugin.PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			CCWPlugin.logError("Saving Preferences failed", e);
			result = false;
		}
		
        return result;
    }

    /*
     * @see org.eclipse.jdt.internal.ui.preferences.IPreferenceConfigurationBlock#dispose()
     */
    public void dispose() {
        fColorManager.dispose();
        
        if (fOverlayStore != null) {
            fOverlayStore.stop();
            fOverlayStore= null;
        }
        
        super.dispose();
    }

    private void handleSyntaxColorListSelection() {
        HighlightingColorListItem item= getHighlightingColorListItem();
        if (item == null) {
            fEnableCheckbox.setEnabled(false);
            fSyntaxForegroundColorEditor.getButton().setEnabled(false);
            fColorEditorLabel.setEnabled(false);
            fBoldCheckBox.setEnabled(false);
            fItalicCheckBox.setEnabled(false);
            /* TODO uncomment this once text attributes are used
            fStrikethroughCheckBox.setEnabled(false);
            fUnderlineCheckBox.setEnabled(false);
            */
            return;
        }
        RGB rgb= PreferenceConverter.getColor(fOverlayStore, item.getColorKey());
        fSyntaxForegroundColorEditor.setColorValue(rgb);
        fBoldCheckBox.setSelection(fOverlayStore.getBoolean(item.getBoldKey()));
        fItalicCheckBox.setSelection(fOverlayStore.getBoolean(item.getItalicKey()));
/*        fStrikethroughCheckBox.setSelection(fOverlayStore.getBoolean(item.getStrikethroughKey()));
        fUnderlineCheckBox.setSelection(fOverlayStore.getBoolean(item.getUnderlineKey()));
        */
        
        fEnableCheckbox.setEnabled(true);
        boolean enable= fOverlayStore.getBoolean(item.getEnableKey());
        fEnableCheckbox.setSelection(enable);
        fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
        fColorEditorLabel.setEnabled(enable);
        fBoldCheckBox.setEnabled(enable);
        fItalicCheckBox.setEnabled(enable);
        /* TODO depend on enable if text attributes are actually used
        fStrikethroughCheckBox.setEnabled(enable);
        fUnderlineCheckBox.setEnabled(enable);
        */
    }
    
    private Control createSyntaxPage(final Composite parent) {
        fOverlayStore.load();
        fOverlayStore.start();
        
        Composite colorComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        colorComposite.setLayout(layout);

        Link link= new Link(colorComposite, SWT.NONE);
        link.setText(Messages.SyntaxColoringPreferencePage_link);
        link.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        });
        // TODO replace by link-specific tooltips when
        // bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=88866 gets fixed
//      link.setToolTipText(Messages.JavaEditorColoringConfigurationBlock_link_tooltip);
        
        GridData gridData= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gridData.widthHint= 150; // only expand further if anyone else requires it
        gridData.horizontalSpan= 2;
        link.setLayoutData(gridData);

        Label label;
        label= new Label(colorComposite, SWT.LEFT);
        label.setText(Messages.SyntaxColoringPreferencePage_coloring_element);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
        Composite editorComposite= new Composite(colorComposite, SWT.NONE);
        layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        editorComposite.setLayout(layout);
        GridData gd= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        editorComposite.setLayoutData(gd);
    
        fListViewer= new ListViewer(editorComposite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fListViewer.setLabelProvider(new ColorListLabelProvider());
        fListViewer.setContentProvider(new ColorListContentProvider());
        fListViewer.setInput(fListModel);

        gd= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true);
        gd.heightHint= convertHeightInCharsToPixels(30);
        int maxWidth= 0;
        for (Iterator<HighlightingColorListItem> it= fListModel.iterator(); it.hasNext();) {
            HighlightingColorListItem item=  it.next();
            maxWidth= Math.max(maxWidth, convertWidthInCharsToPixels(item.getDisplayName().length()));
        }
        ScrollBar vBar= ((Scrollable) fListViewer.getControl()).getVerticalBar();
        if (vBar != null)
            maxWidth += vBar.getSize().x * 3; // scrollbars and tree indentation guess
        gd.widthHint= maxWidth;
        
        fListViewer.getControl().setLayoutData(gd);
                        
        Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        layout.numColumns= 2;
        stylesComposite.setLayout(layout);
        stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        fEnableCheckbox= new Button(stylesComposite, SWT.CHECK);
        fEnableCheckbox.setText(Messages.SyntaxColoringPreferencePage_enable);
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
        fEnableCheckbox.setLayoutData(gd);
        
        fColorEditorLabel= new Label(stylesComposite, SWT.LEFT);
        fColorEditorLabel.setText(Messages.SyntaxColoringPreferencePage_color);
        gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= 20;
        fColorEditorLabel.setLayoutData(gd);
    
        fSyntaxForegroundColorEditor= new ColorSelector(stylesComposite);
        Button foregroundColorButton= fSyntaxForegroundColorEditor.getButton();
        gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        foregroundColorButton.setLayoutData(gd);
        
        fBoldCheckBox= new Button(stylesComposite, SWT.CHECK);
        fBoldCheckBox.setText(Messages.SyntaxColoringPreferencePage_bold);
        gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= 20;
        gd.horizontalSpan= 2;
        fBoldCheckBox.setLayoutData(gd);
        
        fItalicCheckBox= new Button(stylesComposite, SWT.CHECK);
        fItalicCheckBox.setText(Messages.SyntaxColoringPreferencePage_italic);
        gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= 20;
        gd.horizontalSpan= 2;
        fItalicCheckBox.setLayoutData(gd);
        
        /* TODO enable once text attributes are used
        fStrikethroughCheckBox= new Button(stylesComposite, SWT.CHECK);
        fStrikethroughCheckBox.setText(Messages.SyntaxColoringPreferencePage_strikethrough);
        gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= 20;
        gd.horizontalSpan= 2;
        fStrikethroughCheckBox.setLayoutData(gd);
        
        fUnderlineCheckBox= new Button(stylesComposite, SWT.CHECK);
        fUnderlineCheckBox.setText(Messages.SyntaxColoringPreferencePage_underline);
        gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= 20;
        gd.horizontalSpan= 2;
        fUnderlineCheckBox.setLayoutData(gd);
        */
        
        label= new Label(colorComposite, SWT.LEFT);
        label.setText(Messages.SyntaxColoringPreferencePage_preview);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Control previewer= createPreviewer(colorComposite);
        gd= new GridData(GridData.FILL_BOTH);
        gd.widthHint= convertWidthInCharsToPixels(20);
        gd.heightHint= convertHeightInCharsToPixels(5);
        previewer.setLayoutData(gd);
        
        fListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                handleSyntaxColorListSelection();
            }
        });
        
        foregroundColorButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                // TODO: remove this ? PreferenceConverter.setValue(getPreferenceStore(), item.getColorKey(), fSyntaxForegroundColorEditor.getColorValue());
                PreferenceConverter.setValue(fOverlayStore, item.getColorKey(), fSyntaxForegroundColorEditor.getColorValue());
            }
        });
    
        fBoldCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                fOverlayStore.setValue(item.getBoldKey(), fBoldCheckBox.getSelection());
            }
        });
                
        fItalicCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                fOverlayStore.setValue(item.getItalicKey(), fItalicCheckBox.getSelection());
            }
        });
        /*        
        fStrikethroughCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                fOverlayStore.setValue(item.getStrikethroughKey(), fStrikethroughCheckBox.getSelection());
            }
        });
        fUnderlineCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                fOverlayStore.setValue(item.getUnderlineKey(), fUnderlineCheckBox.getSelection());
            }
        });
        */
                
        fEnableCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                boolean enable= fEnableCheckbox.getSelection();
                fOverlayStore.setValue(item.getEnableKey(), enable);
                fEnableCheckbox.setSelection(enable);
                fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
                fColorEditorLabel.setEnabled(enable);
                fBoldCheckBox.setEnabled(enable);
                fItalicCheckBox.setEnabled(enable);
                /* TODO re-enable once text attributes are used
                fStrikethroughCheckBox.setEnabled(enable);
                fUnderlineCheckBox.setEnabled(enable);
                */
            }
        });
        
        colorComposite.layout(false);
                
        fListViewer.setSelection(new StructuredSelection(fListModel.get(0)));

        return colorComposite;
    }

    /**
     * The source code to display inside the syntax coloring preference dialog.
     */
    private static final String PREVIEW_SOURCE =
        "(ns ccw.syntaxcoloring)\n" //$NON-NLS-1$
        + "\n" //$NON-NLS-1$
        + "; this is a comment\n" //$NON-NLS-1$
        + "\n" //$NON-NLS-1$
        + "(def *global-var* \"the answer is\")\n" //$NON-NLS-1$
        + "\n" //$NON-NLS-1$
        + "(defn function\n" //$NON-NLS-1$
        + "  \"demonstrate Counterclockwise Clojure syntax coloring\"\n" //$NON-NLS-1$
        + "  [#^java.lang.String string]\n" //$NON-NLS-1$
        + "  (let [m {:keyword (String/valueOf 42)}]\n" //$NON-NLS-1$
        + "    (str string \" \" (get m :keyword))))\n";  //$NON-NLS-1$

    private Control createPreviewer(Composite parent) {
        IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore();
        IPreferenceStore store= new ChainedPreferenceStore(new IPreferenceStore[] { fOverlayStore, generalTextStore });
        
        fPreviewViewer= new ClojureSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, store, null) {
			public void setStatusLineErrorMessage(String you_need_a_running_repl) {
				// Do nothing
			}
        };
        
        ClojureSourceViewerConfiguration configuration= new ClojureSourceViewerConfiguration(store, fPreviewViewer);
        fPreviewViewer.configure(configuration);
        fPreviewViewer.initializeViewerColors();
        
        Font font= JFaceResources.getFont(org.eclipse.jdt.ui.PreferenceConstants.EDITOR_TEXT_FONT);
        fPreviewViewer.getTextWidget().setFont(font);
        
        IDocument document= new Document(PREVIEW_SOURCE);
        IDocumentPartitioner partitioner = new ClojurePartitioner(new ClojurePartitionScanner(), 
                ClojurePartitionScanner.CLOJURE_CONTENT_TYPES);

        Map<String, IDocumentPartitioner> m = new HashMap<String, IDocumentPartitioner>();
        m.put(ClojurePartitionScanner.CLOJURE_PARTITIONING, partitioner);
        
        TextUtilities.addDocumentPartitioners(document, m);
        fPreviewViewer.setDocument(document);
        
        return fPreviewViewer.getControl();
    }

    /**
     * Returns the current highlighting color list item.
     * 
     * @return the current highlighting color list item
     */
    private HighlightingColorListItem getHighlightingColorListItem() {
        IStructuredSelection selection= (IStructuredSelection) fListViewer.getSelection();
        Object element= selection.getFirstElement();
        if (element instanceof String)
            return null;
        return (HighlightingColorListItem) element;
    }
    
    /**
     * Initializes the computation of horizontal and vertical dialog units based
     * on the size of current font.
     * <p>
     * This method must be called before any of the dialog unit based conversion
     * methods are called.
     * </p>
     * 
     * @param testControl
     *            a control from which to obtain the current font
     */
    protected void initializeDialogUnits(Control testControl) {
        // Compute and store a font metric
        GC gc = new GC(testControl);
        gc.setFont(JFaceResources.getDialogFont());
        fFontMetrics = gc.getFontMetrics();
        gc.dispose();
    }

    public void init(IWorkbench workbench) {
    }
    
    private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {  
        ArrayList<OverlayPreferenceStore.OverlayKey> overlayKeys= new ArrayList<OverlayPreferenceStore.OverlayKey>();

        for (String[] s: fSyntaxColorListModel) {
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, s[1]));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, getBoldPreferenceKey(s[1])));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, getItalicPreferenceKey(s[1])));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, getEnabledPreferenceKey(s[1])));
        }
        
        OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
        return overlayKeys.toArray(keys);
    }
    
    /**
     * A named preference that controls if the given semantic highlighting has the text attribute strikethrough.
     *
     * @param semanticHighlighting the semantic highlighting
     * @return the strikethrough preference key
     *//*
    public static String getStrikethroughPreferenceKey(SemanticHighlighting semanticHighlighting) {
        return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX;
    }

    *//**
     * A named preference that controls if the given semantic highlighting has the text attribute underline.
     *
     * @param semanticHighlighting the semantic highlighting
     * @return the underline preference key
     *//*
    public static String getUnderlinePreferenceKey(SemanticHighlighting semanticHighlighting) {
        return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX;
    }
*/
    /**
     * A named preference that controls if the given Clojure syntax highlighting is enabled.
     *
     * @param the preference key
     * @return the enabled preference key
     */
    public static String getEnabledPreferenceKey(String preferenceKey) {
    	return preferenceKey + PreferenceConstants.EDITOR_COLORING_ENABLED_SUFFIX;
    }
    
    /**
     * A named preference that controls if the given semantic highlighting has the text attribute bold.
     *
     * @param semanticHighlighting the semantic highlighting
     * @return the bold preference key
     */
    public static String getBoldPreferenceKey(String keyPrefix) {
        return keyPrefix + PreferenceConstants.EDITOR_BOLD_SUFFIX;
    }

    /**
     * A named preference that controls if the given semantic highlighting has the text attribute italic.
     *
     * @param semanticHighlighting the semantic highlighting
     * @return the italic preference key
     */
    public static String getItalicPreferenceKey(String keyPrefix) {
        return keyPrefix + PreferenceConstants.EDITOR_ITALIC_SUFFIX;
    }

}

