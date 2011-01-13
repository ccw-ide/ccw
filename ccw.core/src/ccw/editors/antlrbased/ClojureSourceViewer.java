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

import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
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
import org.eclipse.ui.texteditor.StatusLineContributionItem;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.editors.rulesbased.ClojurePartitionScanner;
import ccw.repl.REPLView;
import ccw.util.DisplayUtil;

public abstract class ClojureSourceViewer extends ProjectionViewer implements
        IClojureEditor, IPropertyChangeListener {
    public static final String STATUS_CATEGORY_STRUCTURAL_EDITION = "CCW.STATUS_CATEGORY_STRUCTURAL_EDITING_POSSIBLE";

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
    
	/**
	 * Set to true if the editor is in "Strict" Structural editing mode
	 */
	private boolean useStrictStructuralEditing;

    /** History for structure select action
	 * STOLEN FROM THE JDT */
	private SelectionHistory fSelectionHistory;
	
	private StatusLineContributionItem structuralEditionStatusField;
	
	/** The error message shown in the status line in case of failed information look up. */
	protected final String fErrorLabel = "An unexpected error occured";


	public SelectionHistory getSelectionHistory() {
		return fSelectionHistory;
	}
    
	/** 
	 * Set to false if structural editing is not possible, because the document
	 * is not parseable.
	 */
	private boolean structuralEditingPossible;

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
        
		useStrictStructuralEditing = store.getBoolean(ccw.preferences.PreferenceConstants.USE_STRICT_STRUCTURAL_EDITING_MODE_BY_DEFAULT);

		structuralEditionStatusField = 
			new StatusLineContributionItem(ClojureSourceViewer.STATUS_CATEGORY_STRUCTURAL_EDITION, true, 33);
		structuralEditionStatusField.setActionHandler(new Action() {
			public void run() {
				toggleStructuralEditionMode();
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

		fSelectionHistory = new SelectionHistory(this);

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

		if (fSelectionHistory != null) {
			fSelectionHistory.dispose();
			fSelectionHistory = null;
		}

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
    	boolean firstTime = (parseRef == null);
        parseRef = EditorSupport.updateParseRef(text, parseRef);
        if (firstTime) {
        	EditorSupport.startWatchParseRef(parseRef, this);
        }
    }
    
    public Object getParsed () {
        if (parseRef == null) {
            updateParseRef(getDocument().get());
        }
        return EditorSupport.getParser(getDocument().get(), parseRef);
    }
    
    private boolean structuralEditionPossible = true;
    public void setStructuralEditionPossible(final boolean state) {
    	structuralEditionPossible = state;
    	syncWithStructuralEditionPossibleState();
    }
    public boolean isStructuralEditionPossible() {
    	return structuralEditionPossible;
    }
    private void syncWithStructuralEditionPossibleState() {
    	DisplayUtil.asyncExec(new Runnable() {
			public void run() {
				getTextWidget().setBackground(
						structuralEditionPossible ? null : Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
				getTextWidget().setToolTipText(structuralEditionPossible ? null : "Unparseable source code. Structural Edition temporarily disabled.");
			}
		});
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

    // TODO rename because it's really "should we be in strict mode or not?" 
    public boolean isStructuralEditingEnabled() {
        return useStrictStructuralEditing;
    }

    public boolean isInEscapeSequence () {
        return inEscapeSequence;
    }
    
    public String findDeclaringNamespace() {
        return ClojureCore.findDeclaringNamespace((Map) getParsed());
    }

    public IJavaProject getAssociatedProject() {
        return null;
    }
    
    public REPLView getCorrespondingREPL () {
        // this gets overridden in REPLView as appropriate so that the toolConnection there gets returned
        return null;
    }
    
    public void updateTabsToSpacesConverter () {}
    
    // TODO get rid of this way of handling document initialization
    @Override
    public void setDocument(IDocument document,
    		IAnnotationModel annotationModel, int modelRangeOffset,
    		int modelRangeLength) {
    	super.setDocument(document, annotationModel, modelRangeOffset, modelRangeLength);
    	if (document != null) {
    		updateParseRef(document.get());
    	}
    }
    
    /** Preference key for matching brackets color */
	//PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;

	public final static char[] PAIRS= { '{', '}', '(', ')', '[', ']' };
	
	private DefaultCharacterPairMatcher pairsMatcher = new DefaultCharacterPairMatcher(PAIRS, ClojurePartitionScanner.CLOJURE_PARTITIONING) {
		/* tries to match a pair be the cursor after or before a pair start/end element */
		@Override
		public IRegion match(IDocument doc, int offset) {
			IRegion region = super.match(doc, offset);
			if (region == null && offset < (doc.getLength()-1)) {
				return super.match(doc, offset + 1);
			} else {
				return region;
			}
		}
	};
    /**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {
		IDocument document= getDocument();
		if (document == null)
			return;

		IRegion selection= getSignedSelection();

		int selectionLength= Math.abs(selection.getLength());
		if (selectionLength > 1) {
			setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracketAction_error_invalidSelection);
			getTextWidget().getDisplay().beep();
			return;
		}

//		// #26314
		int sourceCaretOffset= selection.getOffset() + selection.getLength();
		// From JavaEditor, but I don't understand what it does so I maintain it commented out
//		if (isSurroundedByBrackets(document, sourceCaretOffset))
//			sourceCaretOffset -= selection.getLength();
//
		IRegion region= pairsMatcher.match(document, sourceCaretOffset);
		if (region == null) {
			setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracketAction_error_noMatchingBracket);
			getTextWidget().getDisplay().beep();
			return;
		}

		int offset= region.getOffset();
		int length= region.getLength();

		if (length < 1)
			return;

		int anchor= pairsMatcher.getAnchor();
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
		int targetOffset= (ICharacterPairMatcher.RIGHT == anchor) ? offset + 1: offset + length;

		boolean visible= false;
		if (this instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) this;
			visible= (extension.modelOffset2WidgetOffset(targetOffset) > -1);
		} else {
			IRegion visibleRegion= getVisibleRegion();
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
			visible= (targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
		}

		if (!visible) {
			setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracketAction_error_bracketOutsideSelectedElement);
			getTextWidget().getDisplay().beep();
			return;
		}

		if (selection.getLength() < 0)
			targetOffset -= selection.getLength();

		setSelectedRange(targetOffset, selection.getLength());
		revealRange(targetOffset, selection.getLength());
	}

	public DefaultCharacterPairMatcher getPairsMatcher() {
		return pairsMatcher;
	}
	
	public void setStructuralEditingPossible(boolean state) {
		if (state != this.structuralEditingPossible) {
			this.structuralEditingPossible = state;
			updateStatusField();
		}
	}
	
    public void toggleStructuralEditionMode() {
       useStrictStructuralEditing = !useStrictStructuralEditing;
       updateStatusField();
    }
    
	private void updateStatusField() {
		if (structuralEditionStatusField != null) {
			/*
			 * Disabled, because currently structuralEditingPossible is not reliable (some paredit commands stop after having parsed all the text)
			 * TODO reactivate when paredit has been ported to parsley
			String text= "Structural Edition: " 
				+ (structuralEditingPossible ? "enabled" : "disabled");
				*/
			String text = "Structural Edition: " + (isStructuralEditingEnabled() ? "Strict mode" : "Default mode");
			structuralEditionStatusField.setText(text == null ? fErrorLabel : text);
			structuralEditionStatusField.setToolTipText(
					(isStructuralEditingEnabled() 
							? "Strict mode: editor does its best to prevent you from breaking the structure of the code (requires you to know shortcut commands well). Click to switch to Default Mode."
						   : "Default mode: helps you with edition, but does not get in your way Click to switch to Strict Mode."));
		}
	}

	public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		statusLineManager.add(structuralEditionStatusField);
		updateStatusField();
	}

	public Object getAdapter(Class adapter) {
		if ( IClojureEditor.class == adapter) {
			return this;
		}
		if (ITextOperationTarget.class == adapter) {
			return this;
		}
		return null;
	}
}
