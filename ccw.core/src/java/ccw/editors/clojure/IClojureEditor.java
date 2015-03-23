package ccw.editors.clojure;

import org.eclipse.core.runtime.IAdaptable;

/**
 * A callback interface allowing various facilities to treat
 * the {@link ClojureEditor} and {@link ClojureSourceViewer}
 * interchangeably (necessary because editors cannot be used within views,
 * but {@link ClojureSourceViewer} is needed for useful REPL presentation).
 */
public interface IClojureEditor extends IClojureAwarePart, IReplAwarePart, IClojurePart, IAdaptable {
	
	String KEY_BINDING_SCOPE = "ccw.ui.clojureEditorScope";   //$NON-NLS-1$
	
	/**
	 * Install/uninstall the Tab-to-Space converter.
	 */
    void updateTabsToSpacesConverter();

	/**
     * Returns the ClojureSourceViewer, it should never be null.
     * @return Instance or null.
     */
    IClojureSourceViewer sourceViewer();
}
