package ccw.editors.antlrbased;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import ccw.repl.REPLView;

/**
 * A callback interface allowing various facilities to treat
 * the {@link AntlrBasedClojureEditor} and {@link ClojureSourceViewer}
 * interchangeably (necessary because editors cannot be used within views,
 * but {@link ClojureSourceViewer} is needed for useful REPL presentation).
 */
public interface IClojureEditor {
	
	String KEY_BINDING_SCOPE = "ccw.ui.clojureEditorScope";   //$NON-NLS-1$
	
    /**
     * Corresponds to {@link AbstractTextEditor#selectAndReveal(int, int)}
     */
    void selectAndReveal (int start, int length);
    
    /**
     * Returns true if paredit-style strict structural editing is enabled.
     */
    boolean isStructuralEditingEnabled ();
    
    /**
     * Returns true only if the editor is in an "escape sequence", temporarily
     * disabling structural editing mode (if enabled).
     */
    boolean isInEscapeSequence ();

    // @todo -- what does "unsigned"/"signed" mean in this context?
    /**
     * Returns the unsigned current selection.
     * The length will always be positive.
     * <p>
     * The selection offset is model based.
     * </p>
     *
     * @return a region denoting the current unsigned selection
     */
    IRegion getUnSignedSelection ();
    
    /**
     * Returns the signed current selection.
     * The length will be negative if the resulting selection
     * is right-to-left (RtoL).
     * <p>
     * The selection offset is model based.
     * </p>
     *
     * @return a region denoting the current signed selection, for a resulting RtoL selections length is < 0
     */
    IRegion getSignedSelection ();
    
    /**
     * Returns the current namespace of the editor.
     */
    String getDeclaringNamespace ();

    /**
     * Returns the project associated with the editor, can be null.
     */
    IJavaProject getAssociatedProject ();

    Object getParsed ();
    
    /**
     * Can be null...
     */
    REPLView getCorrespondingREPL ();
    
    void updateTabsToSpacesConverter ();

	IDocument getDocument();

	void setStatusLineErrorMessage(String you_need_a_running_repl);

	void gotoMatchingBracket();
	
	DefaultCharacterPairMatcher getPairsMatcher();
}
