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
package ccw.editors.clojure.hovers;

import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * The hover model. It is injected during the plugin initialization.
 * @author Andrea Richiardi
 *
 */
public interface HoverModel {
	
    /**
     * Retrieves a list of HoverDescriptor objects.
     * @return An ObservableList
     */
	IObservableList observableHoverDescriptors();

	/**
	 * Persists the input hovers.
	 */
	void persistHoverDescriptors(List<HoverDescriptor> descriptors);
}
