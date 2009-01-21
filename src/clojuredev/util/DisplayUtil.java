package clojuredev.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class DisplayUtil {
	
	public static void asyncExec(Runnable r) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(r);
	}

	public static void syncExec(Runnable r) {
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(r);
		} else {
			r.run();
		}
	}

}
