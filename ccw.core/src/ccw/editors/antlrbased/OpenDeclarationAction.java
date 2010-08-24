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

import org.eclipse.jface.action.Action;

import ccw.ClojureCore;
import ccw.debug.ClojureClient;
import clojure.lang.PersistentArrayMap;

public class OpenDeclarationAction extends Action {
    public final static String ID = "OpenDeclarationAction"; //$NON-NLS-1$
    private final AntlrBasedClojureEditor editor;

    public OpenDeclarationAction(AntlrBasedClojureEditor editor) {
        this.editor = editor;
    }

    @Override
    public void run() {
    	// TODO factorize with ClojureHyperlinkDetector
        int caretOffset = editor.getUnSignedSelection(editor.sourceViewer()).getOffset();
        Tokens tokens = new Tokens(editor.getDocument(), caretOffset);
        tokens.tokenAtCaret();
        String tokenContents = tokens.tokenContents();
        
        run(tokenContents, editor);
    }
    
    public static void run(String tokenContents, AntlrBasedClojureEditor editor) {
        List<String> split = Arrays.asList(tokenContents.split("/"));
        String symbol = tokenContents;
        String declaringNamespace = editor.getDeclaringNamespace();
        String namespace = null;
        if (split.size() == 2) {
            symbol = split.get(1);
            namespace = split.get(0);
        }
        String command = String.format("(ccw.debug.serverrepl/find-symbol \"%s\" \"%s\" \"%s\")", symbol, declaringNamespace, namespace);
        ClojureClient clojure = ClojureClient.newClientForActiveRepl();
        if (clojure == null) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.You_need_a_running_repl);
            return;
        }
        PersistentArrayMap result2 = (PersistentArrayMap) clojure.remoteLoadRead(command);
        List<String> result = (List<String>) result2.get("response");
        if (result == null || result.isEmpty() || result2.get("response-type").equals(-1)) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Cannot_find_declaration);
            return;
        }
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
