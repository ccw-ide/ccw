package ccw.core;

import org.eclipse.swtbot.swt.finder.SWTBotAssert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestModeTests {

    public static BotUtils bot = null;

    @BeforeClass
    public static void setupClass() throws Exception {
        System.setProperty(StaticStrings.CCW_PROPERTY_TEST_MODE, "true");
        bot = new BotUtils();
    }

    @Test
    public void canShowTestGeneratorEntryInClojureMenu() throws Exception {
        SWTBotAssert.assertVisible(bot.menu("Clojure", "Test Generator..."));
    }
}
