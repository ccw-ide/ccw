package clojuredev.outline;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import clojuredev.ClojuredevPlugin;
import clojuredev.debug.IClojureClientProvider;

public class ClojureNSOutlinePage extends ContentOutlinePage {
	private final IClojureClientProvider clojureClientProvider;
	
	public ClojureNSOutlinePage(IClojureClientProvider clojureClientProvider) {
		this.clojureClientProvider = clojureClientProvider;
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getTreeViewer().setContentProvider(new ContentProvider());
		getTreeViewer().setLabelProvider(new LabelProvider());
		getTreeViewer().setSorter(new NSSorter());

		Object remoteTree = getRemoteNsTree();
		getTreeViewer().setInput(remoteTree);
	}
	
	private static class ContentProvider implements ITreeContentProvider {
		private Map<String, List<String>> input; 
		public Object[] getElements(Object inputElement) {
			return ((Map)inputElement).entrySet().toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.input = (Map<String, List<String>>) newInput;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Map) {
				return ((Map) parentElement).entrySet().toArray();
			} else if (parentElement instanceof Map.Entry) {
				return ((Map.Entry<String, List<String>>) parentElement).getValue().toArray();
			} else if (parentElement instanceof String) {
				return new Object[0];
			} else {
				return new Object[0];
			}
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object parentElement) {
			if (parentElement instanceof Map) {
				return ! ((Map) parentElement).isEmpty();
			} else if (parentElement instanceof Map.Entry) {
				return ! ((Map.Entry<String, List<String>>) parentElement).getValue().isEmpty();
			} else if (parentElement instanceof String) {
				return false;
			} else {
				return false;
			}
		}
		
	}
	
	private static class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof Map) {
				return "namespaces";
			} else if (element instanceof Map.Entry) {
				return ((Map.Entry<String, List<String>>) element).getKey();
			} else if (element instanceof String) {
				return (String) element;
			} else {
				return element.toString();
			}
		}
		@Override
		public Image getImage(Object element) {
			if (element instanceof Map) {
				return null;
			} else if (element instanceof Map.Entry) {
				return ClojuredevPlugin.getDefault().getImageRegistry().get(ClojuredevPlugin.NS);
			} else if (element instanceof String) {
				return ClojuredevPlugin.getDefault().getImageRegistry().get(ClojuredevPlugin.PUBLIC_FUNCTION);
			} else {
				return null;
			}
		}
	}
	
	private static class NSSorter extends ViewerSorter {
		
	}
	
	private Map<String, List<String>> getRemoteNsTree() {
		return (Map<String, List<String>>) 
			clojureClientProvider.getClojureClient().invoke(
				"(let [ns-names (map (comp str ns-name) (all-ns))" +
                "      ns-with-symbols (reduce (fn [m name]" + 
                "                                (assoc m name (apply vector (map (fn [s] (str s)) (keys (ns-interns (symbol name)))))))" +
                "                              {} ns-names)]" +
                "  ns-with-symbols)");	
	}
}
