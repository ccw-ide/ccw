/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *    Andrea Richiardi - abstraction & interface refactoring
 *******************************************************************************/
package ccw.editors.clojure;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.StatusLineContributionItem;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ccw.CCWPlugin;
import ccw.editors.clojure.scanners.ClojurePartitionScanner;
import ccw.editors.outline.ClojureOutlinePage;
import ccw.launching.ClojureLaunchShortcut;
import ccw.preferences.PreferenceConstants;
import ccw.repl.REPLView;
import ccw.repl.SafeConnection;
import ccw.util.ClojureInvoker;
import ccw.util.StringUtils;

public class ClojureEditor extends TextEditor implements IClojureEditor {
	/**
	 * Shortens a namespace name,
	 * e.g. net.cgrand.parsley.core => n.c.parsley.core.
	 * @param namespace
	 * @return
	 */
	private String shortenNamespace(String namespace) {
		String[] segments = namespace.split("\\.");
		int nextToLast = segments.length - 2;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nextToLast; i++) {
			sb.append(segments[i].charAt(0));
			sb.append('.');
		}
		if (nextToLast >= 0) {
			sb.append(segments[nextToLast]);
			sb.append('.');
		}
		sb.append(segments[nextToLast + 1]);
		return sb.toString();
	}
	
	public void updatePartNameAndDescription() {
		String partName = getEditorInput().getName();
		String contentDescription = "";

		final String maybeNamespace = this.findDeclaringNamespace();
		if (!StringUtils.isEmpty(maybeNamespace)) {
			if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_DISPLAY_NAMESPACE_IN_TABS)) {
				partName = shortenNamespace(maybeNamespace);
			}
			contentDescription = String.format("Namespace %s", maybeNamespace);
		}
		
		this.setPartName(partName);
		this.setContentDescription(contentDescription);
	}
	
	private final ClojureInvoker editorSupport = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.editor-support");

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
        setDocumentProvider(new ClojureDocumentProvider()); 
        setHelpContextId(EDITOR_REFERENCE_HELP_CONTEXT_ID);
        addPropertyListener(new IPropertyListener() {
			@Override
			public void propertyChanged(Object source, int propId) {
				if (propId == IEditorPart.PROP_INPUT) {
					updatePartNameAndDescription();
					outlinePage.setInput(getEditorInput());
				}
			}
		});
	}
	
	/**
	 * Creates the custom ClojureSourceViewer.
	 * @param parent Composite parent
	 * @param ruler
	 * @param styles
	 * @return
	 */
	private ClojureSourceViewer createClojureSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
	    
	    ClojureSourceViewer viewer= new ClojureSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, getPreferenceStore(),
                new ClojureSourceViewer.IStatusLineHandler() {
                    public StatusLineContributionItem getEditingModeStatusContributionItem() {
                        return (StatusLineContributionItem) ClojureEditor.this.getStatusField(ClojureSourceViewer.STATUS_CATEGORY_STRUCTURAL_EDITION);
                    }
                }) {
            
            @Override
            public void setStatusLineErrorMessage(String message) {
                ClojureEditor.this.setStatusLineErrorMessage(message);
            }

            @Override
            public @Nullable REPLView getCorrespondingREPL() {
             // Experiment: always return the active REPL instead of a potentially
                //             better match being a REPL started from same project as the file
//              IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
//              if (file != null) {
//                  REPLView repl = CCWPlugin.getDefault().getProjectREPL(file.getProject());
//                  if (repl !=  null) {
//                      return repl;
//                  }
//              }
//              // Last resort : we return the current active REPL, if any
                return REPLView.activeREPL.get();
            }

            @Override
            public @Nullable SafeConnection getSafeToolingConnection() {
                return REPLView.activeREPL.get().getSafeToolingConnection();
            }
        };
        
        return viewer;
	}
	
	private ClojureSourceViewerConfiguration createClojureSourceViewerConfiguration() {
	    return new ClojureSourceViewerConfiguration(getPreferenceStore(), this);
	}
	
	@Override
	protected void initializeViewerColors(ISourceViewer viewer) {
	    sourceViewer().initializeViewerColors();
	}
	
	@Override
    protected void setSourceViewerConfiguration(SourceViewerConfiguration configuration) {
        super.setSourceViewerConfiguration(createClojureSourceViewerConfiguration());
    }

	
    @Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());
		
		ClojureSourceViewer viewer = createClojureSourceViewer(parent, ruler, styles);
        if (viewer instanceof ProjectionViewer) {
            /*
             * Need to hook up here to force a re-evaluation of the preferences
             * for the syntax coloring, after the token scanner has been
             * initialized. Otherwise the very first Clojure editor will not
             * have any tokens colored.
             * TODO this is repeated in ClojureEditor...surely we can make the source viewer self-sufficient here
             *
             * AR - Solved by initializing the ClojureSourceViewerConfiguration at the very
             * beginning of the ClojureSourceViewer
             */
//            viewer.propertyChange(null);
            
            fProjectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());

            // TODO remove the 2 following lines ?
            fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
            fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$

            fProjectionSupport.install();
            
            // TODO Add the hovers on the Projection?
//          fProjectionSupport.setHoverControlCreator(new IInformationControlCreator() {
//                public IInformationControl createInformationControl(Shell shell) {
//                    return new SourceViewerInformationControl(shell, false, getOrientation(), EditorsUI.getTooltipAffordanceString());
//                }
//            });
//            fProjectionSupport.setInformationPresenterControlCreator(new IInformationControlCreator() {
//                public IInformationControl createInformationControl(Shell shell) {
//                    return new SourceViewerInformationControl(shell, true, getOrientation(), null);
//                }
//            });
        }
        
        viewer.doOperation(ClojureSourceViewer.TOGGLE);

		// ensure decoration support has been created and configured.
		SourceViewerDecorationSupport sourceViewerDecorationSupport = getSourceViewerDecorationSupport(viewer);
		editorSupport._("configureSourceViewerDecorationSupport", sourceViewerDecorationSupport, viewer);
		
		return viewer;
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent); // AR - the doc say to configure and then call this
		
		final IPropertyChangeListener prefsListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(PreferenceConstants.EDITOR_DISPLAY_NAMESPACE_IN_TABS)) {
					updatePartNameAndDescription();
				}
			}
		};
		getPreferenceStore().addPropertyChangeListener(prefsListener);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				getPreferenceStore().removePropertyChangeListener(prefsListener);
			}
		});
		
		// AR - This goes after because I am sure now that the SourceViewer
		// is correctly setup (is there another way to know it?) 
		updatePartNameAndDescription();
	}
	
	@Override
	protected void editorSaved() {
		super.editorSaved();
		updatePartNameAndDescription();
		editorSupport._("editor-saved", this);
	}
	
	/**
	 * Updates the status fields for the given category.
	 *
	 * @param category the category
	 * @since 2.0
	 */
	protected void updateStatusField(String category) {
		if (ClojureSourceViewer.STATUS_CATEGORY_STRUCTURAL_EDITION.equals(category)) {
		    IClojureSourceViewer sourceViewer = (IClojureSourceViewer) getSourceViewer();
            if (sourceViewer instanceof IClojureSourceViewer) {
                ((ClojureSourceViewer) sourceViewer).updateStructuralEditingModeStatusField();
            }
		} else {
			super.updateStatusField(category);
		}
	}

	
	public boolean isInEscapeSequence () {
	    return ((IClojureSourceViewer)getSourceViewer()).isInEscapeSequence();
	}
	
	public void toggleStructuralEditionMode() {
		sourceViewer().toggleStructuralEditionMode();
	}
	
    public DefaultCharacterPairMatcher getPairsMatcher() {
        IClojureSourceViewer isv = (IClojureSourceViewer)getSourceViewer();
        return isv ==null ? null : isv.getPairsMatcher();
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

		action = new RunTestsAction(this, CCWPlugin.getDefault().getColorCache());
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
				// is this code dead?
				new ClojureLaunchShortcut().launch(
						ClojureEditor.this, 
						null /* default run mode*/);
			};
		};
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.LAUNCH_REPL);
		setAction("ClojureLaunchAction", action);

//		TODO: same for content-assist handler ? markAsStateDependentAction(CONTENT_ASSIST_PROPOSAL, true);
		
		action = new Action() {
			@Override
			public void run() {
			    IClojureSourceViewer sourceViewer = (IClojureSourceViewer) getSourceViewer();
			    if (sourceViewer instanceof IClojureSourceViewer) {
			        ((ClojureSourceViewer) sourceViewer).updateStructuralEditingModeStatusField();
			    }
			}
		};
		action.setActionDefinitionId(ClojureSourceViewer.STATUS_CATEGORY_STRUCTURAL_EDITION);
		setAction(ClojureSourceViewer.STATUS_CATEGORY_STRUCTURAL_EDITION, action);
		
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

	public String getCurrentTopLevelSExpression() {
		return (String) editorSupport._("top-level-code-form", getParseState(), getSourceCaretOffset());
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
	    return sourceViewer().getUnSignedSelection();
	}
	
	public IRegion getSignedSelection () {
	    return sourceViewer().getSignedSelection();
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope", IClojureEditor.KEY_BINDING_SCOPE });  //$NON-NLS-1$
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
		return sourceViewer().findDeclaringNamespace();
	}
	
	public REPLView getCorrespondingREPL () {
		return sourceViewer().getCorrespondingREPL();
	}
	
	@Override
    @Nullable
    public SafeConnection getSafeToolingConnection() {
        return sourceViewer().getSafeToolingConnection();
    }

    @Override
    public final @NonNull IClojureSourceViewer sourceViewer() {
        return (IClojureSourceViewer) super.getSourceViewer();
    }

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

    /* (non-Javadoc)
     * @see ccw.editors.clojure.IClojureSourceViewer#isForceRepair()
     */
	public boolean isForceRepair() {
		return sourceViewer().isForceRepair();
	}

    @Override
    public boolean isShowRainbowParens() {
    	return sourceViewer().isShowRainbowParens();
    }
    
    public void toggleShowRainbowParens() {
    	sourceViewer().toggleShowRainbowParens();
    }

	public void markDamagedAndRedraw() {
		sourceViewer().markDamagedAndRedraw();
	}

	public boolean isEscapeInStringLiteralsEnabled() {
		return sourceViewer().isEscapeInStringLiteralsEnabled();
	}
	
}
