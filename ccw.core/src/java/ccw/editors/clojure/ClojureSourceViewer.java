/*******************************************************************************
 * Copyright (c) 2009 Stephan Muehlstrasser.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Stephan Muehlstrasser - initial implementation
 *    Andrea Richiardi - refactoring of initialization and cleaning
 *******************************************************************************/

package ccw.editors.clojure;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.core.IPropertyPublisher;
import ccw.editors.clojure.scanners.ClojurePartitionScanner;
import ccw.preferences.PreferenceConstants;
import ccw.repl.IReplProvider;
import ccw.util.ClojureInvoker;
import ccw.util.DisplayUtil;

/**
 * The Counterclockwise SourceViewer.<br/>
 * <br/>
 * <i>Note: the IReplProvider is only temporarily part of the contract of this class, as it does not make sense to have a SourceViewer as Repl provider.
 * It is necessary at the moment because the SourceViewer is passed to ITextHover (and extensions) in input and in turn ITextHover implementations needs a Repl.
 * This will be reworked soon in some better way.</i>
 */
public abstract class ClojureSourceViewer extends ProjectionViewer implements IClojureEditor, IReplProvider, IPropertyPublisher {

	private final ClojureInvoker editorSupport = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.editor-support");
    
	private final ClojureInvoker handlers = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.handlers");

    /** 
     * Status category used e.g. with TextEditors embedding a ClojureSourceViewer
     * reporting the status of the structural edition mode (Strict/Default).
     *  */
    public static final String STATUS_CATEGORY_STRUCTURAL_EDITION = "CCW.STATUS_CATEGORY_STRUCTURAL_EDITING_POSSIBLE";
    
    /**
     * Due to Eclipse idiosyncracies, it is not possible for the viewer to 
     * directly manage lifecycle of StatusLineContributionItems.
     * 
     *  But it is still required, to stay DRY, to centralise as much as possible
     *  of the state reporting of the ClojureSourceViewer.
     *  
     *  This interface must be implemented by "components" (Editors, Viewers, whatever)
     *  which are capable of reporting status to status line managers
     */
    public static interface IStatusLineHandler {
    	StatusLineContributionItem getEditingModeStatusContributionItem();
    }

    /**
     * The preference store.
     */
    private IPreferenceStore fPreferenceStore;
    
    public static class EditorColors {
    	/**
    	 * This viewer's foreground color.
    	 */
    	public Color fForegroundColor;
    	
    	/**
    	 * The viewer's background color.
    	 */
    	public Color fBackgroundColor;
    	
    	/**
    	 * This viewer's selection foreground color.
    	 */
    	public Color fSelectionForegroundColor;
    	
    	/**
    	 * The viewer's selection background color.
    	 */
    	public Color fSelectionBackgroundColor;

    	/**
    	 * The viewer's background color for the selected line
    	 */
		public Color fCurrentLineBackgroundColor;
    }
    
    private EditorColors editorColors = new EditorColors();

    /**
     * The source viewer configuration instance, only needed for property change events.
     */
    private IPropertyChangeListener fConfiguration;

    private boolean inEscapeSequence;
    
    private boolean isContentAssistantActive;
    
	/**
	 * Set to true if the editor is in "Strict" Structural editing mode
	 */
	private boolean useStrictStructuralEditing;
	
	/**
	 * Set to true to have the viewer show rainbow parens
	 */
	private boolean isShowRainbowParens;

    /**
     * Set to true to indicate a Damager to consider that the whole document
     * must be considered damaged, e.g. to force syntax coloring & al.
     * to refresh.
     */
    private boolean isForceRepair;

    /** History for structure select action
	 * STOLEN FROM THE JDT */
	private SelectionHistory fSelectionHistory;
	
	/** can be null */
	private IStatusLineHandler statusLineHandler;
	
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
	
    public ClojureSourceViewer(@NonNull Composite parent, @Nullable IVerticalRuler verticalRuler, @Nullable IOverviewRuler overviewRuler,
            boolean showAnnotationsOverview, int styles, @NonNull IPreferenceStore store, @Nullable IStatusLineHandler statusLineHandler) {
        
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
        
        fPreferenceStore = store;

        KeyListener escListener = new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.ESC) {
                	if (!isContentAssistantActive) {
                		inEscapeSequence = true;
                		updateTabsToSpacesConverter();
                		updateStructuralEditingModeStatusField();
                	}
                }
            }

            public void keyReleased(KeyEvent e) {
                if (inEscapeSequence && !(e.character == SWT.ESC)) {
                    inEscapeSequence = false;
                    updateTabsToSpacesConverter();
                    updateStructuralEditingModeStatusField();
                }
            }
        };
        
        // add before all other listeners so that we're certain we enable/disable 
        //the Esc key feature based on accurate state information
        addKeyListenerFirst(getTextWidget(), escListener);
        
        addTextInputListener(new ITextInputListener() {
            public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
                if (newInput != null) {
                    newInput.addDocumentListener(parseTreeConstructorDocumentListener);
                    String text = newInput.get();
                    updateTextBuffer(text, 0, -1, text);
                }
            }
            
            public void inputDocumentAboutToBeChanged(IDocument oldInput,
                    IDocument newInput) {
                if (oldInput != null)
                    oldInput.removeDocumentListener(parseTreeConstructorDocumentListener);
            }
        });
        
		useStrictStructuralEditing = store.getBoolean(ccw.preferences.PreferenceConstants.USE_STRICT_STRUCTURAL_EDITING_MODE_BY_DEFAULT);
		isShowRainbowParens = store.getBoolean(ccw.preferences.PreferenceConstants.SHOW_RAINBOW_PARENS_BY_DEFAULT);
		this.statusLineHandler = statusLineHandler;
	}
    
    private void addKeyListenerFirst(Control control, KeyListener listener) {
    	Listener[] keyDownListeners = control.getListeners(SWT.KeyDown);
    	Listener[] keyUpListeners = control.getListeners(SWT.KeyUp);
    	
    	removeAll(control, SWT.KeyDown, keyDownListeners);
    	removeAll(control, SWT.KeyUp, keyUpListeners);

    	control.addKeyListener(listener);
    	
    	addAll(control, SWT.KeyDown, keyDownListeners);
    	addAll(control, SWT.KeyUp, keyUpListeners);
    }
    private void removeAll(Control control, int eventType, Listener[] listeners) {
    	for (Listener listener: listeners) {
    		control.removeListener(eventType, listener);
    	}
    }
    private void addAll(Control control, int eventType, Listener[] listeners) {
    	for (Listener listener: listeners) {
    		control.addListener(eventType, listener);
    	}
    }
    
    public static StatusLineContributionItem createStructuralEditionModeStatusContributionItem() {
		return new StatusLineContributionItem(
				ClojureSourceViewer.STATUS_CATEGORY_STRUCTURAL_EDITION, 
				true, 
				STATUS_STRUCTURAL_EDITION_CHARS_WIDTH);
    }

    public void configure(SourceViewerConfiguration configuration) {
        assert(configuration instanceof IPropertyChangeListener);
        super.configure(configuration);
        
        initializeViewerColors();
        fConfiguration = (IPropertyChangeListener)configuration;
        
        fSelectionHistory = new SelectionHistory(this);
        
        // AR - In order to respect the configure/unconfigure life-cycle while propagating
        // and refreshing I need to do this little trick
        addPropertyChangeListener(fConfiguration);
    }

    @Override
    public void initializeViewerColors() {
		initializeViewerColors(getTextWidget(), fPreferenceStore, editorColors);
		// AR - it has to be initialized before SourceViewerConfiguration or
        //  the ITokenScanner won't be able to pick up the color.
//		if (fPreferenceStore != null) {
//			CCWPlugin.registerEditorColors(fPreferenceStore, getTextWidget().getForeground().getRGB());
//		}
	}
	
	public static void initializeViewerColors(StyledText styledText, IPreferenceStore preferenceStore, EditorColors editorColors) {
		if (preferenceStore != null) {
			// ----------- foreground color --------------------
			Color color= preferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
			    ? null
			    : CCWPlugin.getPreferenceColor(preferenceStore, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND);
			styledText.setForeground(color);

			editorColors.fForegroundColor= color;

			// ---------- background color ----------------------
			color= preferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
			    ? null
			    : CCWPlugin.getPreferenceColor(preferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
			styledText.setBackground(color);

			editorColors.fBackgroundColor= color;

			// ----------- selection foreground color --------------------
			color= preferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR)
				? null
				: CCWPlugin.getPreferenceColor(preferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR);
			styledText.setSelectionForeground(color);

			editorColors.fSelectionForegroundColor= color;

			// ---------- selection background color ----------------------
			color= preferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR)
				? null
				: CCWPlugin.getPreferenceColor(preferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR);
			styledText.setSelectionBackground(color);

			editorColors.fSelectionBackgroundColor= color;

			// ---------- current line background color ----------------------
			color= CCWPlugin.getPreferenceColor(preferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR);

			editorColors.fCurrentLineBackgroundColor= color;
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
	 * @since 3.0
	 */
	public void unconfigure() {

	    removePropertyChangeListener(fConfiguration);

	    if (fSelectionHistory != null) {
	        fSelectionHistory.dispose();
	        fSelectionHistory = null;
	    }

	    super.unconfigure();
	    fConfiguration = null;
	}

    /** This is manipulated by clojure functions.
     * It's a ref, holding a map {:text "the raw text file" :parser parser}
     * where state is a future holding the parser's state
     */
    private Object parseState; 

    private IDocumentListener parseTreeConstructorDocumentListener = new IDocumentListener() {
        public void documentAboutToBeChanged(DocumentEvent event) {
        	//  TODO ?? maybe call updateTextBuffer directly from within an overriden method of AbstractDocument (so creating our own ClojureDocument ?) => maintaining parse tree with document ...
        	String newText = replace(event.getDocument().get(), event.getOffset(), event.getLength(), event.getText());
        	updateTextBuffer(newText, event.getOffset(), event.getLength(), event.getText());
        }
        public void documentChanged(DocumentEvent event) {
        }
    };
    
    private String replace(String doc, int offset, int length, String text) {
    	return doc.substring(0, offset) + text + doc.substring(offset + length);
    }
    
    private void updateTextBuffer (String finalText, long offset, long length, String text) {
    	boolean firstTime = (parseState == null);
    	parseState = editorSupport._("updateTextBuffer",parseState, finalText, offset, length, text);
        if (firstTime) {
        	editorSupport._("startWatchParseRef", parseState, this);
        }
    }
    
    // TODO rename getParseInfo or get.. ?
    public Object getParseState () {
        if (parseState == null) {
        	String text = getDocument().get();
            updateTextBuffer(text, 0, -1, text);
        }
        return editorSupport._("getParseState", getDocument().get(), parseState);
    }
    
    public boolean isParseTreeBroken() {
    	return (Boolean) editorSupport._("brokenParseTree?", getParseState());
    }
    
    public Object getPreviousParseTree () {
        if (parseState == null) {
        	return null;
        } else {
        	return editorSupport._("getPreviousParseTree", parseState);
        }
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
				StyledText textWidget = getTextWidget();
				if (textWidget != null && !textWidget.isDisposed()) {
					getTextWidget().setBackground(
							structuralEditionPossible ? editorColors.fBackgroundColor : Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
					getTextWidget().setToolTipText(structuralEditionPossible ? null : "Unparseable source code. Structural Edition temporarily disabled.");

				}
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
    
    public boolean isShowRainbowParens() {
    	return isShowRainbowParens;
    }

    public boolean isInEscapeSequence () {
        return inEscapeSequence;
    }
    
    public String findDeclaringNamespace() {
		return ClojureCore.findDeclaringNamespace((Map) editorSupport._("getParseTree", getParseState()));
    }

    public IJavaProject getAssociatedProject() {
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
    		String text = document.get();
    		updateTextBuffer(text, 0, -1, text);
    	}
    }
    
    /** Preference key for matching brackets color */
	//PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;

	public final static char[] PAIRS= { '{', '}', '(', ')', '[', ']' };
	public static final int STATUS_STRUCTURAL_EDITION_CHARS_WIDTH = 33;
	
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
			updateStructuralEditingModeStatusField();
		}
	}
	
    public void toggleStructuralEditionMode() {
       useStrictStructuralEditing = !useStrictStructuralEditing;
       updateStructuralEditingModeStatusField();
    }
	
    public void toggleShowRainbowParens() {
       isShowRainbowParens = !isShowRainbowParens;
       markDamagedAndRedraw();
    }
    
    @Override
	public void markDamagedAndRedraw() {
        // AR - This method might be concurrently called by multiple threads,
        // but it should not make too much damage
        try {
     	   isForceRepair = true;
     	   this.invalidateTextPresentation();
        } catch (Exception e) {
            CCWPlugin.logError(e);
        } finally {
     	   isForceRepair = false;
        }
    }
    
    /* (non-Javadoc)
     * @see ccw.editors.clojure.IClojureSourceViewer#isForceRepair()
     */
    @Override
    public boolean isForceRepair() {
    	return isForceRepair;
    }
    
	public void updateStructuralEditingModeStatusField() {
		if (this.statusLineHandler == null) {
			return;
		}
			
		StatusLineContributionItem field = this.statusLineHandler.getEditingModeStatusContributionItem();
		if (field != null) {
			field.setText((isStructuralEditingEnabled() ? "strict/paredit" : "unrestricted")
			              + " edit mode" + (inEscapeSequence ? " ESC" : ""));
			field.setToolTipText(
					(isStructuralEditingEnabled() 
							? "strict/paredit edit mode:\neditor does its best to prevent you from breaking the structure of the code (requires you to know shortcut commands well)."
						    : "unrestricted edit mode:\nhelps you with edition, but does not get in your way."));
		}
	}

	/*
	 * Eclipse TextEditor framework uses old "Action" framework. So it is impossible
	 * to use handlers declaratively, one must plug the new behaviour via code,
	 * some way or the other.
	 * It was decided here to plug new behaviour by overriding directly the 
	 * doOperation(operation) call, at the most central point, that is.
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.projection.ProjectionViewer#doOperation(int)
	 */
	@Override
	public void doOperation(int operation) {
		if (operation == TextViewer.PASTE) {
			if (!getTextWidget().getBlockSelection()) {
				handlers._("smart-paste", this);
				return;
			} else {
				// We're not trying (at least yet) to handle paste inside
				// block selections
				super.doOperation(operation);
			}
		} else {
			super.doOperation(operation);
		}
	}

	public Object getAdapter(Class adapter) {
        if (IClojureEditor.class == adapter) {
			return this;
		}
		if (ITextOperationTarget.class == adapter) {
			return this;
		}
		return null;
	}
	
	public boolean isEscapeInStringLiteralsEnabled() {
		return fPreferenceStore.getBoolean(PreferenceConstants.EDITOR_ESCAPE_ON_PASTE);
	}

	public boolean isContentAssistantActive() {
		return isContentAssistantActive;
	}

	public void setContentAssistantActive(boolean isContentAssistantActive) {
		this.isContentAssistantActive = isContentAssistantActive;
	}

    @Override
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        fPreferenceStore.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        fPreferenceStore.removePropertyChangeListener(listener);
    }

    @Override
    public @Nullable String getTopLevelSExpression(int caretOffset) {
        String form = (String) editorSupport._("top-level-code-form", getParseState(), caretOffset);
        return form;
    }
}
