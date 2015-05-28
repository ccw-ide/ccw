package ccw.util;

import org.eclipse.swt.widgets.Widget;

public final class TestUtil {

	/**
	 * Wrapper function to call for setting id on any kind of Widget, useful
	 * for testing purposes.
	 */
	public static <W extends Widget> W setTestId(W w, String id) {
		w.setData("org.eclipse.swtbot.widget.key", id);
		return w;
	}
}
