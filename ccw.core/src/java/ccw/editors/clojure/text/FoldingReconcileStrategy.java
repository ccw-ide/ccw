/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Andrea RICHIARDI - Initial creation
 *******************************************************************************/
package ccw.editors.clojure.text;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

import ccw.CCWPlugin;
import ccw.TraceOptions;
import ccw.editors.clojure.IClojureEditor;
import ccw.util.ClojureInvoker;

/**
 * Clojure folding reconciling strategy
 */
public class FoldingReconcileStrategy  implements IReconcilingStrategy, IReconcilingStrategyExtension {

    private final ClojureInvoker foldingSupport = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.folding-support");
    
    /** The Clojure editor */
    private IClojureEditor editor;
    
    /** Document (mutable) */
    private IDocument document;
    
    /** Progress monitor (mutable) */
    private IProgressMonitor progressMonitor;
    
    public FoldingReconcileStrategy(IClojureEditor editor) {
        this.editor = editor;
    }

    @Override
    public void setProgressMonitor(IProgressMonitor monitor) {
        progressMonitor = monitor;
    }

    @Override
    public void initialReconcile() {
        CCWPlugin.getTracer().trace(TraceOptions.EDITOR_TEXT, "Starting initialReconcile...");
        foldingSupport._("compute-folding!", editor);
    }

    @Override
    public void setDocument(IDocument document) {
        this.document = document;
    }

    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        throw new RuntimeException("Incremental reconciler not implemented yet");
    }

    @Override
    public void reconcile(IRegion partition) {
        CCWPlugin.getTracer().trace(TraceOptions.EDITOR_TEXT, "Reconcile partition: " + partition);
        foldingSupport._("compute-folding!", editor);
    }
}
