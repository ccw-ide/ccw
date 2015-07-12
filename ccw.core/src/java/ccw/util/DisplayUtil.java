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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import ccw.CCWPlugin;

public class DisplayUtil {

	public static boolean isUIThread() {
		return Display.getCurrent() != null;
	}

	public static void asyncExec(Runnable r) {
	    boolean launched = false;
	    
	    IWorkbench workbench = PlatformUI.getWorkbench();
	    if (workbench != null) {
	        Display display = workbench.getDisplay();
	        if (display != null && !display.isDisposed()) {
	            display.asyncExec(r);
	            launched = true;
	        }
	    }
	    
	    if (launched == false) {
	        CCWPlugin.logWarning("Either the Workbench or the Display was null, cannot asyncExec runnable");
	    }
	}

	public static void syncExec(Runnable r) {
	    if (Display.getCurrent() == null) {
	        IWorkbench workbench = PlatformUI.getWorkbench();
	        if (workbench != null) {
	            Display display = workbench.getDisplay();
	            if (display != null && !display.isDisposed()) {
	                display.syncExec(r);
	            }
	        }
		} else {
			r.run();
		}
	}

	public static void beep() {
	    IWorkbench workbench = PlatformUI.getWorkbench();
	    if (workbench != null) {
	        Display display = workbench.getDisplay();
	        if (display != null && !display.isDisposed()) {
	            display.beep();
	        }
	    }
	}
}
