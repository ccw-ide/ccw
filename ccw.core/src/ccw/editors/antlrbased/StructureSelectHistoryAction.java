package ccw.editors.antlrbased;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.texteditor.IUpdate;

public class StructureSelectHistoryAction extends Action implements IUpdate {
	private AntlrBasedClojureEditor fEditor;
	private SelectionHistory fHistory;

	public StructureSelectHistoryAction(AntlrBasedClojureEditor editor, SelectionHistory history) {
		super(ClojureEditorMessages.StructureSelectHistoryAction_label);
		//setToolTipText(SelectionActionMessages.StructureSelectHistory_tooltip);
		setDescription(ClojureEditorMessages.StructureSelectHistoryAction_description);
		Assert.isNotNull(history);
		Assert.isNotNull(editor);
		fHistory= history;
		fEditor= editor;
		update();
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.STRUCTURED_SELECTION_HISTORY_ACTION);
	}

	public void update() {
		setEnabled(!fHistory.isEmpty());
	}

	public void run() {
		ISourceRange old= fHistory.getLast();
		if (old != null) {
		System.out.println("Action: restoring last selection");
			try {
				fHistory.ignoreSelectionChanges();
				System.out.println("before restoring selection:" + old);
				fEditor.selectAndReveal(old.getOffset(), old.getLength());
				System.out.println("after restoring selection:" + old);
			} finally {
				fHistory.listenToSelectionChanges();
			}
		}
	}
}
