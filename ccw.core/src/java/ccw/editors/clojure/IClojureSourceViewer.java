package ccw.editors.clojure;

import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Common interface of a SourceViewer in Counterclockwise.
 * @author Andrea Richiardi
 *
 */
public interface IClojureSourceViewer extends IClojureAwarePart, IReplAwarePart, ISourceViewer, IClojurePart {
    /**
     * Initializes the viewer colors, adding them in the color cache.
     */
    void initializeViewerColors();
}
