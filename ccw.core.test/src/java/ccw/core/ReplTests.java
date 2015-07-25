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

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ReplTests {

    public static BotUtils bot = null;

    public static final String PROJECT_NAME = "my-first-clojure-project";

    @BeforeClass
    public static void setupClass() throws Exception {
        bot = new BotUtils();
    }

    @AfterClass
    public static void cleanClass() throws Exception {
        bot.quietlyCloseRepl()
           .purgeProject(PROJECT_NAME);
    }

    @Before
    public void beforeTest() {
        bot.openJavaPerspective()
           .createAndWaitForProject(PROJECT_NAME)
           .clickInLeiningenMenuForProject(PROJECT_NAME, MenuLabels.LAUNCH_HEADLESS_REPL);
    }

    @Test
    public void canOpenRepl() throws Exception {
        bot.waitForRepl();
    }
}
