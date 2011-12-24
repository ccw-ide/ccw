package ccw.editors.clojure;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

import ccw.CCWPlugin;
import ccw.util.ClojureUtils;
import clojure.lang.Ref;

public class PareditAutoEditStrategy implements IAutoEditStrategy {
	private static final String PareditAutoEditStrategyImpl_NS = "ccw.editors.clojure.PareditAutoEditStrategyImpl";
	public final Ref state;
	
	static {
		try {
			CCWPlugin.getClojureOSGi().require(CCWPlugin.getDefault().getBundle(), PareditAutoEditStrategyImpl_NS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public PareditAutoEditStrategy(IClojureEditor editor, IPreferenceStore prefs) {
		state = (Ref) ClojureUtils.invoke(PareditAutoEditStrategyImpl_NS, "init", editor, prefs);
	}
	
	public void customizeDocumentCommand(IDocument document,
			DocumentCommand command) {
		ClojureUtils.invoke(PareditAutoEditStrategyImpl_NS, "customizeDocumentCommand", 
				this, document, command);
	}
}
