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
package ccw.editors.clojure;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
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
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ccw.CCWPlugin;
import ccw.editors.outline.ClojureOutlinePage;
import ccw.launching.ClojureLaunchShortcut;
import ccw.repl.REPLView;

public class ClojureEditor extends TextEditor implements IClojureEditor {
	public static final String EDITOR_REFERENCE_HELP_CONTEXT_ID = "ccw.branding.editor_context_help";

    public static final String ID = "ccw.clojureeditor"; //$NON-NLS-1$
	/** Preference key for matching brackets */
	//PreferenceConstants.EDITOR_MATCHING_BRACKETS;

	public SelectionHistory getSelectionHistory() {
		return sourceViewer().getSelectionHistory();
	}

	/** The projection support */
	private ProjectionSupport fProjectionSupport;

	private ClojureOutlinePage outlinePage;

	public ClojureEditor() {
        setPreferenceStore(CCWPlugin.getDefault().getCombinedPreferenceStore());
		setSourceViewerConfiguration(new ClojureSourceViewerConfiguration(getPreferenceStore(), this));
        setDocumentProvider(new ClojureDocumentProvider());
        setHelpContextId(EDITOR_REFERENCE_HELP_CONTEXT_ID);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
	}

	ClojureSourceViewer viewer; // TODO try a way of removing this horrible hack
								// (currently if I replace viewer in configureSourceViewerDecorationSupport(),
								// there's a NPE thrown due to initialization ordering issue
	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		EditorSupport.configureSourceViewerDecorationSupport(support, viewer);
		super.configureSourceViewerDecorationSupport(support);
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());

		// ISourceViewer viewer= new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		ClojureSourceViewer viewer= new ClojureSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, getPreferenceStore()) {
			public void setStatusLineErrorMessage(String message) {
				ClojureEditor.this.setStatusLineErrorMessage(message);
			}
		};
		this.viewer = viewer;
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

		sourceViewer().contributeToStatusLine(getStatusLineManager());
	}

	public boolean isInEscapeSequence () {
	    return ((ClojureSourceViewer)getSourceViewer()).isInEscapeSequence();
	}

	public void toggleStructuralEditionMode() {
		sourceViewer().toggleStructuralEditionMode();
	}

    public DefaultCharacterPairMatcher getPairsMatcher() {
        return ((ClojureSourceViewer) getSourceViewer())==null ? null :
        	((ClojureSourceViewer) getSourceViewer())
        	.getPairsMatcher();
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        return getSourceViewer().getSelectionProvider();
    }

	@Override
	protected void createActions() {
		super.createActions();

		// @todo push many (if not most) of these into ClojureSourceViewer (somehow, that's SWT and actions are eclipse-land :-/)
		Action action;

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

        action = new SwitchNamespaceAction(this);
        action.setActionDefinitionId(IClojureEditorActionDefinitionIds.SWITCH_NS);
        setAction(SwitchNamespaceAction.ID, action);

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

		action = new Action() {
			@Override
			public void run() {
				new ClojureLaunchShortcut().launch(ClojureEditor.this, ILaunchManager.RUN_MODE);
			};
		};
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.LAUNCH_REPL);
		setAction("ClojureLaunchAction", action);

//		TODO: same for content-assist handler ? markAsStateDependentAction(CONTENT_ASSIST_PROPOSAL, true);

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
		setKeyBindingScopes(new String[] { IClojureEditor.KEY_BINDING_SCOPE });
	}

	@Override
	public void dispose() {
		if (getPairsMatcher() != null) {
			getPairsMatcher().dispose();
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

		if (ITextOperationTarget.class == required) {
			return sourceViewer();
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

	public String findDeclaringNamespace () {
		return ((ClojureSourceViewer)getSourceViewer()).findDeclaringNamespace();
	}

	public REPLView getCorrespondingREPL () {
		IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
		if (file != null) {
			return CCWPlugin.getDefault().getProjectREPL(file.getProject());
		} else {
			// Last resort : we return the current active REPL, if any
			return REPLView.activeREPL.get();
		}
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

    public Object getParseState() {
    	return sourceViewer().getParseState();
    }

    public Object getPreviousParseTree() {
    	return sourceViewer().getPreviousParseTree();
    }

	public void gotoMatchingBracket() {
		sourceViewer().gotoMatchingBracket();
	}

	public boolean isStructuralEditionPossible() {
		return sourceViewer().isStructuralEditionPossible();
	}

    public boolean isStructuralEditingEnabled() {
        return sourceViewer().isStructuralEditingEnabled();
    }
}
