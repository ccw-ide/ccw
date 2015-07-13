package ccw.core;

import static org.junit.Assert.fail;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

public class BotUtils {

	public final SWTWorkbenchBot bot;
	
	public BotUtils() throws Exception {
		bot = eclipseBot();
	}

	public SWTWorkbenchBot eclipseBot() {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		return bot;
	}

	public BotUtils openJavaPerspective() {
		closeWelcome();
		bot.perspectiveByLabel("Java").activate();
		return this;
	}
	
	public BotUtils closeWelcome() {
		try {
			SWTBotView v = bot.viewByTitle("Welcome");
			if (v != null) {
				v.close();
			}
		} catch (Exception e) { 
			// Nevermind
		}
		return this;
		
	}
	public SWTBotMenu menu(String menu, String... subMenus) {
		SWTBotMenu ret = bot.menu(menu);
		for (String subMenu: subMenus) {
			ret = ret.menu(subMenu);
		}
		return ret;
	}

	public BotUtils activateShell(String shell) {
		SWTBotShell s = bot.shell(shell);
		s.activate();
		return this;
	}
	
	public BotUtils waitForWorkspace() throws Exception {
		// ensure that all queued workspace operations and locks are released
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
					// nothing to do!
				}
			}, new NullProgressMonitor());
		return this;
	}
	

	public BotUtils createClojureProject(String projectName) throws Exception {
		menu("File", "New", "Project...").click();
		return fillNewProject(bot, projectName).runningInBackground();
	}

	/** Create new project in the workspace root folder */
	public BotUtils fillNewProject(SWTBot bot, String projectName) throws Exception {
		bot = activateShell("New Project").bot();
		bot.tree().expandNode("Clojure").select("Clojure Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName);
		SWTBotCheckBox sameLocation = bot.checkBoxWithId("same-as-previous-location");
		SWTBotText location = bot.textWithId("location");
		final String testLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		if (!testLocation.equals(location.getText())) {
			if (!location.isEnabled()) {
				if (sameLocation.isEnabled() && sameLocation.isChecked()) {
					sameLocation.deselect(); // should have side effect of enabling location widget
				} else {
					fail("Location should only be disabled if sameLocation is enabled and checked");
				}
			}
			location.setText(testLocation);
		}
		bot.button("Finish").click();
		return this;
	}
	/** Test if a project exists by checking the Package Explorer View */
	public BotUtils assertProjectExists(String projectName) {
		SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		SWTBotTree projectsTree = packageExplorer.bot().tree();
		projectsTree.expandNode(projectName);
		return this;
	}
	public BotUtils runningInBackground() {
	    try {
	        bot.buttonWithLabel("Run in background");
	    } catch (WidgetNotFoundException e) {
	        // wooosh
	    }
	    return this;
	}
	public BotUtils whenSelectInClojureMenu(String entryName) throws Exception {
        menu("Clojure", entryName).click();
        return this;
    }

	public SWTWorkbenchBot bot() {
		return bot;
	}
	
}
