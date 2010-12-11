/*******************************************************************************
 * Copyright (c) 2010 Tuomas KARKKAINEN.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Tuomas KARKKAINEN - initial API and implementation
 *******************************************************************************/
package ccw.editors.antlrbased;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.WorkbenchPart;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.repl.REPLView;
import ccw.util.PlatformUtil;
import clojure.tools.nrepl.Connection;

public class OpenDeclarationHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	IWorkbenchPart part = HandlerUtil.getActivePart(event);
    	IClojureEditor clojureEditor = (IClojureEditor) PlatformUtil.getAdapter(part, IClojureEditor.class);
    	if (clojureEditor == null) {
    		CCWPlugin.logWarning("Handler " + OpenDeclarationHandler.class.getSimpleName()
    				+ " executed on a IWorkbenchPart (id:" + part.getSite().getId() + ") which is not able to adapt to " + IClojureEditor.class.getSimpleName());
    		return null;
    	}
        int caretOffset = clojureEditor.getUnSignedSelection().getOffset();
        Tokens tokens = new Tokens(clojureEditor.getDocument(), caretOffset);
        tokens.tokenAtCaret();
        String tokenContents = tokens.tokenContents();
        List<String> split = Arrays.asList(tokenContents.split("/"));
        String symbol = tokenContents;
        String declaringNamespace = clojureEditor.getDeclaringNamespace();
        String namespace = null;
        if (split.size() == 2) {
            symbol = split.get(1);
            namespace = split.get(0);
        }
        String command = String.format("(ccw.debug.serverrepl/find-symbol \"%s\" \"%s\" \"%s\")", symbol, declaringNamespace, namespace);
        // TODO this isn't right, we should be using a REPL specifically for the project where the file is located
        REPLView replView = REPLView.activeREPL.get();
        if (replView == null || replView.isDisposed()) {
            clojureEditor.setStatusLineErrorMessage(ClojureEditorMessages.You_need_a_running_repl);
            return null;
        }
        Connection repl = replView.getToolingConnection();
        List values = repl.send(command).values();
        if (values.isEmpty()) {
            clojureEditor.setStatusLineErrorMessage(ClojureEditorMessages.Cannot_find_declaration);
        } else {
            List<String> result = (List<String>)values.get(0);
            String file = result.get(0);
            Integer line = Integer.valueOf(result.get(1));
            String ns = result.get(3);
            // TODO fix this (or not), the cast is ugly
            if ((clojureEditor instanceof WorkbenchPart) && file.endsWith(((WorkbenchPart) clojureEditor).getPartName())) {
                ClojureCore.gotoEditorLine(clojureEditor, line);
            } else {
                ClojureCore.openInEditor(ns, file, line);
            }
        }
        return null;
    }
}
