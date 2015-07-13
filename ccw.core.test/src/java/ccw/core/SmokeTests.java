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
		.createClojureProject("my-first-clojure-project")
		.assertProjectExists("my-first-clojure-project");
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
        SWTBotAssert.assertVisible(bot.menu("Clojure", "Test", "Generator..."));
    }
}
