package ccw.editors.clojure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;


public class SelectionHistory {

	private List fHistory;
	private final TextViewer fTextViewer;
	private ISelectionChangedListener fSelectionListener;
	private int fSelectionChangeListenerCounter;

	public SelectionHistory(TextViewer viewer) {
		Assert.isNotNull(viewer);
		fTextViewer= viewer;
		fHistory= new ArrayList(3);
		fSelectionListener= new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (fSelectionChangeListenerCounter == 0)
					flush();
			}
		};
		fTextViewer.getSelectionProvider().addSelectionChangedListener(fSelectionListener);
	}

	public boolean isEmpty() {
		return fHistory.isEmpty();
	}

	public void remember(ISourceRange range) {
//		System.out.println("asked to remember range:" + range);
		fHistory.add(range);
//		fHistoryAction.update(); TODO correctement
	}

	public ISourceRange getLast() {
		if (isEmpty())
			return null;
		int size= fHistory.size();
		ISourceRange result= (ISourceRange)fHistory.remove(size - 1);
//		fHistoryAction.update(); TODO correctement
		return result;
	}

	public void flush() {
		if (fHistory.isEmpty())
			return;
		fHistory.clear();
//		fHistoryAction.update(); TODO correctement
	}

	public void ignoreSelectionChanges() {
		fSelectionChangeListenerCounter++;
	}

	public void listenToSelectionChanges() {
		fSelectionChangeListenerCounter--;
	}

	public void dispose() {
	    fTextViewer.getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
	}
}
