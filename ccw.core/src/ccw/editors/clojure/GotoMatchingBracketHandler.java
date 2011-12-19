/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.editors.clojure;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ccw.CCWPlugin;
import ccw.util.PlatformUtil;

public class GotoMatchingBracketHandler extends AbstractHandler {
	// TODO factoriser le fait de recuperer un clojureEditor
	public Object execute(ExecutionEvent event) throws ExecutionException {
    	IWorkbenchPart part = HandlerUtil.getActivePart(event);
    	IClojureEditor clojureEditor = (IClojureEditor) PlatformUtil.getAdapter(part, IClojureEditor.class);
    	if (clojureEditor == null) {
    		CCWPlugin.logWarning("Handler " + GotoMatchingBracketHandler.class.getSimpleName()
    				+ " executed on a IWorkbenchPart (id:" + part.getSite().getId() + ") which is not able to adapt to " + IClojureEditor.class.getSimpleName());
    		return null;
    	}
    	clojureEditor.gotoMatchingBracket();
		return null;
	}
}
