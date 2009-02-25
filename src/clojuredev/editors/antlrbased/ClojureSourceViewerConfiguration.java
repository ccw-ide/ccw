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
package clojuredev.editors.antlrbased;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import clojuredev.editors.rulesbased.ClojurePartitionScanner;


public class ClojureSourceViewerConfiguration extends TextSourceViewerConfiguration {
	private final ITokenScanner tokenScanner;
	private final AntlrBasedClojureEditor editor;

	public ClojureSourceViewerConfiguration(AntlrBasedClojureEditor editor) {
		tokenScanner = new ClojureTokenScannerFactory().create(JavaUI.getColorManager());
		this.editor = editor;
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler= new PresentationReconciler();

		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		addDamagerRepairerForContentType(reconciler, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}
	
	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return IDocumentExtension3.DEFAULT_PARTITIONING;
	}
	
	private void addDamagerRepairerForContentType(PresentationReconciler reconciler, String contentType) {
		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(tokenScanner) {
			@Override
			public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
				return partition;
			}
		};

		reconciler.setDamager(dr, contentType);
		reconciler.setRepairer(dr, contentType);	
	}
	
	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		return 2;
	}
	
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { 
				IDocument.DEFAULT_CONTENT_TYPE
		};
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
	    ContentAssistant assistant = new ContentAssistant();

	    assistant.setDocumentPartitioning(ClojurePartitionScanner.CLOJURE_PARTITIONING);
	    assistant.setContentAssistProcessor(new ClojureProposalProcessor(editor), IDocument.DEFAULT_CONTENT_TYPE);
	    assistant.setContentAssistProcessor(new ClojureProposalProcessor(editor), ClojurePartitionScanner.CLOJURE_COMMENT);
	    assistant.setContentAssistProcessor(new ClojureProposalProcessor(editor), ClojurePartitionScanner.CLOJURE_STRING);

	    assistant.enableAutoActivation(true);
	    assistant.setEmptyMessage("No completions available.");
	    assistant.enableAutoInsert(true);
	    assistant.setAutoActivationDelay(500);
//	    assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
	    assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
//	    assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
	    assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
	    assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		assistant.setContextInformationPopupBackground(sourceViewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		assistant.setContextInformationPopupForeground(sourceViewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		assistant.setContextSelectorBackground(sourceViewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		assistant.setContextSelectorForeground(sourceViewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_BLACK));

	    return assistant;

	}
	
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				DefaultInformationControl ret = new DefaultInformationControl(parent, new HTMLTextPresenter());
				ret.setBackgroundColor(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				ret.setForegroundColor(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				return ret;
			}
		};
	}
	
}
