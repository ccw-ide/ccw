/*******************************************************************************
 * Copyright (c) 2009 Stephan Muehlstrasser.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Stephan Muehlstrasser - initial implementation
 *******************************************************************************/

package ccw.editors.antlrbased;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.repl.REPLView;

public class ClojureSourceViewer extends ProjectionViewer implements
        IClojureEditor, IPropertyChangeListener {
    /**
     * The preference store.
     */
    private IPreferenceStore fPreferenceStore;
    
	/**
	 * This viewer's foreground color.
	 */
	private Color fForegroundColor;
	
	/**
	 * The viewer's background color.
	 */
	private Color fBackgroundColor;
	
	/**
	 * This viewer's selection foreground color.
	 */
	private Color fSelectionForegroundColor;
	
	/**
	 * The viewer's selection background color.
	 */
	private Color fSelectionBackgroundColor;

    /**
     * The source viewer configuration. Needed for property change events
     * for reconfiguring.
     */
    private ClojureSourceViewerConfiguration fConfiguration;
    
    /**
     * Is this source viewer configured?
     */
    private boolean fIsConfigured;

    private boolean inEscapeSequence;
    
    public ClojureSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
        setPreferenceStore(store);
        
        getTextWidget().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.ESC) {
                    inEscapeSequence = true;
                    updateTabsToSpacesConverter();
                }
            }

            public void keyReleased(KeyEvent e) {
                if (inEscapeSequence && !(e.character == SWT.ESC)) {
                    inEscapeSequence = false;
                    updateTabsToSpacesConverter();
                }
            }
        });
        
        addTextInputListener(new ITextInputListener() {
            public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
                if (newInput != null) {
                    newInput.addDocumentListener(parseTreeConstructorDocumentListener);
                    updateParseRef(newInput.get());
                }
            }
            
            public void inputDocumentAboutToBeChanged(IDocument oldInput,
                    IDocument newInput) {
                if (oldInput != null)
                    oldInput.removeDocumentListener(parseTreeConstructorDocumentListener);
            }
        });
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (fConfiguration != null) {
            ClojureSourceViewerConfiguration tmp = fConfiguration;
            unconfigure();
            initializeViewerColors();
            tmp.initTokenScanner();
            configure(tmp);
        }
    }

    /**
     * Sets the preference store on this viewer.
     *
     * @param store the preference store
     *
     * @since 3.0
     */
    public void setPreferenceStore(IPreferenceStore store) {
        if (fIsConfigured && fPreferenceStore != null)
            fPreferenceStore.removePropertyChangeListener(this);

        fPreferenceStore= store;

        if (fIsConfigured && fPreferenceStore != null) {
            fPreferenceStore.addPropertyChangeListener(this);
            initializeViewerColors();
        }
    }

    public void configure(SourceViewerConfiguration configuration) {
        super.configure(configuration);

        if (fPreferenceStore != null) {
            fPreferenceStore.addPropertyChangeListener(this);
            initializeViewerColors();
        }
        
        if (configuration instanceof ClojureSourceViewerConfiguration)
            fConfiguration = (ClojureSourceViewerConfiguration) configuration;

        fIsConfigured= true;
    }
    
    /**
     * Creates a color from the information stored in the given preference store.
     * Returns <code>null</code> if there is no such information available.
     *
     * @param store the store to read from
     * @param key the key used for the lookup in the preference store
     * @param display the display used create the color
     * @return the created color according to the specification in the preference store
     */
    static public Color createColor(IPreferenceStore store, String key, Display display) {
        RGB rgb = null;

        if (store.contains(key)) {

            if (store.isDefault(key))
                rgb = PreferenceConverter.getDefaultColor(store, key);
            else
                rgb = PreferenceConverter.getColor(store, key);

            if (rgb != null)
                return new Color(display, rgb);
        }

        return null;
    }
    
	public void initializeViewerColors() {
		if (fPreferenceStore != null) {
			StyledText styledText= getTextWidget();

			// ----------- foreground color --------------------
			Color color= fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
			? null
			: createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, styledText.getDisplay());
			styledText.setForeground(color);

			if (fForegroundColor != null)
				fForegroundColor.dispose();

			fForegroundColor= color;

			// ---------- background color ----------------------
			color= fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
			? null
			: createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
			styledText.setBackground(color);

			if (fBackgroundColor != null)
				fBackgroundColor.dispose();

			fBackgroundColor= color;

			// ----------- selection foreground color --------------------
			color= fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR)
				? null
				: createColor(fPreferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, styledText.getDisplay());
			styledText.setSelectionForeground(color);

			if (fSelectionForegroundColor != null)
				fSelectionForegroundColor.dispose();

			fSelectionForegroundColor= color;

			// ---------- selection background color ----------------------
			color= fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR)
				? null
				: createColor(fPreferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, styledText.getDisplay());
			styledText.setSelectionBackground(color);

			if (fSelectionBackgroundColor != null)
				fSelectionBackgroundColor.dispose();

			fSelectionBackgroundColor= color;
			
            CCWPlugin.registerEditorColors(fPreferenceStore, styledText.getForeground().getRGB());
		}
    }

    /*
     * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
     * @since 3.0
     */
    public void unconfigure() {
		if (fForegroundColor != null) {
			fForegroundColor.dispose();
			fForegroundColor= null;
		}
		
		if (fBackgroundColor != null) {
			fBackgroundColor.dispose();
			fBackgroundColor= null;
		}
		
		if (fSelectionForegroundColor != null) {
			fSelectionForegroundColor.dispose();
			fSelectionForegroundColor= null;
		}
		
		if (fSelectionBackgroundColor != null) {
			fSelectionBackgroundColor.dispose();
			fSelectionBackgroundColor= null;
		}
		
        if (fPreferenceStore != null)
            fPreferenceStore.removePropertyChangeListener(this);

        super.unconfigure();
        fIsConfigured= false;
        fConfiguration = null;
    }

    /** This is manipulated by clojure functions.
     * It's a ref, holding a map {:text "the raw text file" :parser parser}
     * where state is a future holding the parser's state
     */
    private Object parseRef; 

    private IDocumentListener parseTreeConstructorDocumentListener = new IDocumentListener() {
        public void documentAboutToBeChanged(DocumentEvent event) { }
        public void documentChanged(DocumentEvent event) {
            updateParseRef(event.getDocument().get());
        }
    };
    
    private void updateParseRef (String text) {
        parseRef = EditorSupport.updateParseRef(text, parseRef);
    }
    
    public Object getParsed () {
        if (parseRef == null) {
            updateParseRef(getDocument().get());
        }
        return EditorSupport.getParser(getDocument().get(), parseRef);
    }
    
    public IRegion getSignedSelection () {
        StyledText text = getTextWidget();
        Point selection = text.getSelectionRange();

        if (text.getCaretOffset() == selection.x) {
            selection.x = selection.x + selection.y;
            selection.y = -selection.y;
        }

        selection.x = widgetOffset2ModelOffset(selection.x);

        return new Region(selection.x, selection.y);
    }
    
    public IRegion getUnSignedSelection () {
        StyledText text = getTextWidget();
        Point selection = text.getSelectionRange();

        selection.x = widgetOffset2ModelOffset(selection.x);

        return new Region(selection.x, selection.y);
    }

    public void selectAndReveal(int start, int length) {
        setSelection(new TextSelection(start, length), true);
    }

    public boolean isStructuralEditingEnabled() {
        return true;
    }

    public boolean isInEscapeSequence () {
        return inEscapeSequence;
    }
    
    public String getDeclaringNamespace() {
        return ClojureCore.getDeclaringNamespace(getDocument().get());
    }

    public IJavaProject getAssociatedProject() {
        return null;
    }
    
    public REPLView getCorrespondingREPL () {
        // this gets overridden in REPLView as appropriate so that the toolConnection there gets returned
        return null;
    }
    
    public void setStructuralEditingPossible (boolean possible) {}
    
    public void updateTabsToSpacesConverter () {}
}
