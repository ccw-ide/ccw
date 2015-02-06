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
 *      clojure rule based part
 *******************************************************************************/
package ccw.editors.clojure;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
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
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;

import ccw.CCWPlugin;
import ccw.preferences.PreferenceConstants;
import ccw.util.ClojureInvoker;

public class ClojureSourceViewerConfiguration extends
		SimpleSourceViewerConfiguration {
	protected ITokenScanner tokenScanner;
	private final IClojureAwarePart part;

	private final ClojureInvoker proposalProcessor = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.clojure-proposal-processor");

	ClojureInvoker hoverSupportInvoker = ClojureInvoker.newInvoker(CCWPlugin.getDefault(),
            "ccw.editors.clojure.hover-support");

	public ClojureSourceViewerConfiguration(IPreferenceStore preferenceStore,
	        IClojureAwarePart part) {
		super(preferenceStore, part);
		this.part = part;
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
				(IContentAssistProcessor) proposalProcessor._("make-process", assistant), 
				IDocument.DEFAULT_CONTENT_TYPE);
		//assistant.setContentAssistProcessor(
		//		(IContentAssistProcessor) proposalProcessor._("make-process", part, assistant), 
		//		ClojurePartitionScanner.CLOJURE_COMMENT);
		//assistant.setContentAssistProcessor(
		//		(IContentAssistProcessor) proposalProcessor._("make-process", part, assistant), 
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

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		// cf. http://code.google.com/p/counterclockwise/issues/detail?id=5
		// Completely disable spellchecking until we can distinguish comments
		// and code, and spell check based on these partitions
		return null;
	}

	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		return (ITextHover) hoverSupportInvoker._("hover-instance", contentType, stateMask);
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
				new PareditAutoEditStrategy(part, fPreferenceStore),
			    new PareditAutoAdjustWhitespaceStrategy(part, fPreferenceStore)};
	}
	
	@Override
	protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map<String, IAdaptable> map = (Map<String, IAdaptable>) super.getHyperlinkDetectorTargets(sourceViewer);
		if (part instanceof IAdaptable)
		    map.put(IHyperlinkConstants.ClojureHyperlinkDetector_TARGET_ID, (IAdaptable)part);
		return map;
	}
	
}
