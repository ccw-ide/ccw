/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Andrea RICHIARDI - initial implementation
 *******************************************************************************/
package ccw.editors.clojure.folding;

import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * The folding model. It is injected during the plugin initialization.
 */
public interface FoldingModel {
	
    /**
     * Retrieves a list of observable FoldingDescriptor(s).
     * @return An ObservableList
     */
	IObservableList getObservableDescriptors();

	/**
	 * Persists the input FoldingDescriptor(s).
	 */
	void persistDescriptors(List<FoldingDescriptor> descriptors);
}
