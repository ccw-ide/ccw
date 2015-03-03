package ccw.editors.clojure;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Interface for parts that are aware of Clojure code.
 * @author Andrea Richiardi
 *
 */
public interface IClojureAwarePart {

    /**
     * Corresponds to {@link AbstractTextEditor#selectAndReveal(int, int)}
     */
    void selectAndReveal (int start, int length);
    
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
     * 
     */
    IRegion getSignedSelection ();
    
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
     * Goes to the next matching bracket.
     */
    void gotoMatchingBracket();
    
    /**
     * Gets the PairMatcher of this SourceViewer
     * @return
     */
    DefaultCharacterPairMatcher getPairsMatcher();
    
    /**
     * Gets the selection history
     * @return
     */
    SelectionHistory getSelectionHistory();
    
    /**
     * Returns the current namespace of the editor.
     */
    String findDeclaringNamespace ();
    
    /**
     * Gets the parse state.
     * @return
     */
    Object getParseState ();
    
    /**
     * Gets the parse state of the previous step
     * @return
     */
    Object getPreviousParseTree ();
    
    /**
     * Returns true only if the editor is in an "escape sequence", temporarily
     * disabling structural editing mode (if enabled).
     */
    boolean isInEscapeSequence ();

    boolean isStructuralEditionPossible();

    void toggleStructuralEditionMode();

    boolean isShowRainbowParens();
    
    void toggleShowRainbowParens();

    /**
     * Force the viewer to consider the whole document as damaged, even if its
     * text has not changed.
     * e.g., useful to have syntax coloring adapt to new preference settings.
     */
    void markDamagedAndRedraw();
    
    /**
     * Forces the repairing of this part.
     * @return
     */
    boolean isForceRepair();
    
}
