package ccw.editors.antlrbased;

import org.eclipse.jface.action.Action;

import ccw.ClojureCore;
import ccw.debug.ClojureClient;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;

public class NavigationToDefinitionAction extends Action {
    public final static String ID = "NavigationToDefinitionAction"; //$NON-NLS-1$
    private final AntlrBasedClojureEditor editor;

    public NavigationToDefinitionAction(AntlrBasedClojureEditor editor) {
        this.editor = editor;
    }

    @Override
    public void run() {
        int caretOffset = editor.sourceViewer().getTextWidget().getCaretOffset();
        Tokens tokens = new Tokens(editor.getDocument(), caretOffset);
        String tokenContents = tokens.tokenContents();
        String[] split = tokenContents.split("/");
        String lastPart = split[split.length - 1];
        ClojureClient clojure = ClojureClient.newClientForActiveRepl();
        PersistentArrayMap result2 = (PersistentArrayMap) clojure.remoteLoadRead("(ccw.debug.serverrepl/find-symbol \"" + lastPart + "\")");
        System.out.println("searching for " + tokenContents + " --> " + result2);
        PersistentArrayMap result = (PersistentArrayMap) result2.get("response");
        String file = (String) result.get(Keyword.intern(null, "file"));
        String ns = (String) result.get(Keyword.intern(null, "ns"));
        Integer line = Integer.valueOf((String) result.get(Keyword.intern(null, "line")));
        ClojureCore.openInEditor(ns, file, line);
    }
}
