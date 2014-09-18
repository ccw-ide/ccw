package ccw.editors.clojure;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import ccw.repl.REPLView;

/**
 * A callback interface allowing various facilities to treat
 * the {@link ClojureEditor} and {@link ClojureSourceViewer}
 * interchangeably (necessary because editors cannot be used within views,
 * but {@link ClojureSourceViewer} is needed for useful REPL presentation).
 */
public interface IClojureEditor extends IAdaptable {
	
    String KEY_BINDING_SCOPE = "ccw.ui.clojureEditorScope";   //$NON-NLS-1$
    String STRUCT_KEY_BINDING_SCOPE = "ccw.ui.clojureStructEditorScope";
    /**
     * Corresponds to {@link AbstractTextEditor#selectAndReveal(int, int)}
     */
    void selectAndReveal (int start, int length);
    
    /**
     * Returns true if paredit-style strict structural editing is enabled.
     */
    boolean isStructuralEditingEnabled ();
    
    /**
     * Returns true if the user enabled the escaping of clipboard's content
     * when pasting inside String literals. 
     */
    boolean isEscapeInStringLiteralsEnabled();
    
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
    
	SelectionHistory getSelectionHistory();

    ISelectionProvider getSelectionProvider();

    /**
     * Returns the current namespace of the editor.
     */
    String findDeclaringNamespace ();

    /**
     * Returns the project associated with the editor, can be null.
     */
    IJavaProject getAssociatedProject ();

    Object getParseState ();
    Object getPreviousParseTree ();
    
    void updateTabsToSpacesConverter ();

	IDocument getDocument();

	void setStatusLineErrorMessage(String msg);

	void gotoMatchingBracket();
	
	DefaultCharacterPairMatcher getPairsMatcher();

	boolean isStructuralEditionPossible();

	void toggleStructuralEditionMode();

    void setMode(ClojureEditorMode mode);
    ClojureEditorMode getMode();

	boolean isShowRainbowParens();
	
	void toggleShowRainbowParens();

    /**
     * Force the viewer to consider the whole document as damaged, even if its
     * text has not changed.
     * e.g., useful to have syntax coloring adapt to new preference settings.
     */
	void markDamagedAndRedraw();
	
	boolean isForceRepair();
}
