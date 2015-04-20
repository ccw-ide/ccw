package ccw.editors.clojure.hovers;

import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;

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
