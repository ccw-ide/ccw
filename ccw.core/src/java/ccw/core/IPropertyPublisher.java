/*******************************************************************************
 * Copyright (c) 2015 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Andrea RICHIARDI - initial implementation
 *******************************************************************************/
package ccw.core;

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * Protocol for objects that publish their properties to registered listeners.
 * @author Andrea Richiardi
 */
public interface IPropertyPublisher {

    void addPropertyChangeListener(IPropertyChangeListener listener);

    void removePropertyChangeListener(IPropertyChangeListener listener);

}