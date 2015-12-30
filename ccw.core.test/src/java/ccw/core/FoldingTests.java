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
package ccw.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ccw.core.bots.FoldingBot;
import ccw.util.TestUtil;

@RunWith(SWTBotJunit4ClassRunner.class)
public class FoldingTests {

    public static final String MULTI_LINE_FORM = "(defn my-fun\n\"Lorem ipsum dolor sit amet,\nconsectetur adipiscing elit,\nsed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\"\n(println \"docet\"))";
    public static final Position MULTI_LINE_LIST_ANNOTATION_POSITION = new Position(1, 156);
    public static final Position MULTI_LINE_STRING_ANNOTATION_POSITION = new Position(14, 124);

    public static final String PROJECT_NAME = "editor-test";
    public static final String CORE_CLJ_NAME = "src/editor_test/core.clj";

    public static FoldingBot bot = null;

    @BeforeClass
    public static void setupClass() throws Exception {
        bot = BotUtils.foldingBot();
    }

    @AfterClass
    public static void cleanClass() throws Exception {
        bot.utils.purgeProject(PROJECT_NAME);
    }

    @Before
    public void initTest() throws Exception {
        bot.utils.openJavaPerspective().createAndWaitForProjectIfNecessary(PROJECT_NAME);
    }

    //////////////////////
    /// TT EE SS TT SS ///
    //////////////////////

    @Test
    public void shouldSelectFoldingPreferencePage() throws Exception {
        bot.selectPreferencePage().utils
        .assertPreferencePage(PrefStrings.TREE_ENTRY_CLOJURE_EDITOR_FOLDING)
        .ok();
    }

    @Test
    public void canFoldParensAndDoubleApices() throws Exception {
        bot.selectPreferencePage()
        .checkPreference(FoldingBot.TABLE_FOLD_TARGET_PAREN)
        .checkPreference(FoldingBot.TABLE_FOLD_TARGET_DOUBLEAPICES)
        .ok()
        .enableProjectionPreference()
        .fillAndWaitForAnnotations(PROJECT_NAME, CORE_CLJ_NAME, MULTI_LINE_FORM);

        Map<Annotation,Position> projectionMap = TestUtil.getProjectionMap(bot.utils.editor().clojure);

        assertThat("Editor should contain correct folding positions",
                projectionMap.values(),
                containsInAnyOrder(MULTI_LINE_LIST_ANNOTATION_POSITION,
                        MULTI_LINE_STRING_ANNOTATION_POSITION));

        bot.utils.clsActiveEditor().closeActiveEditor();
    }

    @Test
    public void canFoldParensOnly() throws Exception {
        bot.selectPreferencePage()
        .checkPreference(FoldingBot.TABLE_FOLD_TARGET_PAREN)
        .uncheckPreference(FoldingBot.TABLE_FOLD_TARGET_DOUBLEAPICES)
        .ok()
        .enableProjectionPreference()
        .fillAndWaitForAnnotations(PROJECT_NAME, CORE_CLJ_NAME, MULTI_LINE_FORM);

        Map<Annotation,Position> projectionMap =  TestUtil.getProjectionMap(bot.utils.editor().clojure);

        assertThat("Editor should contain only parens (top level lists) folding positions",
                projectionMap.values(),
                contains(MULTI_LINE_LIST_ANNOTATION_POSITION));

        bot.utils.clsActiveEditor().closeActiveEditor();
    }

    @Test
    public void canFoldDoubleApicesOnly() throws Exception {
        bot.selectPreferencePage()
        .uncheckPreference(FoldingBot.TABLE_FOLD_TARGET_PAREN)
        .checkPreference(FoldingBot.TABLE_FOLD_TARGET_DOUBLEAPICES)
        .ok()
        .enableProjectionPreference()
        .fillAndWaitForAnnotations(PROJECT_NAME, CORE_CLJ_NAME, MULTI_LINE_FORM);

        Map<Annotation,Position> projectionMap =  TestUtil.getProjectionMap(bot.utils.editor().clojure);

        assertThat("Editor should contain only string folding positions",
                projectionMap.values(),
                contains(MULTI_LINE_STRING_ANNOTATION_POSITION));

        bot.utils.clsActiveEditor().closeActiveEditor();
    }

    @Test(expected = TimeoutException.class)
    public void noFoldIfAllOptionsAreDisabled() throws Exception {
        bot.selectPreferencePage()
        .uncheckPreference(FoldingBot.TABLE_FOLD_TARGET_PAREN)
        .uncheckPreference(FoldingBot.TABLE_FOLD_TARGET_DOUBLEAPICES)
        .ok()
        .enableProjectionPreference()
        .fillAndWaitForAnnotations(PROJECT_NAME, CORE_CLJ_NAME, MULTI_LINE_FORM);

        bot.utils.clsActiveEditor().closeActiveEditor();
    }

    @Test
    public void projectionShouldBeOnAfterEnablingPreference() throws Exception {
        bot.utils.doubleClickOnFileInProject(PROJECT_NAME, CORE_CLJ_NAME);
        bot.enableProjectionPreference().waitForProjectionEnabled()
           .utils.closeActiveEditor();
    }

    @Test
    public void projectionShouldBeOffAfterDisablingPreference() throws Exception {
        bot.utils.doubleClickOnFileInProject(PROJECT_NAME, CORE_CLJ_NAME);
        bot.disableProjectionPreference().waitForProjectionDisabled()
           .utils.closeActiveEditor();
    }

    @Test
    public void projectionShouldBeOnAfterDisableAndEnablePreference() throws Exception {
        bot.utils.doubleClickOnFileInProject(PROJECT_NAME, CORE_CLJ_NAME);
        bot.disableProjectionPreference()
           .waitForProjectionDisabled()
           .enableProjectionPreference()
           .waitForProjectionEnabled()
        .utils.closeActiveEditor();
    }
}
