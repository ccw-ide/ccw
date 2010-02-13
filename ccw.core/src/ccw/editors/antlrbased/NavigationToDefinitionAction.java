package ccw.editors.antlrbased;

import java.util.Arrays;
import java.util.List;
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
        List<String> split = Arrays.asList(tokenContents.split("/"));
        String symbol = tokenContents;
        String namespace = editor.getDeclaringNamespace();
        if (split.size() == 2) {
            symbol = split.get(1);
            namespace = split.get(0);
        }
        String command = String.format("(ccw.debug.serverrepl/find-symbol \"%s\" \"%s\")", symbol, namespace);
        ClojureClient clojure = ClojureClient.newClientForActiveRepl();
        if (clojure==null) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.You_need_a_running_repl);
            return;            
        }
        PersistentArrayMap result2 = (PersistentArrayMap) clojure.remoteLoadRead(command);
        System.out.println("lol: " + editor.getDeclaringNamespace());
        System.out.println("searching for " + tokenContents + " --> " + result2);
        PersistentArrayMap result = (PersistentArrayMap) result2.get("response");
        if (result == null) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Cannot_find_definition);
            return;
        }
        String file = (String) result.get(Keyword.intern(null, "file"));
        String ns = (String) result.get(Keyword.intern(null, "ns"));
        Integer line = Integer.valueOf((String) result.get(Keyword.intern(null, "line")));
        if (file.endsWith(editor.getPartName())) {
            ClojureCore.gotoEditorLine(editor, line);
        } else {
            ClojureCore.openInEditor(ns, file, line);
        }
    }
}
