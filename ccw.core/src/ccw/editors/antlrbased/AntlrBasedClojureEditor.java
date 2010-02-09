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

import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.debug.ClojureClient;
import ccw.editors.antlrbased.formatting.FormatAction;
import ccw.editors.outline.ClojureOutlinePage;
import ccw.editors.rulesbased.ClojureDocumentProvider;
import ccw.editors.rulesbased.ClojurePartitionScanner;
import ccw.launching.ClojureLaunchShortcut;

public class AntlrBasedClojureEditor extends TextEditor {
    private static final String CONTENT_ASSIST_PROPOSAL = "ContentAssistProposal"; //$NON-NLS-1$
    public static final String ID = "ccw.antlrbasededitor"; //$NON-NLS-1$
    /** Preference key for matching brackets */
    // PreferenceConstants.EDITOR_MATCHING_BRACKETS;
    /** Preference key for matching brackets color */
    // PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;
    public final static char[] PAIRS = { '{', '}', '(', ')', '[', ']' };
    private DefaultCharacterPairMatcher pairsMatcher = new DefaultCharacterPairMatcher(PAIRS, ClojurePartitionScanner.CLOJURE_PARTITIONING) {
        /*
         * tries to match a pair be the cursor after or before a pair start/end
         * element
         */
        @Override
        public IRegion match(IDocument doc, int offset) {
            IRegion region = super.match(doc, offset);
            if (region == null && offset < doc.getLength() - 1) {
                return super.match(doc, offset + 1);
            } else {
                return region;
            }
        }
    };

    public DefaultCharacterPairMatcher getPairsMatcher() {
        return pairsMatcher;
    }

    /** The projection support */
    private ProjectionSupport fProjectionSupport;
    private ClojureOutlinePage outlinePage;
    private ITokenScanner tokenScanner;

    public ITokenScanner getTokenScanner() {
        return tokenScanner;
    }

    public AntlrBasedClojureEditor() {
        IPreferenceStore preferenceStore = createCombinedPreferenceStore();
        CCWPlugin.registerEditorColors(preferenceStore);
        ClojureSourceViewerConfiguration configuration = new ClojureSourceViewerConfiguration(preferenceStore, this);
        tokenScanner = configuration.tokenScanner;
        setSourceViewerConfiguration(configuration);
        setPreferenceStore(preferenceStore);
        setDocumentProvider(new ClojureDocumentProvider());
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
    }

    @Override
    protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        support.setCharacterPairMatcher(pairsMatcher);
        support.setMatchingCharacterPainterPreferenceKeys(PreferenceConstants.EDITOR_MATCHING_BRACKETS, PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);
        super.configureSourceViewerDecorationSupport(support);
    }

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        fAnnotationAccess = createAnnotationAccess();
        fOverviewRuler = createOverviewRuler(getSharedColors());
        // ISourceViewer viewer= new ProjectionViewer(parent, ruler,
        // getOverviewRuler(), isOverviewRulerVisible(), styles);
        ISourceViewer viewer = new ClojureSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, getPreferenceStore());
        // ensure decoration support has been created and configured.
        getSourceViewerDecorationSupport(viewer);
        viewer.getTextWidget().addCaretListener(new SameWordHighlightingCaretListener(this));
        return viewer;
    }

    /*
     * @see
     * org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        ClojureSourceViewer viewer = (ClojureSourceViewer) getSourceViewer();
        fProjectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        // TODO remove the 2 following lines ?
        fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
        fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
        fProjectionSupport.install();
        viewer.doOperation(ClojureSourceViewer.TOGGLE);
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        return getSourceViewer().getSelectionProvider();
    }

    /**
     * Create a preference store combined from the Clojure, the EditorsUI and
     * the PlatformUI preference stores to inherit all the default text editor
     * settings from the Eclipse preferences.
     * 
     * @return the combined preference store.
     */
    private IPreferenceStore createCombinedPreferenceStore() {
        List<IPreferenceStore> stores = new LinkedList<IPreferenceStore>();
        stores.add(CCWPlugin.getDefault().getPreferenceStore());
        stores.add(EditorsUI.getPreferenceStore());
        stores.add(PlatformUI.getPreferenceStore());
        return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
    }

    @Override
    protected void createActions() {
        super.createActions();
        Action action = new GotoMatchingBracketAction(this);
        action.setActionDefinitionId(IClojureEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
        setAction(GotoMatchingBracketAction.ID, action);
        action = new OutwardExpandingSelectAction(this);
        action.setActionDefinitionId(IClojureEditorActionDefinitionIds.SELECT_TO_MATCHING_BRACKET);
        setAction(OutwardExpandingSelectAction.ID, action);
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
        action = new RunTestsAction(this);
        action.setActionDefinitionId(IClojureEditorActionDefinitionIds.RUN_TESTS);
        setAction(RunTestsAction.RUN_TESTS_ID, action);
        action = new CompileLibAction(this);
        action.setActionDefinitionId(IClojureEditorActionDefinitionIds.COMPILE_LIB);
        setAction(CompileLibAction.ID, action);
        action = new FormatAction(this);
        action.setActionDefinitionId(IClojureEditorActionDefinitionIds.FORMAT_CODE);
        setAction(FormatAction.ID, action);
        action = new Action() {
            public void run() {
                new ClojureLaunchShortcut().launch(AntlrBasedClojureEditor.this, ILaunchManager.RUN_MODE);
            };
        };
        action.setActionDefinitionId(IClojureEditorActionDefinitionIds.LAUNCH_REPL);
        setAction("ClojureLaunchAction", action);
        action = new ContentAssistAction(ClojureEditorMessages.getBundleForConstructedKeys(), CONTENT_ASSIST_PROPOSAL + ".", this); //$NON-NLS-1$
        String id = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
        action.setActionDefinitionId(id);
        setAction(CONTENT_ASSIST_PROPOSAL, action);
        markAsStateDependentAction(CONTENT_ASSIST_PROPOSAL, true);
    }

    /**
     * Jumps to the matching bracket.
     */
    public void gotoMatchingBracket() {
        ISourceViewer sourceViewer = getSourceViewer();
        IDocument document = sourceViewer.getDocument();
        if (document == null) {
            return;
        }
        IRegion selection = getSignedSelection(sourceViewer);
        int selectionLength = Math.abs(selection.getLength());
        if (selectionLength > 1) {
            setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracket_error_invalidSelection);
            sourceViewer.getTextWidget().getDisplay().beep();
            return;
        }
        // // #26314
        int sourceCaretOffset = selection.getOffset() + selection.getLength();
        // From JavaEditor, but I don't understand what it does so I maintain it
        // commented out
        // if (isSurroundedByBrackets(document, sourceCaretOffset))
        // sourceCaretOffset -= selection.getLength();
        //
        IRegion region = pairsMatcher.match(document, sourceCaretOffset);
        if (region == null) {
            setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracket_error_noMatchingBracket);
            sourceViewer.getTextWidget().getDisplay().beep();
            return;
        }
        int offset = region.getOffset();
        int length = region.getLength();
        if (length < 1) {
            return;
        }
        int anchor = pairsMatcher.getAnchor();
        // http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
        int targetOffset = ICharacterPairMatcher.RIGHT == anchor ? offset + 1 : offset + length;
        boolean visible = false;
        if (sourceViewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
            visible = extension.modelOffset2WidgetOffset(targetOffset) > -1;
        } else {
            IRegion visibleRegion = sourceViewer.getVisibleRegion();
            // http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
            visible = targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength();
        }
        if (!visible) {
            setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracket_error_bracketOutsideSelectedElement);
            sourceViewer.getTextWidget().getDisplay().beep();
            return;
        }
        if (selection.getLength() < 0) {
            targetOffset -= selection.getLength();
        }
        sourceViewer.setSelectedRange(targetOffset, selection.getLength());
        sourceViewer.revealRange(targetOffset, selection.getLength());
    }

    /**
     * Move to beginning of current or preceding defun (beginning-of-defun).
     */
    /*
     * place 0 in variable currentLevel, and 0 in variable highest level and -1
     * in variable highest level open paren repeat until found beginning of
     * file: move backward until you find a left or right paren (or attain the
     * beginning of the file) update currentChar position with the position of
     * the paren if open paren increment currentLevel. if currentLevel > highest
     * level highest level <- currentLevel highest level open paren <-
     * currentChar position else if close paren decrement currentLevel if
     * beginning of file if highest level still 0 : no solution else return
     * highest level open paren
     * 
     * Note: the found paren must be in the correct partition (code, not string
     * or comment)
     */
    public void gotoPreviousMember() {
        if (!checkSelectionAndWarnUserIfProblem(ClojureEditorMessages.GotoMatchingBracket_error_invalidSelection)) {
            return;
        }
        int sourceCaretOffset = getSourceCaretOffset();
        int previousMemberOffset = getBeginningOfCurrentOrPrecedingTopLevelSExpressionFor(sourceCaretOffset);
        if (previousMemberOffset >= 0) {
            selectAndReveal(previousMemberOffset, 0);
        }
    }

    private boolean checkSelectionAndWarnUserIfProblem(String errorMessageIfProblem) {
        if (getDocument() == null) {
            return false;
        }
        IRegion selection = getSignedSelection(getSourceViewer());
        int selectionLength = Math.abs(selection.getLength());
        if (selectionLength > 0) {
            setStatusLineErrorMessage(errorMessageIfProblem);
            getSourceViewer().getTextWidget().getDisplay().beep();
            return false;
        }
        return true;
    }

    public IDocument getDocument() {
        ISourceViewer sourceViewer = getSourceViewer();
        return sourceViewer.getDocument();
    }

    /**
     * Asserts document != null.
     * 
     * @return
     */
    private int getSourceCaretOffset() {
        IRegion selection = getSignedSelection(getSourceViewer());
        return selection.getOffset() + selection.getLength();
    }

    private int getBeginningOfCurrentOrPrecedingTopLevelSExpressionFor(final int sourceCaretOffset) {
        IDocument document = getDocument();
        int currentLevel = 0;
        int highestLevel = 0;
        int highestLevelCaretOffset = -1;
        int nextParenOffset = sourceCaretOffset - 1;
        try {
            while (nextParenOffset >= 0) {
                nextParenOffset = nextCharInContentTypeMatching(nextParenOffset, IDocument.DEFAULT_CONTENT_TYPE, new char[] { '(', ')' }, false);
                if (nextParenOffset == -1) {
                    break;
                }
                if (document.getChar(nextParenOffset) == '(') {
                    currentLevel += 1;
                } else if (document.getChar(nextParenOffset) == ')') {
                    currentLevel -= 1;
                }
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
        if (r != null) {
            selectAndReveal(r.getOffset(), r.getLength());
        }
    }

    private IRegion getTopLevelSExpression() {
        if (!checkSelectionAndWarnUserIfProblem(ClojureEditorMessages.GotoMatchingBracket_error_invalidSelection)) {
            return null;
        }
        int sourceCaretOffset = getSourceCaretOffset();
        int endOffset = getEndOfCurrentOrNextTopLevelSExpressionFor(sourceCaretOffset);
        int beginningOffset = getBeginningOfCurrentOrPrecedingTopLevelSExpressionFor(endOffset);
        if (beginningOffset >= 0 && endOffset >= 0) {
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
        if (!checkSelectionAndWarnUserIfProblem(ClojureEditorMessages.GotoMatchingBracket_error_invalidSelection)) {
            return;
        }
        int sourceCaretOffset = getSourceCaretOffset();
        int endOfMemberOffset = getEndOfCurrentOrNextTopLevelSExpressionFor(sourceCaretOffset);
        if (endOfMemberOffset >= 0) {
            selectAndReveal(endOfMemberOffset, 0);
        }
    }

    private int getEndOfCurrentOrNextTopLevelSExpressionFor(int sourceCaretOffset) {
        ISourceViewer sourceViewer = getSourceViewer();
        IDocument document = sourceViewer.getDocument();
        int currentLevel = 0;
        int highestLevel = 0;
        int highestLevelCaretOffset = -1;
        int nextParenOffset = sourceCaretOffset;
        try {
            while (nextParenOffset < document.getLength()) {
                nextParenOffset = nextCharInContentTypeMatching(nextParenOffset, IDocument.DEFAULT_CONTENT_TYPE, new char[] { '(', ')' }, true);
                if (nextParenOffset == -1) {
                    break;
                }
                if (document.getChar(nextParenOffset) == '(') {
                    currentLevel -= 1;
                } else if (document.getChar(nextParenOffset) == ')') {
                    currentLevel += 1;
                }
                if (currentLevel > highestLevel) {
                    highestLevel = currentLevel;
                    highestLevelCaretOffset = nextParenOffset;
                } else if (currentLevel == 0 && highestLevelCaretOffset == -1) {
                    highestLevelCaretOffset = nextParenOffset;
                }
                nextParenOffset++;
            }
            if (highestLevelCaretOffset >= 0) {
                if (highestLevelCaretOffset + 1 <= document.getLength()) {
                    highestLevelCaretOffset++;
                }
            }
        } catch (BadLocationException e) {
        }
        return highestLevelCaretOffset;
    }

    private int nextCharInContentTypeMatching(int currentOffset, String contentType, char[] charsToMatch, boolean searchForward) throws BadLocationException {
        ISourceViewer sourceViewer = getSourceViewer();
        IDocument document = sourceViewer.getDocument();
        int offset = currentOffset;
        while (!(TextUtilities.getContentType(document, ClojurePartitionScanner.CLOJURE_PARTITIONING, offset, false).equals(contentType) && matchChar(document.getChar(offset), charsToMatch))) {
            if (searchForward) {
                offset++;
                if (offset >= document.getLength()) {
                    return -1;
                }
            } else {
                offset--;
                if (offset < 0) {
                    return -1;
                }
            }
        }
        return offset;
    }

    private boolean matchChar(char c, char[] charsToMatch) {
        for (char ctm : charsToMatch) {
            if (c == ctm) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the signed current selection. The length will be negative if the
     * resulting selection is right-to-left (RtoL).
     * <p>
     * The selection offset is model based.
     * </p>
     * 
     * @param sourceViewer
     *            the source viewer
     * @return a region denoting the current signed selection, for a resulting
     *         RtoL selections length is < 0
     */
    public IRegion getSignedSelection(ISourceViewer sourceViewer) {
        StyledText text = sourceViewer.getTextWidget();
        Point selection = text.getSelectionRange();
        if (text.getCaretOffset() == selection.x) {
            selection.x = selection.x + selection.y;
            selection.y = -selection.y;
        }
        selection.x = widgetOffset2ModelOffset(sourceViewer, selection.x);
        return new Region(selection.x, selection.y);
    }

    /**
     * Returns the unsigned current selection. The length will always be
     * positive.
     * <p>
     * The selection offset is model based.
     * </p>
     * 
     * @param sourceViewer
     *            the source viewer
     * @return a region denoting the current unsigned selection
     */
    protected IRegion getUnSignedSelection(ISourceViewer sourceViewer) {
        StyledText text = sourceViewer.getTextWidget();
        Point selection = text.getSelectionRange();
        selection.x = widgetOffset2ModelOffset(sourceViewer, selection.x);
        return new Region(selection.x, selection.y);
    }

    /*
     * @seeorg.eclipse.ui.texteditor.AbstractDecoratedTextEditor#
     * initializeKeyBindingScopes()
     */
    @Override
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] { "ccw.ui.clojureEditorScope" }); //$NON-NLS-1$
    }

    @Override
    public void dispose() {
        if (pairsMatcher != null) {
            pairsMatcher.dispose();
            pairsMatcher = null;
        }
        super.dispose();
    }

    /**
     * The <code>JavaEditor</code> implementation of this
     * <code>AbstractTextEditor</code> method performs gets the java content
     * outline page if request is for a an outline page.
     * 
     * @param required
     *            the required type
     * @return an adapter for the required type or <code>null</code>
     */
    @Override
    public Object getAdapter(Class required) {
        if (IContentOutlinePage.class.equals(required)) {
            if (outlinePage == null) {
                outlinePage = new ClojureOutlinePage(getDocumentProvider(), this);
                if (getEditorInput() != null) {
                    outlinePage.setInput(getEditorInput());
                }
            }
            return outlinePage;
        }
        if (fProjectionSupport != null) {
            Object adapter = fProjectionSupport.getAdapter(getSourceViewer(), required);
            if (adapter != null) {
                return adapter;
            }
        }
        return super.getAdapter(required);
    }

    public String getSelectedText() {
        IRegion r = getUnSignedSelection(getSourceViewer());
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

    public String getDeclaringNamespace() {
        return ClojureCore.getDeclaringNamespace(getDocument().get());
    }

    public ClojureClient getCorrespondingClojureClient() {
        IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
        if (file != null) {
            return CCWPlugin.getDefault().getProjectClojureClient(file.getProject());
        } else {
            return null;
        }
    }

    /*
     * @see
     * org.eclipse.ui.texteditor.AbstractTextEditor#setPreferenceStore(org.eclipse
     * .jface.preference.IPreferenceStore)
     * 
     * @since 3.0
     */
    @Override
    protected void setPreferenceStore(IPreferenceStore store) {
        super.setPreferenceStore(store);
        if (getSourceViewer() instanceof ClojureSourceViewer) {
            ((ClojureSourceViewer) getSourceViewer()).setPreferenceStore(store);
        }
    }

    public final ISourceViewer sourceViewer() {
        return super.getSourceViewer();
    }

    @Override
    public void setStatusLineErrorMessage(String message) {
        super.setStatusLineMessage(message);
    }
}
