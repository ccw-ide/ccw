package ccw.editors.clojure;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionProvider;

public interface IClojurePart {

    /**
     * Returns the project associated with the editor, can be null.
     */
    IJavaProject getAssociatedProject ();
    
    /**
     * Gets the selection provider
     * @return
     */
    ISelectionProvider getSelectionProvider();
    
    /**
     * Overrides the status line message (visibility widened from AbstractTextEditor).
     * @param msg A message.
     */
    void setStatusLineErrorMessage(String msg);
    
    /**
     * Gets the underlying document.
     * @return The Document instance
     */
    IDocument getDocument();
}
