/*******************************************************************************
 * Copyright (c) 2009 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class DisplayUtil {

	public static void asyncExec(Runnable r) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (!display.isDisposed()) {
			display.asyncExec(r);
		}
	}

	public static void syncExec(Runnable r) {
		if (Display.getCurrent() == null) {
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()) {
				display.syncExec(r);
			}
		} else {
			r.run();
		}
	}

}
