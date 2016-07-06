package ccw.editors.clojure;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.presentation.IPresentationDamager;

import ccw.CCWPlugin;
import ccw.util.ClojureInvoker;
import clojure.lang.Ref;

public class ClojureTopLevelFormsDamager implements IPresentationDamager {
	private final ClojureInvoker topLevelFormsDamager = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.ClojureTopLevelFormsDamagerImpl");

	public final Ref state;
	
	public ClojureTopLevelFormsDamager(IClojureEditor editor) {
		state = (Ref) topLevelFormsDamager.__("init", editor);
	}
	
	public void setDocument(IDocument document) {
		topLevelFormsDamager.__("setDocument", 
				this, document);
	}

	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event,
			boolean documentPartitioningChanged) {
		return (IRegion) topLevelFormsDamager.__("getDamageRegion",
				this, partition, event, documentPartitioningChanged);
	}
}
