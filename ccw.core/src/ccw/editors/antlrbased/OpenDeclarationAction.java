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
import java.util.Map;

import org.eclipse.jface.action.Action;

import ccw.ClojureCore;
import ccw.repl.REPLView;
import cemerick.nrepl.Connection;

public class OpenDeclarationAction extends Action {
    public final static String ID = "OpenDeclarationAction"; //$NON-NLS-1$
    private final AntlrBasedClojureEditor editor;

    public OpenDeclarationAction(AntlrBasedClojureEditor editor) {
        this.editor = editor;
    }

    @Override
    public void run() {
        int caretOffset = editor.getUnSignedSelection().getOffset();
        Tokens tokens = new Tokens(editor.getDocument(), caretOffset);
        tokens.tokenAtCaret();
        String tokenContents = tokens.tokenContents();
        List<String> split = Arrays.asList(tokenContents.split("/"));
        String symbol = tokenContents;
        String declaringNamespace = editor.getDeclaringNamespace();
        String namespace = null;
        if (split.size() == 2) {
            symbol = split.get(1);
            namespace = split.get(0);
        }
        String command = String.format("(ccw.debug.serverrepl/find-symbol \"%s\" \"%s\" \"%s\")", symbol, declaringNamespace, namespace);
        // TODO this isn't right, we should be using a REPL specifically for the project where the file is located
        REPLView replView = REPLView.activeREPL.get();
        if (replView == null || replView.isDisposed()) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.You_need_a_running_repl);
            return;
        }
        Connection repl = replView.getToolingConnection();
        List values = repl.send(command).values();
        if (values.isEmpty()) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Cannot_find_declaration);
        } else {
            List<String> result = (List<String>)values.get(0);
            String file = result.get(0);
            Integer line = Integer.valueOf(result.get(1));
            String ns = result.get(3);
            if (file.endsWith(editor.getPartName())) {
                ClojureCore.gotoEditorLine(editor, line);
            } else {
                ClojureCore.openInEditor(ns, file, line);
            }
        }
    }
}
