package ccw.core;

import static org.junit.Assert.assertEquals;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ccw.CCWPlugin;

@RunWith(SWTBotJunit4ClassRunner.class)
public class SmokeTests {

	/**************************************************************************
	 * HELPER METHODS                                                         *
	 **************************************************************************/
	
	/**
	 * @return an Eclipse Bot. Gets rid of the Welcome Page, if any. Opens the Java Perspective.
	 */
	public static SWTWorkbenchBot eclipseBot() {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close();
		} catch (Exception e) { 
			// Nevermind
		}
		bot.perspectiveByLabel("Java").activate();
		return bot;
	}
	
	public static SWTBotMenu menu(SWTWorkbenchBot bot, String menu, String... subMenus) {
		SWTBotMenu ret = bot.menu(menu);
		for (String subMenu: subMenus) {
			ret = ret.menu(subMenu);
		}
		return ret;
	}
	
	public static SWTBotShell activateShell(SWTWorkbenchBot bot, String shell) {
		SWTBotShell s = bot.shell(shell);
		s.activate();
		return s;
	}
	
	public static void createClojureProject(SWTWorkbenchBot bot, String projectName) {
		menu(bot, "File", "New", "Project...").click();
		activateShell(bot, "New Project");
		bot.tree().expandNode("Clojure").select("Clojure Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
	}
	
	/** Test if a project exists by checking the Package Explorer View */
	public static void assertProjectExists(SWTWorkbenchBot bot, String projectName) {
		bot.viewByTitle("Package Explorer").bot().tree().expandNode(projectName);
	}
	
	/**************************************************************************
	 * TESTS                                                                  *
	 **************************************************************************/
	public static SWTWorkbenchBot bot = null;
	
	@BeforeClass
	public static void setupClass() {
		bot = eclipseBot();
	}
	
	@Test
	public void testID() {
		assertEquals("ccw.core", CCWPlugin.PLUGIN_ID);
	}
	
	@Test
	public void canCreateANewClojureProject() throws Exception {
		createClojureProject(bot, "MyFirstClojureProject");
		assertProjectExists(bot, "MyFirstClojureProject");
	}
 
}
