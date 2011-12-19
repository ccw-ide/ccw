/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent PETIT - initial API and implementation
 *    Some code (e.g. contextInformation wiring) from Scala plugin & original
 *      clojure rule based editor
 *******************************************************************************/
package ccw.editors.clojure;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import ccw.CCWPlugin;
import ccw.editors.clojure.PareditAutoEditStrategy;

public class ClojureSourceViewerConfiguration extends
		TextSourceViewerConfiguration {
	protected ITokenScanner tokenScanner;
	private final IClojureEditor editor;

	public ClojureSourceViewerConfiguration(IPreferenceStore preferenceStore,
			IClojureEditor editor) {
		super(preferenceStore);
		this.editor = editor;
		initTokenScanner();
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		reconciler
				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		addDamagerRepairerForContentType(reconciler,
				IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return IDocumentExtension3.DEFAULT_PARTITIONING;
	}

	private void addDamagerRepairerForContentType(
			PresentationReconciler reconciler, String contentType) {

		IPresentationDamager d = new ClojureTopLevelFormsDamager(editor);
		reconciler.setDamager(d, contentType);

		IPresentationRepairer r = new DefaultDamagerRepairer(tokenScanner);
		reconciler.setRepairer(r, contentType);

	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE };
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();

		assistant.setDocumentPartitioning(ClojurePartitionScanner.CLOJURE_PARTITIONING);
		assistant.setContentAssistProcessor(new ClojureProposalProcessor(
				editor, assistant), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(new ClojureProposalProcessor(
				editor, assistant), ClojurePartitionScanner.CLOJURE_COMMENT);
		assistant.setContentAssistProcessor(new ClojureProposalProcessor(
				editor, assistant), ClojurePartitionScanner.CLOJURE_STRING);

		assistant.enableAutoActivation(false);
		assistant.setShowEmptyList(true);
		assistant
				.setEmptyMessage("No completions available. You may want to start a REPL for the project holding this file to activate the code completion feature.");
		assistant.setStatusLineVisible(true);
		assistant.setStatusMessage("no current status mesage");

		assistant.enableAutoInsert(true);
		assistant.setAutoActivationDelay(500);
		// assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		assistant
				.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		// assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		assistant
				.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant
				.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		assistant.enableColoredLabels(true);

		return assistant;

	}

	public IInformationControlCreator getInformationControlCreator(
			ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent,
						new HTMLTextPresenter());
			}
		};
	}

	public void initTokenScanner() {
		tokenScanner = new ClojureTokenScannerFactory().create(CCWPlugin
				.getDefault().getColorRegistry(), CCWPlugin.getDefault()
				.getDefaultScanContext(), editor);
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		// cf. http://code.google.com/p/counterclockwise/issues/detail?id=5
		// Completely disable spellchecking until we can distinguish comments
		// and code, and spell check based on these partitions
		return null;
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(
			ISourceViewer sourceViewer, final String contentType) {

		return new IAutoEditStrategy[] {
				new PareditAutoEditStrategy(editor, fPreferenceStore) };
	}

	@Override
	protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map<String, IAdaptable> map = (Map<String, IAdaptable>) super.getHyperlinkDetectorTargets(sourceViewer);
		if (editor instanceof IAdaptable)
		    map.put(IHyperlinkConstants.ClojureHyperlinkDetector_TARGET_ID, (IAdaptable)editor);
		return map;
	}

}
