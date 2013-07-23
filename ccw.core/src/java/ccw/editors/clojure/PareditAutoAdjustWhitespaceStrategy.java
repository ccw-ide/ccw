package ccw.editors.clojure;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

import ccw.CCWPlugin;
import ccw.util.ClojureInvoker;
import clojure.lang.Ref;

public class PareditAutoAdjustWhitespaceStrategy implements IAutoEditStrategy {
	private final ClojureInvoker pareditAutoAdjustWhitespacetrategyImpl = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.PareditAutoAdjustWhitespaceStrategyImpl");

	public final Ref state;
	
	public PareditAutoAdjustWhitespaceStrategy(IClojureEditor editor, IPreferenceStore prefs) {
		state = (Ref) pareditAutoAdjustWhitespacetrategyImpl._("init", editor, prefs);
	}
	
	public void customizeDocumentCommand(IDocument document,
			DocumentCommand command) {
		pareditAutoAdjustWhitespacetrategyImpl._("customizeDocumentCommand", 
				this, document, command);
	}
}
