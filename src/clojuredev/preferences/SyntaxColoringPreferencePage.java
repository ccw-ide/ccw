package clojuredev.preferences;

/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andre Soereng <andreis@fast.no> - [syntax highlighting] highlight numbers - https://bugs.eclipse.org/bugs/show_bug.cgi?id=63573
 *******************************************************************************/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Preferences;

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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.jdt.core.JavaCore;

import clojuredev.ClojuredevPlugin;
import clojuredev.editors.antlrbased.ClojureSourceViewer;
import clojuredev.editors.antlrbased.ClojureSourceViewerConfiguration;
import clojuredev.editors.rulesbased.ClojureColorManager;
import clojuredev.editors.rulesbased.ClojurePartitionScanner;
import clojuredev.editors.rulesbased.ClojurePartitioner;
import clojuredev.preferences.PreferenceConstants;

import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightingManager;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightings;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightingManager.HighlightedRange;
import org.eclipse.jdt.internal.ui.text.JavaColorManager;
import org.eclipse.jdt.internal.ui.text.PreferencesAdapter;
import org.eclipse.jdt.internal.ui.text.SimpleJavaSourceViewerConfiguration;
import org.eclipse.jdt.internal.ui.util.PixelConverter;

/**
 * Configures Clojure Editor syntax coloring preferences.
 * from org.eclipse.jdt.internal.ui.preferences.JavaEditorColoringConfigurationBlock
 */
public class SyntaxColoringPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    /**
     * Item in the highlighting color list.
     * 
     * @since 3.0
     */
    private static class HighlightingColorListItem {
        /** Display name */
        private String fDisplayName;
        /** Color preference key */
        private String fColorKey;
        /** Bold preference key *//*
        private String fBoldKey;
        *//** Italic preference key *//*
        private String fItalicKey;
        *//**
         * Strikethrough preference key.
         * @since 3.1
         *//*
        private String fStrikethroughKey;
        *//** Underline preference key.
         * @since 3.1
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
        public HighlightingColorListItem(String displayName, String colorKey/*, String boldKey, String italicKey, String strikethroughKey, String underlineKey*/) {
            fDisplayName= displayName;
            fColorKey= colorKey;
            /*fBoldKey= boldKey;
            fItalicKey= italicKey;
            fStrikethroughKey= strikethroughKey;
            fUnderlineKey= underlineKey;*/
        }
        
/*        *//**
         * @return the bold preference key
         *//*
        public String getBoldKey() {
            return fBoldKey;
        }
        
        *//**
         * @return the bold preference key
         *//*
        public String getItalicKey() {
            return fItalicKey;
        }
        
        *//**
         * @return the strikethrough preference key
         * @since 3.1
         *//*
        public String getStrikethroughKey() {
            return fStrikethroughKey;
        }
        
        *//**
         * @return the underline preference key
         * @since 3.1
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
    }
    
    /*private static class SemanticHighlightingColorListItem extends HighlightingColorListItem {
    
        *//** Enablement preference key *//*
        private final String fEnableKey;
        
        *//**
         * Initialize the item with the given values.
         * @param displayName the display name
         * @param colorKey the color preference key
         * @param boldKey the bold preference key
         * @param italicKey the italic preference key
         * @param strikethroughKey the strikethroughKey preference key
         * @param underlineKey the underlineKey preference key
         * @param enableKey the enable preference key
         *//*
        public SemanticHighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey, String enableKey) {
            super(displayName, colorKey, boldKey, italicKey, strikethroughKey, underlineKey);
            fEnableKey= enableKey;
        }
    
        *//**
         * @return the enablement preference key
         *//*
        public String getEnableKey() {
            return fEnableKey;
        }
    }*/

    /**
     * Color list label provider.
     * 
     * @since 3.0
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
     * 
     * @since 3.0
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

/*    private static final String BOLD= PreferenceConstants.EDITOR_BOLD_SUFFIX;
    *//**
     * Preference key suffix for italic preferences.
     * @since  3.0
     *//*
    private static final String ITALIC= PreferenceConstants.EDITOR_ITALIC_SUFFIX;
    *//**
     * Preference key suffix for strikethrough preferences.
     * @since  3.1
     *//*
    private static final String STRIKETHROUGH= PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX;
    *//**
     * Preference key suffix for underline preferences.
     * @since  3.1
     *//*
    private static final String UNDERLINE= PreferenceConstants.EDITOR_UNDERLINE_SUFFIX;
*/    
    private static final String COMPILER_TASK_TAGS= JavaCore.COMPILER_TASK_TAGS;
    /**
     * The keys of the overlay store.
     */
    private final String[][] fSyntaxColorListModel= new String[][] {
        { Messages.SyntaxColoringPreferencePage_function, PreferenceConstants.EDITOR_FUNCTION_COLOR },
        { Messages.SyntaxColoringPreferencePage_literal, PreferenceConstants.EDITOR_LITERAL_COLOR },
        { Messages.SyntaxColoringPreferencePage_specialForm, PreferenceConstants.EDITOR_SPECIAL_FORM_COLOR },
        { Messages.SyntaxColoringPreferencePage_comment, PreferenceConstants.EDITOR_COMMENT_COLOR },
        { Messages.SyntaxColoringPreferencePage_globalVar, PreferenceConstants.EDITOR_GLOBAL_VAR_COLOR },
        { Messages.SyntaxColoringPreferencePage_keyword, PreferenceConstants.EDITOR_KEYWORD_COLOR },
        { Messages.SyntaxColoringPreferencePage_metadataTypehint, PreferenceConstants.EDITOR_METADATA_TYPEHINT_COLOR },
    };
    
/*    private final String fJavaCategory= Messages.SyntaxColoringPreferencePage_coloring_category_java;
    private final String fJavadocCategory= Messages.SyntaxColoringPreferencePage_coloring_category_javadoc;
    private final String fCommentsCategory= Messages.SyntaxColoringPreferencePage_coloring_category_comments;
*/    
    private ColorSelector fSyntaxForegroundColorEditor;
    private Label fColorEditorLabel;
    private Button fBoldCheckBox;
    private Button fEnableCheckbox;
    /**
     * Check box for italic preference.
     * @since  3.0
     */
    private Button fItalicCheckBox;
    /**
     * Check box for strikethrough preference.
     * @since  3.1
     */
    private Button fStrikethroughCheckBox;
    /**
     * Check box for underline preference.
     * @since  3.1
     */
    private Button fUnderlineCheckBox;
    /**
     * Highlighting color list
     * @since  3.0
     */
    private final List<HighlightingColorListItem> fListModel= new ArrayList<HighlightingColorListItem>();
    
    /**
     * Highlighting color list viewer
     */
    private ListViewer fListViewer;
    /**
     * Semantic highlighting manager
     * @since  3.0
     */
    // private SemanticHighlightingManager fSemanticHighlightingManager;
    /**
     * The previewer.
     * @since 3.0
     */
    private ClojureSourceViewer fPreviewViewer;
    /**
     * The color manager.
     * @since 3.1
     */
    private IColorManager fColorManager;
    /**
     * The font metrics.
     * @since 3.1
     */
    private FontMetrics fFontMetrics;

    public SyntaxColoringPreferencePage(/*OverlayPreferenceStore store*/) {
        /*super(store);*/
        
        setPreferenceStore(ClojuredevPlugin.getDefault().getPreferenceStore());
        
        fColorManager= new ClojureColorManager(false);
        
        for (int i= 0, n= fSyntaxColorListModel.length; i < n; i++)
            fListModel.add(new HighlightingColorListItem(
                    fSyntaxColorListModel[i][0],
                    fSyntaxColorListModel[i][1] /*,
                    fSyntaxColorListModel[i][1] + BOLD,
                    fSyntaxColorListModel[i][1] + ITALIC,
                    fSyntaxColorListModel[i][1] + STRIKETHROUGH,
                    fSyntaxColorListModel[i][1] + UNDERLINE*/));
        
        /* SemanticHighlighting[] semanticHighlightings= SemanticHighlightings.getSemanticHighlightings();
        for (int i= 0, n= semanticHighlightings.length; i < n; i++)
            fListModel.add(
                    new SemanticHighlightingColorListItem(
                            semanticHighlightings[i].getDisplayName(),
                            SemanticHighlightings.getColorPreferenceKey(semanticHighlightings[i]),
                            SemanticHighlightings.getBoldPreferenceKey(semanticHighlightings[i]),
                            SemanticHighlightings.getItalicPreferenceKey(semanticHighlightings[i]),
                            SemanticHighlightings.getStrikethroughPreferenceKey(semanticHighlightings[i]),
                            SemanticHighlightings.getUnderlinePreferenceKey(semanticHighlightings[i]),
                            SemanticHighlightings.getEnabledPreferenceKey(semanticHighlightings[i])
                    ));
        
        store.addKeys(createOverlayStoreKeys());*/
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

/*        uninstallSemanticHighlighting();
        installSemanticHighlighting();*/

        fPreviewViewer.invalidateTextPresentation();
    }

    /*
     * @see org.eclipse.jdt.internal.ui.preferences.IPreferenceConfigurationBlock#dispose()
     */
    public void dispose() {
        /*uninstallSemanticHighlighting();*/
        fColorManager.dispose();
        
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
            fStrikethroughCheckBox.setEnabled(false);
            fUnderlineCheckBox.setEnabled(false);
            return;
        }
        RGB rgb= PreferenceConverter.getColor(getPreferenceStore(), item.getColorKey());
        fSyntaxForegroundColorEditor.setColorValue(rgb);
/*        fBoldCheckBox.setSelection(getPreferenceStore().getBoolean(item.getBoldKey()));
        fItalicCheckBox.setSelection(getPreferenceStore().getBoolean(item.getItalicKey()));
        fStrikethroughCheckBox.setSelection(getPreferenceStore().getBoolean(item.getStrikethroughKey()));
        fUnderlineCheckBox.setSelection(getPreferenceStore().getBoolean(item.getUnderlineKey()));
        if (item instanceof SemanticHighlightingColorListItem) {
            fEnableCheckbox.setEnabled(true);
            boolean enable= getPreferenceStore().getBoolean(((SemanticHighlightingColorListItem) item).getEnableKey());
            fEnableCheckbox.setSelection(enable);
            fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
            fColorEditorLabel.setEnabled(enable);
            fBoldCheckBox.setEnabled(enable);
            fItalicCheckBox.setEnabled(enable);
            fStrikethroughCheckBox.setEnabled(enable);
            fUnderlineCheckBox.setEnabled(enable);
        } else {*/
            fSyntaxForegroundColorEditor.getButton().setEnabled(true);
            fColorEditorLabel.setEnabled(true);
            fBoldCheckBox.setEnabled(true);
            fItalicCheckBox.setEnabled(true);
            fStrikethroughCheckBox.setEnabled(true);
            fUnderlineCheckBox.setEnabled(true);
            fEnableCheckbox.setEnabled(false);
            fEnableCheckbox.setSelection(true);
       /* }*/
    }
    
    private Control createSyntaxPage(final Composite parent) {
        
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

        addFiller(colorComposite, 1);
        
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
        /* fListViewer.setComparator(new ViewerComparator() {
            public int category(Object element) {
                // don't sort the top level categories
                if (fJavaCategory.equals(element))
                    return 0;
                if (fJavadocCategory.equals(element))
                    return 1;
                if (fCommentsCategory.equals(element))
                    return 2;
                // to sort semantic settings after partition based ones:
//              if (element instanceof SemanticHighlightingColorListItem)
//                  return 1;
                return 0;
            }
        });*/
        gd= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true);
        gd.heightHint= convertHeightInCharsToPixels(9);
        int maxWidth= 0;
        for (Iterator it= fListModel.iterator(); it.hasNext();) {
            HighlightingColorListItem item= (HighlightingColorListItem) it.next();
            maxWidth= Math.max(maxWidth, convertWidthInCharsToPixels(item.getDisplayName().length()));
        }
        ScrollBar vBar= ((Scrollable) fListViewer.getControl()).getVerticalBar();
        if (vBar != null)
            maxWidth += vBar.getSize().x * 3; // scrollbars and tree indentation guess
        gd.widthHint= maxWidth;
        
        fListViewer.getControl().setLayoutData(gd);
        /* installDoubleClickListener(); */
                        
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
                PreferenceConverter.setValue(getPreferenceStore(), item.getColorKey(), fSyntaxForegroundColorEditor.getColorValue());
            }
        });
    
/*        fBoldCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                getPreferenceStore().setValue(item.getBoldKey(), fBoldCheckBox.getSelection());
            }
        });
                
        fItalicCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                getPreferenceStore().setValue(item.getItalicKey(), fItalicCheckBox.getSelection());
            }
        });
        fStrikethroughCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                getPreferenceStore().setValue(item.getStrikethroughKey(), fStrikethroughCheckBox.getSelection());
            }
        });
        
        fUnderlineCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                getPreferenceStore().setValue(item.getUnderlineKey(), fUnderlineCheckBox.getSelection());
            }
        });
                
        fEnableCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                if (item instanceof SemanticHighlightingColorListItem) {
                    boolean enable= fEnableCheckbox.getSelection();
                    getPreferenceStore().setValue(((SemanticHighlightingColorListItem) item).getEnableKey(), enable);
                    fEnableCheckbox.setSelection(enable);
                    fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
                    fColorEditorLabel.setEnabled(enable);
                    fBoldCheckBox.setEnabled(enable);
                    fItalicCheckBox.setEnabled(enable);
                    fStrikethroughCheckBox.setEnabled(enable);
                    fUnderlineCheckBox.setEnabled(enable);
                    uninstallSemanticHighlighting();
                    installSemanticHighlighting();
                }
            }
        });
*/        
        colorComposite.layout(false);
                
        return colorComposite;
    }
    
    /**
     * Installs a double-click listener which allows
     * to expand and collapse tree items.
     * 
     * @since 3.4
     */
    /*  private void installDoubleClickListener() {
       fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            
             * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
             
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection s= (IStructuredSelection) event.getSelection();
                Object element= s.getFirstElement();
                if (fTreeViewer.isExpandable(element))
                    fTreeViewer.setExpandedState(element, !fTreeViewer.getExpandedState(element));
            }
        });
    }*/

    private void addFiller(Composite composite, int horizontalSpan) {
        PixelConverter pixelConverter= new PixelConverter(composite);
        Label filler= new Label(composite, SWT.LEFT );
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan= horizontalSpan;
        gd.heightHint= pixelConverter.convertHeightInCharsToPixels(1) / 2;
        filler.setLayoutData(gd);
    }

    private static final String PREVIEW_SOURCE =
        "; this is a comment\n" //$NON-NLS-1$
        + "(defmacro and\n" //$NON-NLS-1$
        + "  \"Evaluates exprs one at a time, from left to right. If a form\n" //$NON-NLS-1$
        + "  returns logical false (nil or false), and returns that value and\n" //$NON-NLS-1$
        + "  doesn't evaluate any of the other expressions, otherwise it returns\n" //$NON-NLS-1$
        + "  the value of the last expr. (and) returns true.\"\n" //$NON-NLS-1$
        + "  ([] true)\n" //$NON-NLS-1$
        + "  ([x] x)\n" //$NON-NLS-1$
        + "  ([x & next]\n" //$NON-NLS-1$
        + "  `(let [and# ~x]\n" //$NON-NLS-1$
        + "     (if and# (and ~@next) and#))))\n"; //$NON-NLS-1$
    
    private Control createPreviewer(Composite parent) {
        
        IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore();
        /* TODO: what is the PreferencesAdapter good for
        IPreferenceStore store= new ChainedPreferenceStore(new IPreferenceStore[] { getPreferenceStore(), new PreferencesAdapter(createTemporaryCorePreferenceStore()), generalTextStore });
        */
        IPreferenceStore store= new ChainedPreferenceStore(new IPreferenceStore[] { getPreferenceStore(), generalTextStore });

        fPreviewViewer= new ClojureSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, store);
        ClojureSourceViewerConfiguration configuration= new ClojureSourceViewerConfiguration(store, null);
        fPreviewViewer.configure(configuration);
        Font font= JFaceResources.getFont(org.eclipse.jdt.ui.PreferenceConstants.EDITOR_TEXT_FONT);
        fPreviewViewer.getTextWidget().setFont(font);
        new ClojureSourcePreviewerUpdater(fPreviewViewer, configuration, store);
        fPreviewViewer.setEditable(false);
        
        IDocument document= new Document(PREVIEW_SOURCE);
        IDocumentPartitioner partitioner = new ClojurePartitioner(new ClojurePartitionScanner(), 
                ClojurePartitionScanner.CLOJURE_CONTENT_TYPES);

        Map<String, IDocumentPartitioner> m = new HashMap<String, IDocumentPartitioner>();
        m.put(ClojurePartitionScanner.CLOJURE_PARTITIONING, partitioner);
        
        TextUtilities.addDocumentPartitioners(document, m);
        fPreviewViewer.setDocument(document);
    
        // installSemanticHighlighting();
        
        return fPreviewViewer.getControl();
    }


/* TODO what is this needed for?
 *     private Preferences createTemporaryCorePreferenceStore() {
        Preferences result= new Preferences();
        
        result.setValue(COMPILER_TASK_TAGS, "TASK,TODO"); //$NON-NLS-1$
        
        return result;
    }*/


    private String loadPreviewContentFromFile(String filename) {
        String line;
        String separator= System.getProperty("line.separator"); //$NON-NLS-1$
        StringBuffer buffer= new StringBuffer(512);
        BufferedReader reader= null;
        try {
            reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
            while ((line= reader.readLine()) != null) {
                buffer.append(line);
                buffer.append(separator);
            }
        } catch (IOException io) {
            JavaPlugin.log(io);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) {}
            }
        }
        return buffer.toString();
    }


/*    *//**
     * Install Semantic Highlighting on the previewer
     * 
     * @since 3.0
     *//*
    private void installSemanticHighlighting() {
        if (fSemanticHighlightingManager == null) {
            fSemanticHighlightingManager= new SemanticHighlightingManager();
            fSemanticHighlightingManager.install(fPreviewViewer, fColorManager, getPreferenceStore(), createPreviewerRanges());
        }
    }


    *//**
     * Uninstall Semantic Highlighting from the previewer
     * 
     * @since 3.0
     *//*
    private void uninstallSemanticHighlighting() {
        if (fSemanticHighlightingManager != null) {
            fSemanticHighlightingManager.uninstall();
            fSemanticHighlightingManager= null;
        }
    }
*/

    /**
     * Create the hard coded previewer ranges
     * 
     * @return the hard coded previewer ranges
     * @since 3.0
     */
/*    private SemanticHighlightingManager.HighlightedRange[][] createPreviewerRanges() {
        return new SemanticHighlightingManager.HighlightedRange[][] {
            { createHighlightedRange( 6, 13,  9, SemanticHighlightings.DEPRECATED_MEMBER), createHighlightedRange( 6, 13,  9, SemanticHighlightings.CLASS),  },
            { createHighlightedRange( 6, 23,  1, SemanticHighlightings.TYPE_VARIABLE), createHighlightedRange( 6, 23,  1, SemanticHighlightings.TYPE_ARGUMENT), },
            { createHighlightedRange( 6, 37,  9, SemanticHighlightings.INTERFACE) },
            { createHighlightedRange( 6, 47,  6, SemanticHighlightings.TYPE_ARGUMENT), createHighlightedRange( 6, 47,  6, SemanticHighlightings.CLASS) },
            { createHighlightedRange( 7,  6,  5, SemanticHighlightings.ENUM), },
            { createHighlightedRange( 7, 14, 3, SemanticHighlightings.STATIC_FINAL_FIELD), createHighlightedRange(7, 14, 3, SemanticHighlightings.STATIC_FIELD), createHighlightedRange(7, 14, 3, SemanticHighlightings.FIELD) },
            { createHighlightedRange( 7, 19, 5, SemanticHighlightings.STATIC_FINAL_FIELD), createHighlightedRange(7, 19, 5, SemanticHighlightings.STATIC_FIELD), createHighlightedRange(7, 19, 5, SemanticHighlightings.FIELD) },
            { createHighlightedRange( 7, 26, 4, SemanticHighlightings.STATIC_FINAL_FIELD), createHighlightedRange(7, 26, 4, SemanticHighlightings.STATIC_FIELD), createHighlightedRange(7, 26, 4, SemanticHighlightings.FIELD) },
            { createHighlightedRange( 9, 8, 6, SemanticHighlightings.CLASS), },
            { createHighlightedRange( 9, 15, 11, SemanticHighlightings.STATIC_FIELD), createHighlightedRange(9, 15, 11, SemanticHighlightings.FIELD) },
            { createHighlightedRange(11,  9,  1, SemanticHighlightings.TYPE_VARIABLE) },
            { createHighlightedRange(11, 11,  5, SemanticHighlightings.FIELD) },
            { createHighlightedRange(13, 2,  16, SemanticHighlightings.ANNOTATION) },
            { createHighlightedRange(13, 19,  5, SemanticHighlightings.ANNOTATION_ELEMENT_REFERENCE) },
            { createHighlightedRange(14, 12,  3, SemanticHighlightings.METHOD_DECLARATION), createHighlightedRange(14, 12,  3, SemanticHighlightings.METHOD) },
            { createHighlightedRange(14, 24,  9, SemanticHighlightings.PARAMETER_VARIABLE) },
            { createHighlightedRange(15,  2, 14, SemanticHighlightings.ABSTRACT_METHOD_INVOCATION), createHighlightedRange(15,  2, 14, SemanticHighlightings.METHOD) },
            { createHighlightedRange(16,  6,  5, SemanticHighlightings.LOCAL_VARIABLE_DECLARATION) },
            { createHighlightedRange(16, 13, 2, SemanticHighlightings.NUMBER) },
            { createHighlightedRange(16, 16,  8, SemanticHighlightings.INHERITED_METHOD_INVOCATION), createHighlightedRange(16, 16,  8, SemanticHighlightings.METHOD) },
            { createHighlightedRange(17,  2, 12, SemanticHighlightings.STATIC_METHOD_INVOCATION), createHighlightedRange(17,  2, 12, SemanticHighlightings.METHOD) },
            { createHighlightedRange(18, 9,  3, SemanticHighlightings.METHOD) },
            { createHighlightedRange(18, 13,  5, SemanticHighlightings.LOCAL_VARIABLE) },
            { createHighlightedRange(18, 22,  9, SemanticHighlightings.AUTOBOXING) },
        };
    }
*/

    /**
     * Create a highlighted range on the previewers document with the given line, column, length and key.
     * 
     * @param line the line
     * @param column the column
     * @param length the length
     * @param key the key
     * @return the highlighted range
     * @since 3.0
     */
/*    private HighlightedRange createHighlightedRange(int line, int column, int length, String key) {
        try {
            IDocument document= fPreviewViewer.getDocument();
            int offset= document.getLineOffset(line) + column;
            return new HighlightedRange(offset, length, key);
        } catch (BadLocationException x) {
            JavaPlugin.log(x);
        }
        return null;
    }*/


    /**
     * Returns the current highlighting color list item.
     * 
     * @return the current highlighting color list item
     * @since 3.0
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
        // TODO Auto-generated method stub
        
    }
}
