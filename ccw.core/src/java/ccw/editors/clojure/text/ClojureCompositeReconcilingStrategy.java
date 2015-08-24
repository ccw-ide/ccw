/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Andrea RICHIARDI - Initial implementation
 *******************************************************************************/
package ccw.editors.clojure.text;

import org.eclipse.jdt.internal.ui.text.CompositeReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;

import ccw.editors.clojure.IClojureEditor;

/**
 * A composite reconciling strategy for CCW's content types.<br><br>
 * Note that {@link ClojureCompositeReconcilingStrategy} is an internal JDT class and might change.
 *
 */
@SuppressWarnings("restriction")
public class ClojureCompositeReconcilingStrategy extends CompositeReconcilingStrategy{

    public ClojureCompositeReconcilingStrategy(IClojureEditor editor) {
        setReconcilingStrategies(new IReconcilingStrategy[] { new FoldingReconcileStrategy(editor) });
    }
    
    /*
     * @see org.eclipse.jface.text.reconciler.CompositeReconcilingStrategy#initialReconcile()
     */
    @Override
    public void initialReconcile() {
        
    }
}
