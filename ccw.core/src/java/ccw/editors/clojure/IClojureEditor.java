package ccw.editors.clojure;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.annotation.NonNull;

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
     * Returns an IClojureSourceViewer instance.
     * @return Instance or null.
     */
    @NonNull IClojureSourceViewer sourceViewer();
}
