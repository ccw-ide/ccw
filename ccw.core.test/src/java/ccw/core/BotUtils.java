/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent PETIT - initial implementation
 *    Andrea RICHIARDI - reviving and enhancing of the API
 *******************************************************************************/
package ccw.core;

import static org.junit.Assert.fail;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.hamcrest.Matcher;

/**
 * Wrapper utility class for common Counterclockwise UI tests.
 * <ul>
 * <li>The "quietly-*" methods should never be used for asserting tests.
 *     They have been thought for cleaning and never throw exceptions.</li>
 * <li>The "wait-*" methods can be used for asserting tests or for general waits.
 *     They always throw exceptions.</li>
 * </ul>
 */
public class BotUtils {

    public static final long TIMEOUT_REPL = 60000;
    public static final long TIMEOUT_UPDATE_DEPENDENCIES = 25000;

    public static final long DELAY_UPDATE_DEPENDENCIES = 1000;

    public static final Matcher<Widget> MATCHER_WIDGET_UPDATE_DEPENDENCIES = WidgetMatcherFactory.withRegex(".*project dependencies.*");
    public static final Matcher<Widget> MATCHER_WIDGET_REPL_LOG = WidgetMatcherFactory.withRegex("^;; Clojure.*");
    public static final Matcher<Widget> MATCHER_WIDGET_DELETE_PROJECT = WidgetMatcherFactory.withRegex("^Are you sure.*");

    public static final String NAME_REPLVIEW = "REPL";

    public final SWTWorkbenchBot bot;
	
	public BotUtils() throws Exception {
		bot = createSWTBot();
	}

	public SWTWorkbenchBot createSWTBot() {
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
	
	public BotUtils waitForWorkspace() {
	    // ensure that all queued workspace operations and locks are released
	    try {
	        ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
	            public void run(IProgressMonitor monitor) throws CoreException {
	                // nothing to do!
	            }
	        }, new NullProgressMonitor());
	    } catch (CoreException e) {
	        throw new RuntimeException(e.getMessage(), e);
	    }
	    return this;
	}
	

	public BotUtils createClojureProject(String projectName) {
		menu("File", "New", "Project...").click();
		return fillNewProject(bot, projectName);
	}

	/** Create new project in the workspace root folder */
	public BotUtils fillNewProject(SWTBot bot, String projectName) {
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
		waitForWorkspace();
		return this;
	}
	/** Test if a project exists by checking the Package Explorer View */
	public BotUtils assertProjectExists(String projectName) {
		SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		SWTBotTree projectsTree = packageExplorer.bot().tree();
		projectsTree.expandNode(projectName);
		return this;
	}

    public <T extends Widget> BotUtils sendToBackground(Matcher<T> matcher, long timeout, long delay) {
        try {
            bot.waitUntil(Conditions.waitForWidget(matcher), timeout, delay);
            bot.button("Run in Background").click();
        } catch (WidgetNotFoundException e) {
            // wooosh
        }
        return this;
    }
    public <T extends Widget> BotUtils sendToBackground(Matcher<T> matcher, long timeout) {
        bot.waitUntil(Conditions.waitForWidget(matcher), timeout);
        bot.button("Run in Background").click();
        return this;
    }
    public <T extends Widget> BotUtils sendToBackground(Matcher<T> matcher) {
        bot.waitUntil(Conditions.waitForWidget(matcher));
        bot.button("Run in Background").click();
        return this;
    }

    /**
     * Wrapper around sendToBackground, we need custom timeouts.
     * @return
     */
    public <T extends Widget> BotUtils sendUpdateDependeciesToBackground() {
        return sendToBackground(MATCHER_WIDGET_UPDATE_DEPENDENCIES, TIMEOUT_UPDATE_DEPENDENCIES, DELAY_UPDATE_DEPENDENCIES);
    }

    public BotUtils whenSelectInClojureMenu(String entryLabel) throws Exception {
        menu("Clojure", entryLabel).click();
        return this;
    }

    public BotUtils whenSelectInLeiningenContextMenu(String projectName, String entryLabel) throws Exception {
        SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
        SWTBotTree projectsTree = packageExplorer.bot().tree();
        SWTBotTreeItem node = projectsTree.getTreeItem(projectName);
        node.contextMenu("Leiningen").menu(entryLabel).click();
        return this;
    }

    public BotUtils waitForRepl() throws Exception {
        try {
            bot.waitUntil(Conditions.waitForWidget(MATCHER_WIDGET_REPL_LOG), TIMEOUT_REPL);
        } catch (TimeoutException e) {
            String message = "Could not find repl widget"; //$NON-NLS-1$
            throw new WidgetNotFoundException(message, e);
        }
        return this;
    }

    public BotUtils quietlyCloseRepl() throws Exception {
        try {
            bot.viewByPartName(NAME_REPLVIEW).close();
        } catch (WidgetNotFoundException e) {
            // wooosh
        }
        return this;
    }

    public BotUtils purgeProject(String projectName) {
        return deleteProject(projectName).deletingOnDisk().OK().quietlyContinuingIfNotInSync();
    }

    public BotUtils deleteProject(String projectName) {
        SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
        SWTBotTree projectsTree = packageExplorer.bot().tree();
        SWTBotTreeItem node = projectsTree.getTreeItem(projectName);
        node.contextMenu("Refresh").click();
        node.contextMenu("Delete").click();
        return this;
    }

    public BotUtils deletingOnDisk() {
        bot.waitUntil(Conditions.waitForWidget(MATCHER_WIDGET_DELETE_PROJECT));
        bot.checkBox().click();
        return this;
    }

    public BotUtils quietlyContinuingIfNotInSync() {
        try {
            bot.button("Continue").click();
        } catch (Exception e) {
            // wooosh
        }
        return this;
    }

    public BotUtils OK() {
        bot.button("OK").click();
        return this;
    }

    public BotUtils cancel() {
        bot.button("Cancel").click();
        return this;
    }

    public SWTWorkbenchBot bot() {
        return bot;
    }
}
