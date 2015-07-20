/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Laurent PETIT - initial implementation
 *     Andrea RICHIARDI - additional tests: ui thread, repl, test menu
 *******************************************************************************/
package ccw.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.SWTBotAssert;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ccw.CCWPlugin;

@RunWith(SWTBotJunit4ClassRunner.class)
public class SmokeTests {

	public static BotUtils bot = null;
	
	@BeforeClass
	public static void setupClass() throws Exception {
		bot = new BotUtils();
	}

	@Before
	public void beforeTest() {
		bot.openJavaPerspective();
	}
	
	@Test
	public void testID() {
		assertEquals("ccw.core", CCWPlugin.PLUGIN_ID);
	}
	
	@Test
	public void canCreateANewClojureProject() throws Exception {
		bot
		.createClojureProject(PROJECT_NAME)
		.sendUpdateDependeciesToBackground()
		.assertProjectExists(PROJECT_NAME)
		.purgeProject(PROJECT_NAME);
	}
	
	@Test
    public void canAlwaysShowClojureMenu() throws Exception {
	    SWTBotAssert.assertVisible(bot.menu("Clojure"));
    }

    @Test
    public void swtbotDoesNotRunOnTheUIThread() throws Exception {
        assertNull(Display.getCurrent());
        assertNotSame(Thread.currentThread(), SWTUtils.display().getThread());
    }

    @Test
    public void canShowTestGeneratorEntryInClojureMenu() throws Exception {
        SWTBotAssert.assertVisible(bot.menu("Clojure", MenuLabels.TEST, MenuLabels.TEST_GENERATOR));
    }
}
