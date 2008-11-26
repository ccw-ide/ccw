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
package clojuredev.editors.antlrbased;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import clojuredev.ClojuredevPlugin;

public class AntlrBasedClojureEditor extends TextEditor {
	/** Preference key for matching brackets */
	//PreferenceConstants.EDITOR_MATCHING_BRACKETS;
	
	/** Preference key for matching brackets color */
	//PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;

	protected final static char[] PAIRS= { '{', '}', '(', ')', '[', ']' };
	
	private DefaultCharacterPairMatcher pairsMatcher = new DefaultCharacterPairMatcher(PAIRS,ClojurePartitionScannerFactory.CLOJURE_CODE);

	/** The projection support */
	private ProjectionSupport fProjectionSupport;

	public AntlrBasedClojureEditor() {
        setDocumentProvider(new FileDocumentProvider());
		setSourceViewerConfiguration(new ClojureSourceViewerConfiguration());
		setPreferenceStore(ClojuredevPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setDocumentProvider(new FileDocumentProvider());
		setSourceViewerConfiguration(new ClojureSourceViewerConfiguration());
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
		
		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());
		
		ISourceViewer viewer= new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		
		return viewer;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();
		fProjectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		
		// TODO remove the 2 following lines ?
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		
		fProjectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		
		Action action= new GotoMatchingBracketAction(this);
		action.setActionDefinitionId(IClojureEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
		setAction(GotoMatchingBracketAction.ID, action);
	}
	
	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {
		ISourceViewer sourceViewer= getSourceViewer();
		IDocument document= sourceViewer.getDocument();
		if (document == null)
			return;

		IRegion selection= getSignedSelection(sourceViewer);

		int selectionLength= Math.abs(selection.getLength());
		if (selectionLength > 1) {
			setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracket_error_invalidSelection);
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
			setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracket_error_noMatchingBracket);
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
			setStatusLineErrorMessage(ClojureEditorMessages.GotoMatchingBracket_error_bracketOutsideSelectedElement);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		if (selection.getLength() < 0)
			targetOffset -= selection.getLength();

		sourceViewer.setSelectedRange(targetOffset, selection.getLength());
		sourceViewer.revealRange(targetOffset, selection.getLength());
	}

	/**
	 * Returns the signed current selection.
	 * The length will be negative if the resulting selection
	 * is right-to-left (RtoL).
	 * <p>
	 * The selection offset is model based.
	 * </p>
	 *
	 * @param sourceViewer the source viewer
	 * @return a region denoting the current signed selection, for a resulting RtoL selections length is < 0
	 */
	protected IRegion getSignedSelection(ISourceViewer sourceViewer) {
		StyledText text= sourceViewer.getTextWidget();
		Point selection= text.getSelectionRange();

		if (text.getCaretOffset() == selection.x) {
			selection.x= selection.x + selection.y;
			selection.y= -selection.y;
		}

		selection.x= widgetOffset2ModelOffset(sourceViewer, selection.x);

		return new Region(selection.x, selection.y);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "clojuredev.ui.clojureEditorScope" });  //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		if (pairsMatcher != null) {
			pairsMatcher.dispose();
			pairsMatcher = null;
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
	public Object getAdapter(Class required) {
//		if (IContentOutlinePage.class.equals(required)) {
//			if (fOutlinePage == null) {
//				fOutlinePage= new JavaContentOutlinePage(getDocumentProvider(), this);
//				if (getEditorInput() != null)
//					fOutlinePage.setInput(getEditorInput());
//			}
//			return fOutlinePage;
//		}
		
		if (fProjectionSupport != null) {
			Object adapter= fProjectionSupport.getAdapter(getSourceViewer(), required);
			if (adapter != null)
				return adapter;
		}
		
		return super.getAdapter(required);
	}}
