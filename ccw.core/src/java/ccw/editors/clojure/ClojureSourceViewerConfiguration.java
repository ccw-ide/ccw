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
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
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
import ccw.preferences.PreferenceConstants;
import ccw.util.ClojureInvoker;

public class ClojureSourceViewerConfiguration extends
		TextSourceViewerConfiguration {
	protected ITokenScanner tokenScanner;
	private final IClojureEditor editor;

	private final ClojureInvoker proposalProcessor = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.clojure-proposal-processor");
	
	ClojureInvoker hoverSupportInvoker = ClojureInvoker.newInvoker(CCWPlugin.getDefault(),
            "ccw.editors.clojure.hover-support");
	
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
	public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		
		assistant.addCompletionListener(new ICompletionListener() {
			public void assistSessionStarted(ContentAssistEvent event) {
				((ClojureSourceViewer) sourceViewer).setContentAssistantActive(true);
			}
			public void assistSessionEnded(ContentAssistEvent event) {
				((ClojureSourceViewer) sourceViewer).setContentAssistantActive(false);
			}
			public void selectionChanged(ICompletionProposal proposal,
					boolean smartToggle) { }
		});

		assistant.setDocumentPartitioning(ClojurePartitionScanner.CLOJURE_PARTITIONING);
		assistant.setContentAssistProcessor(
				(IContentAssistProcessor) proposalProcessor._("make-process", editor, assistant), 
				IDocument.DEFAULT_CONTENT_TYPE);
		//assistant.setContentAssistProcessor(
		//		(IContentAssistProcessor) proposalProcessor._("make-process", editor, assistant), 
		//		ClojurePartitionScanner.CLOJURE_COMMENT);
		//assistant.setContentAssistProcessor(
		//		(IContentAssistProcessor) proposalProcessor._("make-process", editor, assistant), 
		//		ClojurePartitionScanner.CLOJURE_STRING);

		assistant.enableAutoActivation(this.fPreferenceStore.getBoolean(PreferenceConstants.EDITOR_CODE_COMPLETION_AUTO_ACTIVATE));
		assistant.setShowEmptyList(false);
		assistant.setEmptyMessage(
				"No completions available. You may want to start a REPL for the"
				+ " project holding this file to activate the code completion"
				+ " feature.");
		assistant.setStatusLineVisible(true);
		assistant.setStatusMessage("no current status message");

		assistant.enableAutoInsert(true);
		assistant.setAutoActivationDelay(0);
		assistant
				.setProposalPopupOrientation(IContentAssistant.PROPOSAL_STACKED);
		assistant
				.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		assistant
				.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		assistant.enableColoredLabels(true);
		
		return assistant;

	}

	public IInformationControlCreator getInformationControlCreator(
			ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, new HTMLTextPresenter());
			}
		};
	}

	public void initTokenScanner() {
		tokenScanner = new ClojureTokenScanner(
				CCWPlugin.getDefault().getColorCache(), 
				CCWPlugin.getDefault()
				.getDefaultScanContext(), 
				CCWPlugin.getDefault().getCombinedPreferenceStore(),
				editor);
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		// cf. http://code.google.com/p/counterclockwise/issues/detail?id=5
		// Completely disable spellchecking until we can distinguish comments
		// and code, and spell check based on these partitions
		return null;
	}

	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		return (ITextHover) hoverSupportInvoker._("create-hover-proxy", editor, contentType, stateMask);
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return getTextHover(sourceViewer, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
	}

	@Override
    public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
	    return (int[])hoverSupportInvoker._("configured-state-masks", sourceViewer, contentType);
    }

    @Override
	public IAutoEditStrategy[] getAutoEditStrategies(
			ISourceViewer sourceViewer, final String contentType) {
		
		return new IAutoEditStrategy[] { 
				new PareditAutoEditStrategy(editor, fPreferenceStore),
			    new PareditAutoAdjustWhitespaceStrategy(editor, fPreferenceStore)};
	}
	
	@Override
	protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map<String, IAdaptable> map = (Map<String, IAdaptable>) super.getHyperlinkDetectorTargets(sourceViewer);
		if (editor instanceof IAdaptable)
		    map.put(IHyperlinkConstants.ClojureHyperlinkDetector_TARGET_ID, (IAdaptable)editor);
		return map;
	}
	
}
