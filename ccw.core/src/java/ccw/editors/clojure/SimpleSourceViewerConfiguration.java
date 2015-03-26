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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import ccw.CCWPlugin;
import ccw.editors.clojure.scanners.ClojureTokenScanner;
import ccw.util.Pair;

/**
 * Source viewer configuration for display only (Preview...).
 * It just initializes the token scanner.
 * @author Andrea Richiardi
 *
 */
public class SimpleSourceViewerConfiguration extends TextSourceViewerConfiguration implements IPropertyChangeListener {
    
    protected final Collection<IPropertyChangeListener> propertyChangeListeners;
    protected final Collection<ITokenScanner> tokenScanners;
    protected final IClojureAwarePart part;
    
    public SimpleSourceViewerConfiguration(IPreferenceStore preferenceStore, IClojureAwarePart part) {
        super(preferenceStore);
        this.part = part;
        
        Pair<Collection<ITokenScanner>, Collection<IPropertyChangeListener>> scannerPair = initScanners();
        tokenScanners = scannerPair.e1;
        propertyChangeListeners = scannerPair.e2;
    }

    /**
     * Java is so verbose.
     * @return
     */
    private Pair<Collection<ITokenScanner>, Collection<IPropertyChangeListener>> initScanners() {
        Collection<IPropertyChangeListener> listeners = new ArrayList<>();
        Collection<ITokenScanner> scanners = new ArrayList<>();
        
        ITokenScanner scanner = new ClojureTokenScanner(CCWPlugin.getDefault().getColorCache(),
                CCWPlugin.getDefault().getDefaultScanContext(),
                CCWPlugin.getDefault().getCombinedPreferenceStore(),
                part);
        
        listeners.add((IPropertyChangeListener) scanner);
        scanners.add(scanner);
        
        return new Pair(Collections.unmodifiableCollection(scanners), Collections.unmodifiableCollection(listeners));
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        for (IPropertyChangeListener listener : propertyChangeListeners) {
          listener.propertyChange(event);
        }
    }
    
    /** 
     * Fire the event when the year value change
     * @param e
     */
    protected void firePropertyListener(PropertyChangeEvent e){
      
    }
    
    protected void addDamagerRepairerForContentType(PresentationReconciler reconciler, String contentType) {
        IPresentationDamager d = new ClojureTopLevelFormsDamager(part); 
        reconciler.setDamager(d, contentType);
        
        for (ITokenScanner scanner : tokenScanners) {
            IPresentationRepairer r = new DefaultDamagerRepairer(scanner);
            reconciler.setRepairer(r, contentType);
        }
    }
    
    @Override
    public IPresentationReconciler getPresentationReconciler(
            ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        addDamagerRepairerForContentType(reconciler, IDocument.DEFAULT_CONTENT_TYPE);
        
        return reconciler;
    }

    @Override
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
        return IDocumentExtension3.DEFAULT_PARTITIONING;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] { IDocument.DEFAULT_CONTENT_TYPE };
    }

    @Override
    public int getTabWidth(ISourceViewer sourceViewer) {
        return 2;
    }

}
