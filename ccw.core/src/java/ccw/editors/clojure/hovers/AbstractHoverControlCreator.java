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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;

import ccw.core.StaticStrings;

/**
 * Abstract control creator for the hover framework, adapted from org.eclipse.jdt.
 * 
 * @author Andrea Richiardi
 */
public abstract class AbstractHoverControlCreator extends AbstractReusableInformationControlCreator {
   
    /**
     * Ctor.
     */
    public AbstractHoverControlCreator() {
    }

    /**
     * Override to customize the toolbar that the enriched hover should show.
     * The default implementation returns null, in which case the toolbar is not shown.
     * @return a ToolBarManager or null.
     */
    protected @Nullable ToolBarManager toolBarManager() {
        return null;
    }
    
    /**
     * Override to customize the Font, it defaults to org.eclipse.jdt.ui.editors.textfont
     * @return
     */
    protected String symbolicFontName() {
        return StaticStrings.CCW_HOVER_FONT;
    }
    
    /**
     * Override to customize the resizable property, it defaults to Boolean.FALSE.
     * @return
     */
    protected @NonNull Boolean resizable() {
        return Boolean.FALSE;
    }
}