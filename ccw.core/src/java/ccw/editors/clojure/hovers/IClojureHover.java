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

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.information.IInformationProviderExtension2;

/**
 * The protocol for Counterclockwise hovers.
 * @author Andrea Richiardi
 *
 */
public interface IClojureHover extends ITextHover, ITextHoverExtension, ITextHoverExtension2, IInformationProviderExtension2 {
    /** Placeholder **/
}
