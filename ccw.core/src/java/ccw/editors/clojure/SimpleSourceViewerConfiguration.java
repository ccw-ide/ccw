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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import ccw.CCWPlugin;

/**
 * Source viewer configuration for display only (Preview...).
 * It just initializes the token scanner.
 * @author Andrea Richiardi
 *
 */
public class SimpleSourceViewerConfiguration extends TextSourceViewerConfiguration {
    
    protected ITokenScanner tokenScanner;
    private final IClojureAwarePart part;
    
    public SimpleSourceViewerConfiguration(IPreferenceStore preferenceStore, IClojureAwarePart part) {
        super(preferenceStore);
        this.part = part;
        
        initTokenScanner();
    }

    protected void addDamagerRepairerForContentType(PresentationReconciler reconciler, String contentType) {
        IPresentationDamager d = new ClojureTopLevelFormsDamager(part); 
        reconciler.setDamager(d, contentType);
        
        IPresentationRepairer r = new DefaultDamagerRepairer(tokenScanner);
        reconciler.setRepairer(r, contentType);
    }
    
    @Override
    public IPresentationReconciler getPresentationReconciler(
            ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        addDamagerRepairerForContentType(reconciler, IDocument.DEFAULT_CONTENT_TYPE);
        
        return reconciler;
    }

    public void initTokenScanner() {
        tokenScanner = new ClojureTokenScanner(
                CCWPlugin.getDefault().getColorCache(), 
                CCWPlugin.getDefault()
                .getDefaultScanContext(), 
                CCWPlugin.getDefault().getCombinedPreferenceStore(),
                part);
    }
    
    @Override
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
        return IDocumentExtension3.DEFAULT_PARTITIONING;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] { IDocument.DEFAULT_CONTENT_TYPE };
    }
}
