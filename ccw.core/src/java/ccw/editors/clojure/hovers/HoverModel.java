package ccw.editors.clojure.hovers;

import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * The hover model. It is injected during the plugin initialization.
 * @author Andrea Richiardi
 *
 */
public interface HoverModel {
	
    /**
     * Retrieves a list of HoverDescriptor objects.
     * @return An ObservableList
     */
	IObservableList observableHoverDescriptors();

	/**
	 * Persists the input hovers.
	 */
	void persistHoverDescriptors(List<HoverDescriptor> descriptors);
}
