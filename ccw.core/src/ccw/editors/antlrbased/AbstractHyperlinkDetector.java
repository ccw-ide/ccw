package ccw.editors.antlrbased;


abstract public class AbstractHyperlinkDetector extends
		org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector {

	public Object getClassAdapter(Class clazz) {
		return super.getAdapter(clazz);
	}
	
}
