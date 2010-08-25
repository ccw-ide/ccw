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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        int caretOffset = editor.getUnSignedSelection(editor.sourceViewer()).getOffset();
        // TODO factorize with ClojureHyperlinkDetector concerning the retrieval of the symbol ...
        Tokens tokens = new Tokens(editor.getDocument(), caretOffset);
        tokens.tokenAtCaret();
        String tokenContents = tokens.tokenContents();
        
        Map<String, Object> decl = findDecl(tokenContents, editor);
        String file = (String) decl.get("file");
        Integer line = (Integer) decl.get("line");
        String ns = (String) decl.get("ns");
        ClojureCore.openInEditor(ns, file, line);
    }
    
    /**
     * @return a map with keys: "file": filename, "line": line in file, 
     *         "ns": searched namespace
     */
    @SuppressWarnings("unchecked")
	public static Map<String, Object> findDecl(String tokenContents, AntlrBasedClojureEditor editor) {
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
            return null;
        }
        PersistentArrayMap result2 = (PersistentArrayMap) clojure.remoteLoadRead(command);
        List<String> result = (List<String>) result2.get("response");
        if (result == null || result.isEmpty() || result2.get("response-type").equals(-1)) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Cannot_find_declaration);
            return null;
        }
        Map<String, Object> r = new HashMap<String, Object>(3);
        r.put("file", result.get(0));
        r.put("line", (Integer) Integer.valueOf(result.get(1)));
        r.put("ns", result.get(3));
        return r;
    }
}
