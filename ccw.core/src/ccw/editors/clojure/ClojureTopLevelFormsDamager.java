package ccw.editors.clojure;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.presentation.IPresentationDamager;

import ccw.CCWPlugin;
import ccw.util.ClojureUtils;
import clojure.lang.Ref;
import clojure.osgi.internal.ClojureOSGi;

public class ClojureTopLevelFormsDamager implements IPresentationDamager {
	private static final String ClojureTopLevelFormsDamagerImpl_NS = "ccw.editors.clojure.ClojureTopLevelFormsDamagerImpl";
	public final Ref state;
	
	static {
		ClojureOSGi.require(CCWPlugin.getDefault().getBundle(), ClojureTopLevelFormsDamagerImpl_NS);
	}
	
	public ClojureTopLevelFormsDamager(IClojureEditor editor) {
		state = (Ref) ClojureUtils.invoke(ClojureTopLevelFormsDamagerImpl_NS, "-init", editor);
	}
	
	public void setDocument(IDocument document) {
		ClojureUtils.invoke(ClojureTopLevelFormsDamagerImpl_NS, "-setDocument", 
				this, document);
	}

	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event,
			boolean documentPartitioningChanged) {
		return (IRegion) ClojureUtils.invoke(ClojureTopLevelFormsDamagerImpl_NS, "-getDamageRegion",
				this, partition, event, documentPartitioningChanged);
	}
}
