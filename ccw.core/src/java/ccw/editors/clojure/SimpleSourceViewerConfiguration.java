/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *    Andrea RICHIARDI - refactoring out common functionality of the read-only SourceViewer 
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
import ccw.editors.clojure.scanners.ClojurePartitionScanner;
import ccw.editors.clojure.scanners.ClojureTokenScanner;
import ccw.editors.clojure.text.ClojureTopLevelFormsDamager;
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
    protected final IClojureEditor editor;
    
    public SimpleSourceViewerConfiguration(IPreferenceStore preferenceStore, IClojureEditor editor) {
        super(preferenceStore);
        this.editor = editor;
        
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
        
        ITokenScanner scanner = new ClojureTokenScanner(CCWPlugin.getDefault().getDefaultScanContext(),
                                                        CCWPlugin.getDefault().getCombinedPreferenceStore(),
                                                        editor);
        
        listeners.add((IPropertyChangeListener) scanner);
        scanners.add(scanner);
        
        return new Pair<Collection<ITokenScanner>, Collection<IPropertyChangeListener>>(
                Collections.unmodifiableCollection(scanners), Collections.unmodifiableCollection(listeners));
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        for (IPropertyChangeListener listener : propertyChangeListeners) {
          listener.propertyChange(event);
        }
    }
    
    protected void addDamagerRepairerForContentType(PresentationReconciler reconciler, String contentType) {
        IPresentationDamager d = new ClojureTopLevelFormsDamager(editor); 
        reconciler.setDamager(d, contentType);
        
        // AR - Each scanner has the Default Repairer at the moment
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
        return ClojurePartitionScanner.DEFAULT_CONTENT_TYPES;
    }

    @Override
    public int getTabWidth(ISourceViewer sourceViewer) {
        return 2;
    }

}
