/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.editors.antlrbased;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IStatusField;
import org.eclipse.ui.texteditor.IStatusFieldExtension;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ccw.CCWPlugin;
import ccw.editors.outline.ClojureOutlinePage;
import ccw.editors.rulesbased.ClojureDocumentProvider;
import ccw.editors.rulesbased.ClojurePartitionScanner;
import ccw.launching.ClojureLaunchShortcut;
import ccw.repl.REPLView;

public class AntlrBasedClojureEditor extends TextEditor implements IClojureEditor {
	public static final String EDITOR_REFERENCE_HELP_CONTEXT_ID = "ccw.branding.editor_context_help";
    public static final String STATUS_CATEGORY_STRUCTURAL_EDITION = "CCW.STATUS_CATEGORY_STRUCTURAL_EDITING_POSSIBLE";
	
    private static final String CONTENT_ASSIST_PROPOSAL = "ContentAssistProposal"; //$NON-NLS-1$
    public static final String ID = "ccw.antlrbasededitor"; //$NON-NLS-1$
	/** Preference key for matching brackets */
	//PreferenceConstants.EDITOR_MATCHING_BRACKETS;

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
	
	/** History for structure select action
	 * STOLEN FROM THE JDT */
	private SelectionHistory fSelectionHistory;


	/** The projection support */
	private ProjectionSupport fProjectionSupport;

	private ClojureOutlinePage outlinePage;
	
	/** 
	 * Set to false if structural editing is not possible, because the document
	 * is not parseable.
	 */
	private boolean structuralEditingPossible;
	/**
	 * Set to true if the editor is in "Strict" Structural editing mode
	 */
	private boolean useStrictStructuralEditing;

	public boolean useStrictStructuralEditing() {
		return useStrictStructuralEditing;
	}
	
	public boolean isStructuralEditingEnabled () {
	    // @todo eliminate non-idomatic useStrictStructuralEditing method
	    return useStrictStructuralEditing();
	}
	
	public AntlrBasedClojureEditor() {
        setPreferenceStore(CCWPlugin.getDefault().getCombinedPreferenceStore());
		setSourceViewerConfiguration(new ClojureSourceViewerConfiguration(getPreferenceStore(), this));
        setDocumentProvider(new ClojureDocumentProvider()); 
        setHelpContextId(EDITOR_REFERENCE_HELP_CONTEXT_ID);
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		useStrictStructuralEditing = getPreferenceStore().getBoolean(ccw.preferences.PreferenceConstants.USE_STRICT_STRUCTURAL_EDITING_MODE_BY_DEFAULT);
	}
	
	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		support.setCharacterPairMatcher(pairsMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(PreferenceConstants.EDITOR_MATCHING_BRACKETS, PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);
		super.configureSourceViewerDecorationSupport(support);
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		
		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());
		
		// ISourceViewer viewer= new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		ISourceViewer viewer= new ClojureSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, getPreferenceStore());
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	/*
	 * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ClojureSourceViewer viewer= (ClojureSourceViewer) getSourceViewer();
		
		/*
		 * Need to hook up here to force a re-evaluation of the preferences
		 * for the syntax coloring, after the token scanner has been
		 * initialized. Otherwise the very first Clojure editor will not
		 * have any tokens colored.
		 * 
         * TODO this is repeated in REPLView...surely we can make the source viewer self-sufficient here
		 */
	    viewer.propertyChange(null);
	    
		fProjectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		
		// TODO remove the 2 following lines ?
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		
		fProjectionSupport.install();
		viewer.doOperation(ClojureSourceViewer.TOGGLE);
	}
	
	public boolean isInEscapeSequence () {
	    return ((ClojureSourceViewer)getSourceViewer()).isInEscapeSequence();
	}
	
	public void setStructuralEditingPossible(boolean state) {
		if (state != this.structuralEditingPossible) {
			this.structuralEditingPossible = state;
			updateStatusField(STATUS_CATEGORY_STRUCTURAL_EDITION);
		}
	}
	
	public void toggleStructuralEditionMode() {
		useStrictStructuralEditing = !useStrictStructuralEditing;
		updateStatusField(STATUS_CATEGORY_STRUCTURAL_EDITION);
	}
	
	protected void updateStatusField(String category) {
		if (!STATUS_CATEGORY_STRUCTURAL_EDITION.equals(category)) {
			super.updateStatusField(category);
			return;
		}

		if (category == null)
			return;

		IStatusField field= getStatusField(category);
		IStatusFieldExtension extField = (IStatusFieldExtension) field;
		if (field != null) {
			/*
			 * Disabled, because currently structuralEditingPossible is not reliable (some paredit commands stop after having parsed all the text)
			 * TODO reactivate when paredit has been ported to parsley
			String text= "Structural Edition: " 
				+ (structuralEditingPossible ? "enabled" : "disabled");
				*/
			String text = "Structural Edition: " + (useStrictStructuralEditing ? "Strict mode" : "Default mode");
			field.setText(text == null ? fErrorLabel : text);
			extField.setToolTipText(
					(useStrictStructuralEditing 
							? "Strict mode: editor does its best to prevent you from breaking the structure of the code (requires you to know shortcut commands well). Click to switch to Default Mode."
						   : "Default mode: helps you with edition, but does not get in your way Click to switch to Strict Mode."));
		}
	}

	
    public DefaultCharacterPairMatcher getPairsMatcher() {
        return pairsMatcher;
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        return getSourceViewer().getSelectionProvider();
    }

	@Override
	protected void createActions() {
		super.createActions();
		
		// @todo push many (if not most) of these into ClojureSourceViewer (somehow, that's SWT and actions are eclipse-land :-/)
		Action action= new GotoMatchingBracketAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
		setAction(GotoMatchingBracketAction.ID, action);
		
		action = new GotoNextMemberAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.GOTO_NEXT_MEMBER);
		setAction(GotoNextMemberAction.ID, action);

		action = new GotoPreviousMemberAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.GOTO_PREVIOUS_MEMBER);
		setAction(GotoPreviousMemberAction.ID, action);

		action = new SelectTopLevelSExpressionAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.SELECT_TOP_LEVEL_S_EXPRESSION);
		setAction(SelectTopLevelSExpressionAction.ID, action);

		action = new EvaluateTopLevelSExpressionAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.EVALUATE_TOP_LEVEL_S_EXPRESSION);
		setAction(EvaluateTopLevelSExpressionAction.ID, action);

		action = new LoadFileAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.LOAD_FILE);
		setAction(LoadFileAction.ID, action);

		action = new CompileLibAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.COMPILE_LIB);
		setAction(CompileLibAction.ID, action);

		action = new RunTestsAction(this, CCWPlugin.getDefault().getColorRegistry());
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.RUN_TESTS);
		setAction(RunTestsAction.RUN_TESTS_ID, action);
		/*
		action = new FormatAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.FORMAT_CODE);
		setAction(FormatAction.ID, action);
	    */
		action = new OpenDeclarationAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.OPEN_DECLARATION);
		setAction(IHyperlinkConstants.OpenDeclarationAction_ID, action);
		
		action = new Action() {
			@Override
			public void run() {
				new ClojureLaunchShortcut().launch(AntlrBasedClojureEditor.this, ILaunchManager.RUN_MODE);
			};
		};
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.LAUNCH_REPL);
		setAction("ClojureLaunchAction", action);

		action = new ContentAssistAction(ClojureEditorMessages.getBundleForConstructedKeys(), CONTENT_ASSIST_PROPOSAL + ".", this);  //$NON-NLS-1$
		String id = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
		action.setActionDefinitionId(id);
		setAction(CONTENT_ASSIST_PROPOSAL, action); 
		markAsStateDependentAction(CONTENT_ASSIST_PROPOSAL, true);

		// Copied directly from JDT (no interest in owning the code currently)
		fSelectionHistory = new SelectionHistory(this);
		StructureSelectHistoryAction historyAction= new StructureSelectHistoryAction(this, fSelectionHistory);
		historyAction.setActionDefinitionId(IClojureEditorActionDefinitionIds.SELECT_LAST);
		setAction("RestoreSelection", historyAction);
		fSelectionHistory.setHistoryAction(historyAction);

		action = new ExpandSelectionUpAction(this, fSelectionHistory);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.EXPAND_SELECTION_UP);
		setAction(/*ExpandSelectionUpAction.ID*/"ExpandSelectionUpAction", action);
		
		action = new ExpandSelectionLeftAction(this, fSelectionHistory);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.EXPAND_SELECTION_LEFT);
		setAction(/*ExpandSelectionLeftAction.ID*/"ExpandSelectionLeftAction", action);
		
		action = new ExpandSelectionRightAction(this, fSelectionHistory);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.EXPAND_SELECTION_RIGHT);
		setAction(/*ExpandSelectionRightAction.ID*/"ExpandSelectionRightAction", action);
		
		action = new IndentSelectionAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.INDENT_SELECTION);
		setAction(/*IndentSelectionAction.ID*/"IndentSelectionAction", action);

		action = new RaiseSelectionAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.RAISE_SELECTION);
		setAction(/*RaiseSelectionAction.ID*/"RaiseSelectionAction", action);

		action = new SplitSexprAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.SPLIT_SEXPR);
		setAction(/*SplitSexprAction.ID*/"SplitSexprAction", action);

		action = new JoinSexprAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.JOIN_SEXPR);
		setAction(/*JoinSexprAction.ID*/"JoinSexprAction", action);
		
		action = new SwitchStructuralEditionModeAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.SWITCH_STRUCTURAL_EDITION_MODE);
		setAction(/*SwitchStructuralEditionModeAction.ID*/"SwitchStructuralEditionModeAction", action);
}
	
	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {
		ISourceViewer sourceViewer= getSourceViewer();
		IDocument document= sourceViewer.getDocument();
		if (document == null)
			return;

		IRegion selection= getSignedSelection();

		int selectionLength= Math.abs(selection.getLength());
		if (selectionLength > 1) {
			setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracketAction_error_invalidSelection);
			sourceViewer.getTextWidget().getDisplay().beep();
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
			sourceViewer.getTextWidget().getDisplay().beep();
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
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) sourceViewer;
			visible= (extension.modelOffset2WidgetOffset(targetOffset) > -1);
		} else {
			IRegion visibleRegion= sourceViewer.getVisibleRegion();
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
			visible= (targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
		}

		if (!visible) {
			setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracketAction_error_bracketOutsideSelectedElement);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		if (selection.getLength() < 0)
			targetOffset -= selection.getLength();

		sourceViewer.setSelectedRange(targetOffset, selection.getLength());
		sourceViewer.revealRange(targetOffset, selection.getLength());
	}

	/**
	 * Move to beginning of current or preceding defun (beginning-of-defun).
	 */
	/*
	 * place 0 in variable currentLevel, and 0 in variable highest level
	 *         and -1 in variable highest level open paren
	 * repeat until found beginning of file:
	 *   move backward until you find a left or right paren (or attain the beginning
	 *   of the file)
	 *   update currentChar position with the position of the paren
	 *   if open paren
	 *     increment currentLevel. 
	 *      if currentLevel > highest level
	 *        highest level <- currentLevel
	 *        highest level open paren <- currentChar position
	 *     else if close paren
	 *       decrement currentLevel      
	 * if beginning of file
	 *   if highest level still 0 : no solution
	 *   else return highest level open paren
	 *   
	 * Note: the found paren must be in the correct partition (code, not string or comment)
	 */
	public void gotoPreviousMember() {
		if (!checkSelectionAndWarnUserIfProblem(ClojureEditorMessages.GotoMatchingBracketAction_error_invalidSelection))
			return;

		int sourceCaretOffset= getSourceCaretOffset();
		
		int previousMemberOffset = getBeginningOfCurrentOrPrecedingTopLevelSExpressionFor(sourceCaretOffset);

		if (previousMemberOffset >= 0) 
			selectAndReveal(previousMemberOffset, 0);
	}
	
	private boolean checkSelectionAndWarnUserIfProblem(String errorMessageIfProblem) {
		if (getDocument() == null)
			return false;
		
		IRegion selection= getSignedSelection();

		int selectionLength= Math.abs(selection.getLength());
		if (selectionLength > 0) {
			setStatusLineErrorMessage(errorMessageIfProblem);
			getSourceViewer().getTextWidget().getDisplay().beep();
			return false;
		}
		
		return true;
	}
	
	public IDocument getDocument() {
		ISourceViewer sourceViewer= getSourceViewer();
		return sourceViewer.getDocument();
	}
	
	/**
	 * Asserts document != null.
	 * @return
	 */
	public int getSourceCaretOffset() {
		IRegion selection= getSignedSelection();
		return selection.getOffset() + selection.getLength();
	}
	
	private int getBeginningOfCurrentOrPrecedingTopLevelSExpressionFor(final int sourceCaretOffset) {
		IDocument document= getDocument();

		int currentLevel = 0;
		int highestLevel = 0;
		int highestLevelCaretOffset = -1;
		int nextParenOffset = sourceCaretOffset - 1;
		
		try {
			while (nextParenOffset >= 0) {
				nextParenOffset = nextCharInContentTypeMatching(nextParenOffset, 
						IDocument.DEFAULT_CONTENT_TYPE, 
						new char[] {'(',')'}, false);
				if (nextParenOffset == -1) break;
				if (document.getChar(nextParenOffset) == '(')
					currentLevel += 1;
				else if (document.getChar(nextParenOffset) == ')')
					currentLevel -= 1;

				if (currentLevel > highestLevel) {
					highestLevel = currentLevel;
					highestLevelCaretOffset = nextParenOffset;
				} else if (currentLevel == 0 && highestLevelCaretOffset == -1) {
					highestLevelCaretOffset = nextParenOffset;
				}
				
				nextParenOffset--;
			}
			
		} catch (BadLocationException e) {
		}
		return highestLevelCaretOffset;
	}
	
	public void selectTopLevelSExpression() {
		IRegion r = getTopLevelSExpression();
		
		if (r != null)
			selectAndReveal(r.getOffset(), r.getLength());
	}
	
	private IRegion getTopLevelSExpression() {
		if (!checkSelectionAndWarnUserIfProblem(ClojureEditorMessages.GotoMatchingBracketAction_error_invalidSelection))
			return null;

		int sourceCaretOffset = getSourceCaretOffset();
		
		int endOffset = getEndOfCurrentOrNextTopLevelSExpressionFor(sourceCaretOffset);
		int beginningOffset = getBeginningOfCurrentOrPrecedingTopLevelSExpressionFor(endOffset);

		if (beginningOffset>=0 && endOffset>=0) {
			// length made to not include end position but
			// (end position - 1)
			int length = endOffset - beginningOffset;
			return new Region(beginningOffset, length);
		} else {
			return null;
		}
	}
	
	public String getCurrentOrNextTopLevelSExpression() {
		IRegion r = getTopLevelSExpression();
		
		if (r != null) {
			try {
				return getDocument().get(r.getOffset(), r.getLength());
			} catch (BadLocationException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Move to end of current or following defun (end-of-defun).
	 */
	public void gotoEndOfMember() {
		if (!checkSelectionAndWarnUserIfProblem(ClojureEditorMessages.GotoMatchingBracketAction_error_invalidSelection))
			return;

		int sourceCaretOffset= getSourceCaretOffset();
		int endOfMemberOffset = getEndOfCurrentOrNextTopLevelSExpressionFor(sourceCaretOffset);

		if (endOfMemberOffset >= 0)
			selectAndReveal(endOfMemberOffset, 0);
	}
	
	private int getEndOfCurrentOrNextTopLevelSExpressionFor(int sourceCaretOffset) {
		
		ISourceViewer sourceViewer= getSourceViewer();
		IDocument document= sourceViewer.getDocument();

		int currentLevel = 0;
		int highestLevel = 0;
		int highestLevelCaretOffset = -1;
		int nextParenOffset = sourceCaretOffset;

		try {
			while (nextParenOffset < document.getLength()) {
				nextParenOffset = nextCharInContentTypeMatching(nextParenOffset, 
						IDocument.DEFAULT_CONTENT_TYPE, 
						new char[] {'(',')'}, true);
				if (nextParenOffset == -1) break;
				if (document.getChar(nextParenOffset) == '(')
					currentLevel -= 1;
				else if (document.getChar(nextParenOffset) == ')')
					currentLevel += 1;
		
				if (currentLevel > highestLevel) {
					highestLevel = currentLevel;
					highestLevelCaretOffset = nextParenOffset;
				} else if (currentLevel == 0 && highestLevelCaretOffset == -1)
					highestLevelCaretOffset = nextParenOffset;
				
				nextParenOffset++;
			}
			if (highestLevelCaretOffset >= 0)
				if ((highestLevelCaretOffset + 1) <= document.getLength())
					highestLevelCaretOffset++;
		} catch (BadLocationException e) {
		}
		return highestLevelCaretOffset; 
	}

	private int nextCharInContentTypeMatching(int currentOffset, String contentType, char[] charsToMatch, boolean searchForward) throws BadLocationException {
		ISourceViewer sourceViewer= getSourceViewer();
		IDocument document= sourceViewer.getDocument();
		
		int offset = currentOffset;
		while ( !( TextUtilities.getContentType(document, 
						ClojurePartitionScanner.CLOJURE_PARTITIONING, offset, false).equals(contentType) 
				   && matchChar(document.getChar(offset), charsToMatch) ) ) {
			if (searchForward) {
				offset++;
				if (offset >= document.getLength())
					return -1;
			} else {
				offset--;
				if (offset < 0)
					return -1;
			}
		}
		return offset;
	}
	
	private boolean matchChar(char c, char[] charsToMatch) {
		for (char ctm: charsToMatch)
			if (c == ctm)
				return true;
		return false;
	}

	public IRegion getUnSignedSelection () {
	    return ((ClojureSourceViewer)getSourceViewer()).getUnSignedSelection();
	}
	
	public IRegion getSignedSelection () {
	    return ((ClojureSourceViewer)getSourceViewer()).getSignedSelection();
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "ccw.ui.clojureEditorScope" });  //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		if (pairsMatcher != null) {
			pairsMatcher.dispose();
			pairsMatcher = null;
		}
		if (fSelectionHistory != null) {
			fSelectionHistory.dispose();
			fSelectionHistory = null;
		}
		super.dispose();
	}

	/** The <code>JavaEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method performs gets
	 * the java content outline page if request is for a an 
	 * outline page.
	 * 
	 * @param required the required type
	 * @return an adapter for the required type or <code>null</code>
	 */ 
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (outlinePage == null) {
				outlinePage= new ClojureOutlinePage(getDocumentProvider(), this);
				if (getEditorInput() != null)
					outlinePage.setInput(getEditorInput());
			}
			return outlinePage;
		}
		
		if (fProjectionSupport != null) {
			Object adapter= fProjectionSupport.getAdapter(getSourceViewer(), required);
			if (adapter != null)
				return adapter;
		}
		
		return super.getAdapter(required);
	}

	public String getSelectedText() {
		IRegion r = getUnSignedSelection();
		
		if (r != null) {
			try {
				return getDocument().get(r.getOffset(), r.getLength());
			} catch (BadLocationException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public IJavaProject getAssociatedProject () {
	    return JavaCore.create(((IFile)getEditorInput().getAdapter(IFile.class)).getProject());
	}
	
	public String getDeclaringNamespace () {
		return ((ClojureSourceViewer)getSourceViewer()).getDeclaringNamespace();
	}
	
	public REPLView getCorrespondingREPL () {
		IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
		if (file != null)
			return CCWPlugin.getDefault().getProjectREPL(file.getProject());
		else 
			return null;
	}

	/*
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#setPreferenceStore(org.eclipse.jface.preference.IPreferenceStore)
     * @since 3.0
     */
    @Override
    protected void setPreferenceStore(IPreferenceStore store) {
        super.setPreferenceStore(store);
        if (getSourceViewer() instanceof ClojureSourceViewer) {
            ((ClojureSourceViewer) getSourceViewer()).setPreferenceStore(store);
        }
    }

    public final ClojureSourceViewer sourceViewer() {
        return (ClojureSourceViewer) super.getSourceViewer();
    }

    /** Change the visibility of the method to public */
    @Override
    public void setStatusLineErrorMessage(String message) {
        super.setStatusLineErrorMessage(message);
    }

    @Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		try {
			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer == null)
				return;

			String property= event.getProperty();

			if (ccw.preferences.PreferenceConstants.USE_TAB_FOR_REINDENTING_LINE.equals(property)) {
				updateTabsToSpacesConverter();
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}
    
    public void updateTabsToSpacesConverter() {
		if (isTabsToSpacesConversionEnabled()) {
			installTabsToSpacesConverter();
		} else {
			uninstallTabsToSpacesConverter();
		}
    }
    
    @Override
    protected boolean isTabsToSpacesConversionEnabled() {
    	if (getPreferenceStore().getBoolean(ccw.preferences.PreferenceConstants.USE_TAB_FOR_REINDENTING_LINE)
    			&& !isInEscapeSequence()) {
    		return false;
    	} else {
    		return super.isTabsToSpacesConversionEnabled();
    	}
    }
    
    /**
     * @return the project, or null if unknown 
     * (case when clojure file open from a Jar via a JarEditorInput, etc.)
     */
    public IProject getProject() {
    	IResource resource = (IResource) getEditorInput().getAdapter(IResource.class);
    	if (resource != null) {
    		return resource.getProject();
    	} else {
    		return null;
    	}
    	
    }
    
    public Object getParsed() {
    	return sourceViewer().getParsed();
    }
}
