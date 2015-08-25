/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Laurent PETIT - initial implementation
 *     Andrea RICHIARDI - additions using new BotUtils API
 *******************************************************************************/
package ccw.core;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertTextContains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ccw.util.TestUtil;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ClojureEditorTests {

    public static final String MULTI_LINE_FORM = "(defn my-fun\n\"Lorem ipsum dolor sit amet,\nconsectetur adipiscing elit,\nsed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\"\n(println \"docet\"))";
    public static final Position MULTI_LINE_LIST_ANNOTATION_POSITION = new Position(1, 156);
    public static final Position MULTI_LINE_STRING_ANNOTATION_POSITION = new Position(14, 124);

	// TODO voir si on peut enlever le static
	public static BotUtils bot = null;
	public static final String PROJECT_NAME = "editor-test";
	public static final String CORE_CLJ_NAME = "src/editor_test/core.clj";

	@BeforeClass
	public static void setupClass() throws Exception {
		bot = new BotUtils();
	}

    @Before
    public void initClass() throws Exception {
        bot.openJavaPerspective().createAndWaitForProject(PROJECT_NAME);
    }

    @After
    public void cleanClass() throws Exception {
        bot.purgeProject(PROJECT_NAME);
    }

    @Test
    public void canOpenEditor() throws Exception {
        bot.doubleClickOnFileInProject(PROJECT_NAME, CORE_CLJ_NAME);
        bot.bot().activeEditor().toTextEditor();
    }
	
	@Test
	public void canOpenEditorAndInsertText() throws Exception {
		bot.doubleClickOnFileInProject(PROJECT_NAME, CORE_CLJ_NAME);
		SWTBotEclipseEditor e = bot.bot().activeEditor().toTextEditor();
		e.insertText("salut laurent");
		e.save();
		assertTextContains("salut laurent", e.bot().getFocusedWidget());
	}

	@Test
    public void canFoldParensAndDoubleApices() throws Exception {
        bot.doubleClickOnFileInProject(PROJECT_NAME, CORE_CLJ_NAME)
           .replaceTextOnActiveEditor(MULTI_LINE_FORM)
           .saveActiveEditor()
           .focusActiveEditor()
           .waitForAnnotations(); // Waits for the reconciler to trigger and fill the annotations up

        Map<Annotation,Position> projectionMap =  TestUtil.getProjectionMap(bot.editor().clojure);
        assertThat("Projection map should contain correct folding positions",
                projectionMap.values(),
                containsInAnyOrder(MULTI_LINE_LIST_ANNOTATION_POSITION, MULTI_LINE_STRING_ANNOTATION_POSITION));
    }
}
