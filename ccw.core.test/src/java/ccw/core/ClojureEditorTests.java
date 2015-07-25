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

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ClojureEditorTests {

	// TODO voir si on peut enlever le static
	public static BotUtils bot = null;
	public static final String PROJECT_NAME = "editor-test";
	public static final String CORE_CLJ_NAME = "src/editor_test/core.clj";
	
	@BeforeClass
	public static void setupClass() throws Exception {
		bot = new BotUtils();
		bot.openJavaPerspective().createAndWaitForProject(PROJECT_NAME);
	}

	@AfterClass
    public static void cleanClass() throws Exception {
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
}
