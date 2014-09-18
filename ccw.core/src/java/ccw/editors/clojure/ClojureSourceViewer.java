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

package ccw.editors.clojure;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
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
import org.eclipse.jface.text.TextViewer;
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
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

import clojure.lang.RT;
import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.preferences.PreferenceConstants;
import ccw.repl.REPLView;
import ccw.util.ClojureInvoker;
import ccw.util.DisplayUtil;

public abstract class ClojureSourceViewer extends ProjectionViewer implements
        IClojureEditor, IPropertyChangeListener {
    
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
    
    public static interface ModeListener {
        void modeChanged(ClojureEditorMode mode, boolean esc);
    }

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
    public static abstract class EditingModeStatusUpdater implements ModeListener {

        abstract protected StatusLineContributionItem getEditingModeStatusContributionItem();

        public void modeChanged(ClojureEditorMode mode, boolean inEscapeSequence) {
            StatusLineContributionItem field = getEditingModeStatusContributionItem();
            if (field != null) {
                switch (mode) {
                case TEXT:
                    field.setText("text edit mode" + (inEscapeSequence ? " ESC" : ""));
                    field.setToolTipText("unrestricted edit mode:\nhelps you with edition, but does not get in your way.");
                    break;
                case PAREDIT:
                    field.setText("paredit mode" + (inEscapeSequence ? " ESC" : ""));
                    field.setToolTipText("paredit edit mode:\neditor does its best to prevent you from breaking the structure of the code (requires you to know shortcut commands well).");
                    break;
                case STRUCTEDIT:
                    field.setText("structedit mode" + (inEscapeSequence ? " ESC" : ""));
                    field.setToolTipText("structedit edit mode:\neditor does its best to assist you manipulating the structure of the code (requires you to know shortcut commands well).");
                    break;
                }
            }
        }
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
         * This viewer's struct selection foreground color.
         */
        public Color fStructSelectionForegroundColor;
        
        /**
         * The viewer's struct selection background color.
         */
        public Color fStructSelectionBackgroundColor;

    	/**
    	 * The viewer's background color for the selected line
    	 */
		public Color fCurrentLineBackgroundColor;

		public void unconfigure() {
			fForegroundColor = unconfigure(fForegroundColor);
			fBackgroundColor = unconfigure(fBackgroundColor= null);
            fSelectionForegroundColor = unconfigure(fSelectionForegroundColor);
            fSelectionBackgroundColor = unconfigure(fSelectionBackgroundColor);
            fStructSelectionForegroundColor = unconfigure(fStructSelectionForegroundColor);
            fStructSelectionBackgroundColor = unconfigure(fStructSelectionBackgroundColor);
			fCurrentLineBackgroundColor = unconfigure(fCurrentLineBackgroundColor);
		}

		private Color unconfigure(Color c) {
			if (c != null) { 
				c.dispose(); 
			}
			return null;
		}
    }
    
    private EditorColors editorColors = new EditorColors();

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
    
    private boolean isContentAssistantActive;
    
    private ClojureEditorMode editorMode;

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
	
	private Set<ModeListener> modeListeners = new HashSet<ClojureSourceViewer.ModeListener>();
	
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
        getTextWidget().addListener(SWT.MouseVerticalWheel, new Listener() {
            public void handleEvent(Event event) {
                if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.SHIFT) {
                    if (event.count > 0) {
                        handlers._("fire-structedit-event", ClojureSourceViewer.this, "paredit-expand-up");
                    } else {
                        handlers._("do-select-last", ClojureSourceViewer.this);
                    }
                    event.doit = false; 
                }
            }
        });
        getTextWidget().addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                if (getTextWidget().isFocusControl() && (e.stateMask & SWT.MODIFIER_MASK) == SWT.COMMAND) {
                    int offset = getOffsetFor(e.x, e.y);
                    if (offset >= 0)
                        handlers._("do-select", ClojureSourceViewer.this, offset);
                }
            }
        });
        getTextWidget().addListener(SWT.Gesture, new Listener() {
            private int lastx = -1, lasty = -1;
            private double lastMagnification;
            private int lastTime;
            public void handleEvent(Event event) {
                switch (event.detail) {
                case SWT.GESTURE_BEGIN:
                    lastMagnification = 1.0;
                    lastTime = event.time;
                    break;
                case SWT.GESTURE_MAGNIFY:
                    event.doit = false;
                    if (event.time - lastTime < 200) {
                        return;
                    }
                    double rmag = (lastx == event.x) && (lasty == event.y) ? event.magnification / lastMagnification : event.magnification;
                    lastMagnification = event.magnification;
                    lastx = event.x;
                    lasty = event.y;
                    lastTime = event.time;
                    System.err.println(rmag);
                    if (rmag > 1.0) {
                        handlers._("fire-structedit-event", ClojureSourceViewer.this, "paredit-expand-up");
                    } else {
                        handlers._("do-select-last", ClojureSourceViewer.this);
                    }
                    break;
                }
            }
        });
        addModeListener(new ModeListener() {
            public void modeChanged(ClojureEditorMode mode, boolean esc) {
                if (mode == ClojureEditorMode.STRUCTEDIT) {
                    getTextWidget().setSelectionBackground(editorColors.fStructSelectionBackgroundColor);
                    getTextWidget().setSelectionForeground(editorColors.fStructSelectionForegroundColor);
                } else {
                    getTextWidget().setSelectionBackground(editorColors.fSelectionBackgroundColor);
                    getTextWidget().setSelectionForeground(editorColors.fSelectionForegroundColor);
                } 
            }
        });

        // add before all other listeners so that we're certain we enable/disable
        //the Esc key feature based on accurate state information
        prependVerifyKeyListener(new VerifyKeyListener() {
            @Override
            public void verifyKey(VerifyEvent e) {
                // TODO when isContentAssistantActive does not handle a key, give us back control
                if (isContentAssistantActive) return;
                if (inEscapeSequence) {
                    inEscapeSequence = false;
                    updateTabsToSpacesConverter();
                    fireModeChange();
                    return;
                }
                if (e.character == SWT.ESC) {
                    if (getMode() == ClojureEditorMode.STRUCTEDIT) {
                        setMode(ClojureEditorMode.PAREDIT);
                        e.doit = false;
                        return;
                    }
                    inEscapeSequence = true;
                    updateTabsToSpacesConverter();
                    fireModeChange();
                    e.doit = false; // double esc -> single esc
                    return;
                }
                e.doit = !RT.booleanCast(handlers._("structedit-key-event", e, ClojureSourceViewer.this, getParseState(), getDocument()));
            }
        });
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

        editorMode = store.getBoolean(ccw.preferences.PreferenceConstants.USE_STRICT_STRUCTURAL_EDITING_MODE_BY_DEFAULT) ? ClojureEditorMode.PAREDIT : ClojureEditorMode.TEXT;
		isShowRainbowParens = store.getBoolean(ccw.preferences.PreferenceConstants.SHOW_RAINBOW_PARENS_BY_DEFAULT);
	}

    public int getOffsetFor(int x, int y) {
        StyledText styledText= getTextWidget();
        try {
            int widgetOffset= styledText.getOffsetAtLocation(new Point(x, y));
            Point p= styledText.getLocationAtOffset(widgetOffset);
            if (p.x > x)
                widgetOffset--;
            return this.widgetOffset2ModelOffset(widgetOffset);
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }
    
    public static StatusLineContributionItem createStructuralEditionModeStatusContributionItem() {
		return new StatusLineContributionItem(
				ClojureSourceViewer.STATUS_CATEGORY_STRUCTURAL_EDITION, 
				true, 
				STATUS_STRUCTURAL_EDITION_CHARS_WIDTH);
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (fConfiguration != null) {
            ClojureSourceViewerConfiguration tmp = fConfiguration;
            unconfigure();
            initializeViewerColors();
            tmp.initTokenScanner();
            configure(tmp); // TODO this causes setRange() to be called twice (does the reinitialization of things
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
        RGB rgb = getRGBColor(store, key);
        return (rgb!= null) ? new Color(display, rgb) : null;
    }
    
    static public RGB getRGBColor(IPreferenceStore store, String key) {
        RGB rgb = null;

        if (store.contains(key)) {
            if (store.isDefault(key))
                rgb = PreferenceConverter.getDefaultColor(store, key);
            else
                rgb = PreferenceConverter.getColor(store, key);
        }

        return rgb;
    }

    public void initializeViewerColors() {
		initializeViewerColors(getTextWidget(), fPreferenceStore, editorColors);
		if (fPreferenceStore != null) {
			CCWPlugin.registerEditorColors(fPreferenceStore, getTextWidget().getForeground().getRGB());
		}
	}
	
	public static void initializeViewerColors(StyledText styledText, IPreferenceStore preferenceStore, EditorColors editorColors) {
		if (preferenceStore != null) {
			// ----------- foreground color --------------------
			Color color= preferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
			? null
			: createColor(preferenceStore, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, styledText.getDisplay());
			styledText.setForeground(color);

			if (editorColors.fForegroundColor != null)
				editorColors.fForegroundColor.dispose();

			editorColors.fForegroundColor= color;

			// ---------- background color ----------------------
			color= preferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
			? null
			: createColor(preferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
			styledText.setBackground(color);

			if (editorColors.fBackgroundColor != null)
				editorColors.fBackgroundColor.dispose();

			editorColors.fBackgroundColor= color;

            // ----------- selection foreground color --------------------
            color= preferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR)
                ? null
                : createColor(preferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, styledText.getDisplay());
            styledText.setSelectionForeground(color);

            if (editorColors.fSelectionForegroundColor != null)
                editorColors.fSelectionForegroundColor.dispose();

            editorColors.fSelectionForegroundColor= color;

            // ---------- selection background color ----------------------
            color= preferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR)
                ? null
                : createColor(preferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, styledText.getDisplay());
            styledText.setSelectionBackground(color);

            if (editorColors.fSelectionBackgroundColor != null)
                editorColors.fSelectionBackgroundColor.dispose();

            editorColors.fSelectionBackgroundColor= color;

            // ----------- struct selection foreground color --------------------
            color = new Color(styledText.getDisplay(), new RGB(0, 0, 0));
            //            color= createColor(preferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, styledText.getDisplay());

            if (editorColors.fStructSelectionForegroundColor != null)
                editorColors.fStructSelectionForegroundColor.dispose();

            editorColors.fStructSelectionForegroundColor= color;

            // ---------- struct selection background color ----------------------
            color = new Color(styledText.getDisplay(), new RGB(255, 255, 0));
 //           color= createColor(preferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, styledText.getDisplay());

            if (editorColors.fStructSelectionBackgroundColor != null)
                editorColors.fStructSelectionBackgroundColor.dispose();

            editorColors.fStructSelectionBackgroundColor= color;

			// ---------- current line background color ----------------------
			color= createColor(preferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, styledText.getDisplay());

			if (editorColors.fCurrentLineBackgroundColor != null)
				editorColors.fCurrentLineBackgroundColor.dispose();

			editorColors.fCurrentLineBackgroundColor= color;
		}
    }

    /*
     * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
     * @since 3.0
     */
    public void unconfigure() {
		
		editorColors.unconfigure();
		
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
				getTextWidget().setBackground(
						structuralEditionPossible ? editorColors.fBackgroundColor : Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
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
        return editorMode == ClojureEditorMode.PAREDIT;
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
	
    public void toggleStructuralEditionMode() {
       setMode(editorMode == ClojureEditorMode.TEXT ? ClojureEditorMode.PAREDIT : ClojureEditorMode.TEXT);
    }
	
    public void addModeListener(ModeListener listener) {
        modeListeners.add(listener);
        listener.modeChanged(editorMode, inEscapeSequence);
    }

    public void setMode(ClojureEditorMode mode) {
        editorMode = mode;
        inEscapeSequence = false;
        fireModeChange();
    }

    protected void fireModeChange() {
        for (ModeListener modeListener : modeListeners) {
            modeListener.modeChanged(editorMode, inEscapeSequence);
        }
    }

    public ClojureEditorMode getMode() {
        return editorMode;
    }

    public void toggleShowRainbowParens() {
       isShowRainbowParens = !isShowRainbowParens;
       markDamagedAndRedraw();
    }
    

	public void markDamagedAndRedraw() {
        try {
     	   isForceRepair = true;
            this.invalidateTextPresentation();
        } finally {
     	   isForceRepair = false;
        }
    }
    
    /**
     * @return true to indicate a Damager to consider that the whole document
     *         must be considered damaged, e.g. to force syntax coloring & al.
     *         to refresh.
     */
    public boolean isForceRepair() {
    	return isForceRepair;
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
		if ( IClojureEditor.class == adapter) {
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

	
}
