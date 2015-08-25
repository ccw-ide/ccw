package ccw.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.swt.widgets.Widget;

import ccw.editors.clojure.IClojureEditor;

public final class TestUtil {

	/**
	 * Wrapper function to call for setting id on any kind of Widget, useful
	 * for testing purposes.
	 */
	public static <W extends Widget> W setTestId(W w, String id) {
		w.setData("org.eclipse.swtbot.widget.key", id);
		return w;
	}

	/**
     * Given a ClojureEditor, return a map Annotation keys to
     * Position values, got from its ProjectionAnnotationModel.
     * @param editor
     * @return A Map, potentially empty.
     */
    public static @NonNull Map<Annotation, Position> getProjectionMap(IClojureEditor editor) {
        Map<Annotation, Position> m = new HashMap<Annotation, Position>();

        ProjectionAnnotationModel model = editor.getProjectionAnnotationModel();
        Iterator annotations = model.getAnnotationIterator();
        while (annotations.hasNext()) {
            Annotation annotation = (Annotation) annotations.next();
            Position position = model.getPosition(annotation);
            m.put(annotation, position);
        }
        return m;
    }
}
