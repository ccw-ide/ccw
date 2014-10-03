package ccw.editors.clojure.strategies;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

import ccw.CCWPlugin;
import ccw.editors.clojure.IClojureEditor;
import ccw.util.ClojureInvoker;
import clojure.lang.Ref;

public class PareditAutoAdjustWhitespaceStrategy implements IAutoEditStrategy {

	private final ClojureInvoker pareditAutoEditSupport = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.paredit-auto-edit-support");
	
	private final ClojureInvoker pareditAutoAdjustWhitespacetrategyImpl = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.PareditAutoAdjustWhitespaceStrategyImpl");

	public final Ref state;
	
	public PareditAutoAdjustWhitespaceStrategy(IClojureEditor editor, IPreferenceStore prefs) {
		state = (Ref) pareditAutoEditSupport._("init", editor, prefs);
	}
	
	public void customizeDocumentCommand(IDocument document,
			DocumentCommand command) {
		pareditAutoAdjustWhitespacetrategyImpl._("customizeDocumentCommand", 
				this, document, command);
	}
}
