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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
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
    public static final long TIMEOUT_FIND_ITEM_IN_PROJECT = 15000;

    public static final Matcher<Widget> MATCHER_WIDGET_UPDATE_DEPENDENCIES = WidgetMatcherFactory.withRegex(".*project dependencies.*");
    public static final Matcher<Widget> MATCHER_WIDGET_REPL_LOG = WidgetMatcherFactory.withRegex("^;; Clojure.*");
    public static final Matcher<Widget> MATCHER_WIDGET_DELETE_PROJECT = WidgetMatcherFactory.withRegex("^Are you sure.*");

    public static final String NAME_REPLVIEW = "REPL";

    public final SWTWorkbenchBot bot;
	public final SWTBotShell mainShell;
	
	public BotUtils() throws Exception {
		bot = createSWTBot();
		mainShell = findMainShell();
	}

	// AR - from https://wiki.eclipse.org/Linux_Tools_Project/SWTBot_Workarounds#Main_Menu_Items_Not_Found
	private SWTBotShell findMainShell() {
	    SWTBotShell mainShell = null;

	    // AR - from https://wiki.eclipse.org/Linux_Tools_Project/SWTBot_Workarounds#Main_Menu_Items_Not_Found
	    for (int i = 0, attempts = 100; i < attempts; i++) {
	        for (SWTBotShell shell : bot.shells()) {
	            if (shell.getText().contains("Eclipse SDK") || shell.getText().contains("Counterclockwise")) {
	                mainShell = shell;
	                shell.setFocus();
	                break;
	            }
	        }
	    }
	    return mainShell;
	}

	public SWTWorkbenchBot createSWTBot() {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		return bot;
	}

	public BotUtils openJavaPerspective() {
		quietlyCloseWelcome();
		bot.perspectiveByLabel("Java").activate();
		return this;
	}

    /**
     * Opens a file in a given project, simulating a double click.
     * The file name should contain "/" as segment divider.</br>
     * For example, "src/editor_test/core.clj", will expand "src", then "editor_test",
     * and finally double click on "core.clj"
     *
     * @param projectName
     * @param fileName
     * @return
     */
    public BotUtils doubleClickOnFileInProject(String projectName, String fileName) {
        SWTBotTree packageExplorerTree = bot.viewByTitle("Package Explorer").bot().tree();

        // splitting on "/";
        String [] segments = fileName.split("/");
        SWTBotTreeItem prj = packageExplorerTree.getTreeItem(projectName);
        boolean found = false;
        long elapsed = 0;
        prj.expand();
        prj.setFocus();

        // AR -I need to wait for the correct display of the tree or it won't work! 
        // TODO find another way?
        while (!found && elapsed < TIMEOUT_FIND_ITEM_IN_PROJECT) {
            try {
                prj.expandNode(segments).doubleClick();
                found = true;
            } catch (WidgetNotFoundException e) {
                Logger.getLogger(this.getClass()).debug("Caught and handled exception: " + e.getMessage());
                bot.sleep(500);
                elapsed += 500;
            } catch (SWTException e) {
                Logger.getLogger(this.getClass()).debug("Caught and handled exception: " + e.getMessage());
                bot.sleep(500);
                elapsed += 500;
            } catch (TimeoutException e) {
                Logger.getLogger(this.getClass()).debug("Caught and handled exception: " + e.getMessage());
                bot.sleep(500);
                elapsed += 500;
            }
        }
        if (found == false) {
            throw new WidgetNotFoundException("Could not find the file " + fileName + " in " + projectName);
        }
        return this;
    }

    /**
     * The welcome screen is shown only the first time we execute Eclipse.
     * The problem is, we don't know which test will be executed first,
     * therefore all but the first test should ignore the welcome screen.
     * This is why the exceptions inside here are not rethrown.
     * @return
     */
	public BotUtils quietlyCloseWelcome() {
	    try {
	        SWTBotView v = bot.viewByTitle("Welcome");
	        if (v != null) {
	            v.close();
	        }
	    } catch (WidgetNotFoundException e) {
	        Logger.getLogger(this.getClass()).info("Caught exception in quietlyCloseWelcome: " + e.getMessage());
	    } catch (SWTException e) {
            Logger.getLogger(this.getClass()).info("Caught exception in quietlyCloseWelcome: " + e.getMessage());
        } catch (TimeoutException e) {
            Logger.getLogger(this.getClass()).info("Caught exception in quietlyCloseWelcome: " + e.getMessage());
        }
		return this;
		
	}
	public SWTBotMenu menu(String menu, String... subMenus) {
	    mainShell.setFocus();
		SWTBotMenu ret = bot.menu(menu);
		for (String subMenu: subMenus) {
			ret = ret.menu(subMenu);
		}
		return ret;
	}

	public <T extends Widget> SWTBotMenu contextMenu(AbstractSWTBot<T> node, String menu, String... subMenus) {
	    // AR - quick and dirty, could not find a way with waitForMenu or anything else
	    node.setFocus();
	    SWTBotMenu m = null;
        boolean found = false;
        long elapsed = 0;

        while (!found && elapsed < 5000) { // standard SWTBot timeout
            try {
                m = node.contextMenu(menu);
                for (String subMenu: subMenus) {
                    m = m.menu(subMenu);
                }
                found = true;
            } catch (WidgetNotFoundException e) {
                Logger.getLogger(this.getClass()).debug("Caught and handled exception: " + e.getMessage());
                bot.sleep(250);
                elapsed += 250;
            } catch (SWTException e) {
                Logger.getLogger(this.getClass()).debug("Caught and handled exception: " + e.getMessage());
                bot.sleep(500);
                elapsed += 500;
            } catch (TimeoutException e) {
                Logger.getLogger(this.getClass()).debug("Caught and handled exception: " + e.getMessage());
                bot.sleep(500);
                elapsed += 500;
            }
        }
        if (found == false) {
            throw new WidgetNotFoundException("Could not find menu: " + MenuLabels.LEININGEN);
        }
        return m;
    }

	public BotUtils activateShell(String shell) {
		SWTBotShell s = bot.shell(shell);
		s.activate();
		return this;
	}
	
	public BotUtils waitForProject(String projName) {
	    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
	    return waitForResource(project);
	}

	public BotUtils waitForResource(IResource resource) {
        boolean isSync = false;
        while (isSync) {
            bot.sleep(500);
            isSync = resource.isSynchronized(IResource.DEPTH_INFINITE);
        }
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

	public BotUtils createAndWaitForProject(String projectName) {
	    return createClojureProject(projectName)
               .waitForWorkspace()
               .quietlySendUpdateDependenciesToBackground()
               .waitForProject(projectName);
	}

    public <T extends Widget> BotUtils sendToBackground(Matcher<T> matcher, long timeout, long delay) {
        bot.waitUntil(Conditions.waitForWidget(matcher), timeout, delay);
        bot.button("Run in Background").click();
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
     * Quietly sends the UpdateDependencies widget to the background.
     * Funnily this widget does not always appear and therefore this 
     * method ignores SWTBot exceptions.
     * @return
     */
    public <T extends Widget> BotUtils quietlySendUpdateDependenciesToBackground() {
        try {
            sendToBackground(MATCHER_WIDGET_UPDATE_DEPENDENCIES, TIMEOUT_UPDATE_DEPENDENCIES);
        } catch (WidgetNotFoundException e) {
            Logger.getLogger(this.getClass()).info("Caught exception in quietlySendUpdateDependenciesToBackground: " + e.getMessage());
        } catch (SWTException e) {
            Logger.getLogger(this.getClass()).info("Caught exception in quietlySendUpdateDependenciesToBackground: " + e.getMessage());
        } catch (TimeoutException e) {
            Logger.getLogger(this.getClass()).info("Caught exception in quietlySendUpdateDependenciesToBackground: " + e.getMessage());
        }
        return this;
    }

    public BotUtils selectInClojureMenu(String entryLabel) throws Exception {
        menu("Clojure", entryLabel).click();
        return this;
    }

    public BotUtils clickInLeiningenMenuForProject(String projectName, String...labels) {
        SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
        SWTBotTree projectsTree = packageExplorer.bot().tree();
        SWTBotTreeItem node = projectsTree.getTreeItem(projectName);

        contextMenu(node, MenuLabels.LEININGEN, labels).click();
        return this;
    }

    public BotUtils waitForRepl() throws Exception {
        bot.waitUntil(Conditions.waitForWidget(MATCHER_WIDGET_REPL_LOG), TIMEOUT_REPL);
        return this;
    }

    public BotUtils closeRepl() throws Exception {
        bot.viewByPartName(NAME_REPLVIEW).close();
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

    /**
     * The warning of not synced resources can o cannot appear,
     * that is why the SWTBot exceptions here are not rethrown.
     * @return
     */
    public BotUtils quietlyContinuingIfNotInSync() {
        try {
            bot.button("Continue").click();
        } catch (WidgetNotFoundException e) {
            Logger.getLogger(this.getClass()).info("Caught exception in quietlyContinuingIfNotInSync: " + e.getMessage());
        } catch (SWTException e) {
            Logger.getLogger(this.getClass()).info("Caught exception in quietlyContinuingIfNotInSync: " + e.getMessage());
        } catch (TimeoutException e) {
            Logger.getLogger(this.getClass()).info("Caught exception in quietlyContinuingIfNotInSync: " + e.getMessage());
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
