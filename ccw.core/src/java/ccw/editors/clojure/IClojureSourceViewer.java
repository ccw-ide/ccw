package ccw.editors.clojure;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Common interface of a SourceViewer in Counterclockwise.
 * @author Andrea Richiardi
 *
 */
public interface IClojureSourceViewer extends IClojureAwarePart, IReplAwarePart, ISourceViewer, IClojurePart {
    
    /**
     * Sets the preference store on this viewer.
     *
     * @param store the preference store
     */
    public void setPreferenceStore(IPreferenceStore store);
}
