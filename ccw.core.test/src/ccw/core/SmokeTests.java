package ccw.core;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.Before;
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
		return bot;
	}

	public static SWTWorkbenchBot openJavaPerspective(SWTWorkbenchBot bot) {
		bot.perspectiveByLabel("Java").activate();
		return bot;
	}
	
	public static SWTWorkbenchBot closeWelcome(SWTWorkbenchBot bot) {
		try {
			bot.viewByTitle("Welcome").close();
		} catch (Exception e) { 
			// Nevermind
		}
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
	
	public static void waitForWorkspace() throws Exception {
		// ensure that all queued workspace operations and locks are released
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
					// nothing to do!
				}
			}, new NullProgressMonitor());
	}
	
	public static SWTWorkbenchBot createClojureProject(SWTWorkbenchBot bot, String projectName) throws Exception {
		menu(bot, "File", "New", "Project...").click();
		return fillNewProject(bot, projectName);
	}

	public static SWTWorkbenchBot fillNewProject(SWTWorkbenchBot bot, String projectName) throws Exception {
		activateShell(bot, "New Project");
		bot.tree().expandNode("Clojure").select("Clojure Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
		waitForWorkspace();
		return bot;
	}
	/** Test if a project exists by checking the Package Explorer View */
	public static void assertProjectExists(SWTWorkbenchBot bot, String projectName) {
		SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		SWTBotTree projectsTree = packageExplorer.bot().tree();
		projectsTree.expandNode(projectName);
	}
	
	/**************************************************************************
	 * TESTS                                                                  *
	 **************************************************************************/
	public static SWTWorkbenchBot bot = null;
	
	@BeforeClass
	public static void setupClass() throws Exception {
		bot = eclipseBot();
		//checkProductDefaultConfiguration();
		closeWelcome(bot);
	}
	
	private static void checkProductDefaultConfiguration() throws Exception {
		bot.viewByTitle("Welcome").setFocus();
		bot.link("New Clojure Project").click();
		fillNewProject(bot, "ProjectFromWelcomePageLink");
		assertProjectExists(bot,  "ProjectFromWelcomePageLink");
	}
	
	@Before
	public void beforeTest() {
		openJavaPerspective(bot);
		assertEquals("Java", bot.activePerspective().getLabel());
	}
	
	@Test
	public void testID() {
		assertEquals("ccw.core", CCWPlugin.PLUGIN_ID);
	}
	
	@Test
	public void canCreateANewClojureProject() throws Exception {
		createClojureProject(bot, "MyFirstClojureProject");
		assertProjectExists(bot,  "MyFirstClojureProject");
	}
 
}
