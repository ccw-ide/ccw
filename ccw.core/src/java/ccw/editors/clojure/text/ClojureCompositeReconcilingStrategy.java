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

import org.eclipse.jface.text.reconciler.IReconcilingStrategy;

import ccw.editors.clojure.IClojureEditor;

/**
 * A composite reconciling strategy for CCW's content types.<br><br>
 * Source of this pattern: {@link org.eclipse.jdt.internal.ui.text.CompositeReconcilingStrategy}
 *
 */
public class ClojureCompositeReconcilingStrategy extends CompositeReconcilingStrategy{

    public ClojureCompositeReconcilingStrategy(IClojureEditor editor) {
        setReconcilingStrategies(new IReconcilingStrategy[] {
                new FoldingReconcileStrategy(editor)
        });
    }
}
