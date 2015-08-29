/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea RICHIARDI - initial implementation
 *******************************************************************************/
package ccw.core.bots;

import org.eclipse.jface.preference.IPreferenceStore;

import waits.ProjectionAnnotationModelNotEmpty;
import waits.ProjectionAnnotationModelNull;
import ccw.CCWPlugin;
import ccw.core.BotUtils;
import ccw.core.PrefStrings;
import ccw.preferences.PreferenceConstants;

/**
 * Bot used for executing actions about the folding feature
 */
public class FoldingBot {

    public static final long TIMEOUT_PROJECT_ANNOTATION_MODEL = 10000;

    public static final String TABLE_FOLD_TARGET_PAREN = "Parens";
    public static final String TABLE_FOLD_TARGET_DOUBLEAPICES = "Double-apices";

    public final BotUtils utils;

    public FoldingBot(BotUtils utils) {
        this.utils = utils;
    }

    public FoldingBot ok() {
        utils.ok();
        return this;
    }

    public FoldingBot selectPreferencePage() {
        utils.selectPreferencePage(PrefStrings.TREE_ENTRY_CLOJURE,
                PrefStrings.TREE_ENTRY_CLOJURE_EDITOR,
                PrefStrings.TREE_ENTRY_CLOJURE_EDITOR_FOLDING);
        return this;
    }

    public FoldingBot uncheckPreference(String preferenceFoldTarget) {
        utils.bot().table().getTableItem(preferenceFoldTarget).uncheck();
        return this;
    }

    public FoldingBot checkPreference(String preferenceFoldTarget) {
        utils.bot().table().getTableItem(preferenceFoldTarget).check();
        return this;
    }

    /**
     * Fills up the given file in the given project with the given text.
     * @param projectName A project name
     * @param fileName A file name (see {@link BotUtils#doubleClickOnFileInProject(String, String)})
     * @param text The text to insert (will overwrite the existing)
     * @return
     * @throws Exception
     */
    public FoldingBot fillAndWaitForAnnotations(String projectName, String fileName, String text) throws Exception {
        utils.replaceTextInFile(projectName, fileName, text).focusActiveEditor();
        return waitForAnnotations(); // Waits for the reconciler to trigger and fill the annotations up
    }

    /**
     * Enable the projection feature on the active editor through the
     * {@link PreferenceConstants#EDITOR_FOLDING_PROJECTION_ENABLED} preference.
     * @return
     * @throws Exception
     */
    public FoldingBot enableProjectionPreference() {
        IPreferenceStore store = CCWPlugin.getDefault().getPreferenceStore();
        store.setValue(PreferenceConstants.EDITOR_FOLDING_PROJECTION_ENABLED, true);
        return this;
    }

    /**
     * Disable the projection feature on the active editor through the
     * {@link PreferenceConstants#EDITOR_FOLDING_PROJECTION_ENABLED} preference.
     * @return
     * @throws Exception
     */
    public FoldingBot disableProjectionPreference() {
        IPreferenceStore store = CCWPlugin.getDefault().getPreferenceStore();
        store.setValue(PreferenceConstants.EDITOR_FOLDING_PROJECTION_ENABLED, false);
        return this;
    }

    public FoldingBot waitForAnnotations() throws Exception {
        utils.bot.waitUntil(new ProjectionAnnotationModelNotEmpty(utils.editor().clojure), TIMEOUT_PROJECT_ANNOTATION_MODEL);
        return this;
    }

    public FoldingBot waitForProjectionEnabled() throws Exception {
        utils.bot.waitWhile(new ProjectionAnnotationModelNull(utils.editor().clojure), TIMEOUT_PROJECT_ANNOTATION_MODEL);
        return this;
    }

    public FoldingBot waitForProjectionDisabled() throws Exception {
        utils.bot.waitUntil(new ProjectionAnnotationModelNull(utils.editor().clojure), TIMEOUT_PROJECT_ANNOTATION_MODEL);
        return this;
    }
}
