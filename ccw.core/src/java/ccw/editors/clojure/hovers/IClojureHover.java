package ccw.editors.clojure.hovers;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.information.IInformationProviderExtension2;

/**
 * The protocol for Counterclockwise hovers.
 * @author Andrea Richiardi
 *
 */
public interface IClojureHover extends ITextHover, ITextHoverExtension, ITextHoverExtension2, IInformationProviderExtension2 {
    /** Placeholder **/
}
