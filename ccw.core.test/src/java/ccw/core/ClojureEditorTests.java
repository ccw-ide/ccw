package ccw.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ccw.util.DisplayUtil;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ClojureEditorTests {

	// TODO voir si on peut enlever le static
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
	public void canCreateANewClojureProject() throws Exception {
		bot
		.createClojureProject("editor-test");
		final IFile f = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("editor-test/src/editor_test/core.clj"));
		DisplayUtil.syncExec(new Runnable() {
			@Override public void run() {
				IEditorPart ep;
				try {
					ep = IDE.openEditor(bot.bot().activeView().getReference().getPage(), f);
					ep.setFocus();
					SWTBotEclipseEditor e = bot.bot().activeEditor().toTextEditor();
					e.insertText("salut laurent");
					e.saveAndClose();
					
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}});
	}

}
