package ccw.editors.clojure;


public abstract class AbstractHyperlinkDetector extends
		org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector {

	public Object getClassAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		return super.getAdapter(clazz);
	}

}
