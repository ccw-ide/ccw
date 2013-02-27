package ccw.editors.clojure;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

import ccw.CCWPlugin;
import ccw.util.ClojureInvoker;
import clojure.lang.Ref;

public class PareditAutoEditStrategy implements IAutoEditStrategy {
	private final ClojureInvoker pareditAutoEditStrategyImpl = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.PareditAutoEditStrategyImpl");

	public final Ref state;
	
	public PareditAutoEditStrategy(IClojureEditor editor, IPreferenceStore prefs) {
		state = (Ref) pareditAutoEditStrategyImpl._("init", editor, prefs);
	}
	
	public void customizeDocumentCommand(IDocument document,
			DocumentCommand command) {
		pareditAutoEditStrategyImpl._("customizeDocumentCommand", 
				this, document, command);
	}
}
