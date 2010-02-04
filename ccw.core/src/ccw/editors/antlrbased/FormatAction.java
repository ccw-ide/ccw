package ccw.editors.antlrbased;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IDocument;

public class FormatAction extends Action {
	private AntlrBasedClojureEditor editor;

	public FormatAction(AntlrBasedClojureEditor editor) {
		super(ClojureEditorMessages.FormatAction_label);
		Assert.isNotNull(editor);
		this.editor = editor;
		setEnabled(true);
	}

	@Override
	public void run() {
		IDocument document = editor.getDocument();
		String original = document.get();
		String formatted = new ClojureFormat().formatCode(original);
		if (!formatted.equals(original)) {
			document.set(formatted);
		}
	}
}
