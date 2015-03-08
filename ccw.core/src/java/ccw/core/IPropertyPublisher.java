package ccw.core;

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * Protocol for objects that publish their properties to registered listeners.
 * @author Andrea Richiardi
 */
public interface IPropertyPublisher {

    void addPropertyChangeListener(IPropertyChangeListener listener);

    void removePropertyChangeListener(IPropertyChangeListener listener);

}