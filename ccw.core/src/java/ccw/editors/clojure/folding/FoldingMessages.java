/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrea RICHIARDI - CCW adjustments
 *******************************************************************************/
package ccw.editors.clojure.folding;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Class that gives access to the folding messages resource bundle.
 */
public class FoldingMessages {

    private static final String BUNDLE_NAME = "ccw.editors.clojure.folding.FoldingMessages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private FoldingMessages() {
        // no instance
    }

    public static String Projection_Toggle_label;
    public static String Projection_Toggle_tooltip;
    public static String Projection_Toggle_description;
    public static String Projection_Toggle_image;
    public static String Projection_ExpandAll_label;
    public static String Projection_ExpandAll_tooltip;
    public static String Projection_ExpandAll_description;
    public static String Projection_ExpandAll_image;
    public static String Projection_Expand_label;
    public static String Projection_Expand_tooltip;
    public static String Projection_Expand_description;
    public static String Projection_Expand_image;
    public static String Projection_CollapseAll_label;
    public static String Projection_CollapseAll_tooltip;
    public static String Projection_CollapseAll_description;
    public static String Projection_CollapseAll_image;
    public static String Projection_Collapse_label;
    public static String Projection_Collapse_tooltip;
    public static String Projection_Collapse_description;
    public static String Projection_Collapse_image;
    public static String Projection_FoldingMenu_name;
    
//    public static String Projection_Restore_label;
//    public static String Projection_Restore_tooltip;
//    public static String Projection_Restore_description;
//    public static String Projection_Restore_image;
//    public static String Projection_CollapseComments_label;
//    public static String Projection_CollapseComments_tooltip;
//    public static String Projection_CollapseComments_description;
//    public static String Projection_CollapseComments_image;

    /**
     * Returns the resource string associated with the given key in the resource bundle. If there isn't
     * any value under the given key, the key is returned.
     *
     * @param key the resource key
     * @return the string
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Returns the resource bundle managed by the receiver.
     *
     * @return the resource bundle
     * @since 3.0
     */
    public static ResourceBundle getResourceBundle() {
        return RESOURCE_BUNDLE;
    }

    static {
        NLS.initializeMessages(BUNDLE_NAME, FoldingMessages.class);
    }
}
