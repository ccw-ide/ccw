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
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension4;
import org.eclipse.swt.widgets.Shell;

import ccw.util.UiUtils;

/**
 * Control creator for the hover framework, adapted from org.eclipse.jdt.
 * The default implementation uses a BrowserInformationControl (if available) taking additional configuration from
 * the protected methods (symbolicFontName, css, ...) which are inherited from AbstractHoverControlCreator.
 * This Control will always be <b> not resizable <b>
 * 
 * @see AbstractHoverControlCreator
 * @author Andrea Richiardi
 */
public class HoverControlCreator extends AbstractHoverControlCreator {
    
    private final IInformationControlCreator fEnrichedInformationControlCreator;

    /**
     * @param enrichedInformationControlCreator control creator for enriched hover
     */
    public HoverControlCreator(IInformationControlCreator enrichedInformationControlCreator) {
        fEnrichedInformationControlCreator = enrichedInformationControlCreator;
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
     */
    @Override
    public IInformationControl doCreateInformationControl(Shell parent) {
        IInformationControl control = null;
        
        if (BrowserInformationControl.isAvailable(parent)) {
            control = new CCWBrowserInformationControl(parent, symbolicFontName(), UiUtils.getTooltipAffordanceString()) {
                
                @Override
                public IInformationControlCreator getInformationPresenterControlCreator() {
                    return fEnrichedInformationControlCreator;
                }
            };
        } else {
            control = new DefaultInformationControl(parent, false);
        }
        
        return control;
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#canReuse(org.eclipse.jface.text.IInformationControl)
     */
    @Override
    public boolean canReuse(IInformationControl control) {
        if (!super.canReuse(control))
            return false;

        if (control instanceof IInformationControlExtension4) {
            ((IInformationControlExtension4)control).setStatusText(UiUtils.getTooltipAffordanceString());
        }

        return true;
    }
}