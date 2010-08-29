package ccw.editors.antlrbased;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import ccw.repl.REPLView;
import cemerick.nrepl.Connection;

/**
 * A callback interface allowing various facilities to treat
 * the {@link AntlrBasedClojureEditor} and {@link ClojureSourceViewer}
 * interchangeably (necessary because editors cannot be used within views,
 * but {@link ClojureSourceViewer} is needed for useful REPL presentation).
 */
public interface IClojureEditor {
    /**
     * Corresponds to {@link AbstractTextEditor#selectAndReveal(int, int)}
     */
    public void selectAndReveal (int start, int length);
    
    /**
     * Returns true if paredit-style strict structural editing is enabled.
     */
    public boolean isStructuralEditingEnabled ();
    
    /**
     * Returns true only if the editor is in an "escape sequence", temporarily
     * disabling structural editing mode (if enabled).
     */
    public boolean isInEscapeSequence ();

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
    public IRegion getUnSignedSelection ();
    
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
    public IRegion getSignedSelection ();
    
    /**
     * Returns the current namespace of the editor.
     */
    public String getDeclaringNamespace ();

    /**
     * Returns the project associated with the editor, can be null.
     */
    public IJavaProject getAssociatedProject ();

    /**
     * Can be null...
     */
    public REPLView getCorrespondingREPL ();
    
    public void setStructuralEditingPossible (boolean possible);
    
    public void updateTabsToSpacesConverter ();
}
