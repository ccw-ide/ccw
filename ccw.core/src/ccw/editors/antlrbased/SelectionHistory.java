package ccw.editors.antlrbased;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;


public class SelectionHistory {

	private List fHistory;
	private AntlrBasedClojureEditor fEditor;
	private ISelectionChangedListener fSelectionListener;
	private int fSelectionChangeListenerCounter;
	private StructureSelectHistoryAction fHistoryAction;

	public SelectionHistory(AntlrBasedClojureEditor editor) {
		Assert.isNotNull(editor);
		fEditor= editor;
		fHistory= new ArrayList(3);
		fSelectionListener= new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (fSelectionChangeListenerCounter == 0)
					flush();
			}
		};
		fEditor.getSelectionProvider().addSelectionChangedListener(fSelectionListener);
	}

	public void setHistoryAction(StructureSelectHistoryAction action) {
		Assert.isNotNull(action);
		fHistoryAction= action;
	}

	public boolean isEmpty() {
		return fHistory.isEmpty();
	}

	public void remember(ISourceRange range) {
		System.out.println("asked to remember range:" + range);
		fHistory.add(range);
		fHistoryAction.update();
	}

	public ISourceRange getLast() {
		if (isEmpty())
			return null;
		int size= fHistory.size();
		ISourceRange result= (ISourceRange)fHistory.remove(size - 1);
		fHistoryAction.update();
		return result;
	}

	public void flush() {
		if (fHistory.isEmpty())
			return;
		fHistory.clear();
		fHistoryAction.update();
	}

	public void ignoreSelectionChanges() {
		fSelectionChangeListenerCounter++;
	}

	public void listenToSelectionChanges() {
		fSelectionChangeListenerCounter--;
	}

	public void dispose() {
		fEditor.getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
	}
}
