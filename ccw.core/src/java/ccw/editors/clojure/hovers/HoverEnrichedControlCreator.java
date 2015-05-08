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

import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.swt.widgets.Shell;

/**
 * Control creator when the hover will be enriched, adapted from org.eclipse.jdt.
 * The default imlementation uses a BrowserInformationControl (if available) taking additional configuration from
 * the protected methods (symbolicFontName, css, ...) which are inherited from AbstractHoverControlCreator.
 * 
 * @see AbstractHoverControlCreator
 * @author Andrea Richiardi
 */
public class HoverEnrichedControlCreator extends AbstractHoverControlCreator {

    /**
     * Creates a new HoverEnrichedControlCreator.
     */
    public HoverEnrichedControlCreator() {
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
     */
    @Override
    public IInformationControl doCreateInformationControl(Shell parent) {
        if (BrowserInformationControl.isAvailable(parent)) {
            BrowserInformationControl iControl= new BrowserInformationControl(parent, symbolicFontName(), toolBarManager());

//          addLinkListener(iControl);
            return iControl;

        } else {
            return new DefaultInformationControl(parent, true);
        }
    }
}