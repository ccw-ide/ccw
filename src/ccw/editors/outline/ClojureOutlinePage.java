package ccw.editors.outline;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import ccw.ClojureCore;
import ccw.editors.antlrbased.AntlrBasedClojureEditor;
import clojure.lang.IMapEntry;
import clojure.lang.Keyword;
import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.LispReader;
import clojure.lang.Obj;
import clojure.lang.LispReader.ReaderException;

public class ClojureOutlinePage extends ContentOutlinePage {
	private static final Keyword KEYWORD_LINE = Keyword.intern(null, "line");
	private final Object REFRESH_OUTLINE_JOB_FAMILY = new Object();
	private final IDocumentProvider documentProvider;
	private final AntlrBasedClojureEditor editor;
	private List<Object> input = new ArrayList<Object>(0);

	private IDocument document;

	public ClojureOutlinePage(IDocumentProvider documentProvider,
			AntlrBasedClojureEditor editor) {
		this.documentProvider = documentProvider;
		this.editor = editor;

	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new ITreeContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

			public void dispose() {

			}

			public Object[] getElements(Object input) {
				return ((List<?>) input).toArray();
			}

			public boolean hasChildren(Object arg0) {
				return false;
			}

			public Object getParent(Object arg0) {
				return null;
			}

			public Object[] getChildren(Object arg0) {
				return null;
			}
		});
		viewer.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof List<?>) {
					List<?> list = (List<?>) element;
					if (list.size() > 1) {
						String kind = safeToString(list.get(0));
						String symbol = safeToString(list.get(1));
						return symbol + " : "+kind;
					}

				}
				return super.getText(element);
			}

		});
		viewer.addSelectionChangedListener(this);
		viewer.setInput(new ArrayList<Object>(input));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				selectInEditor(selection);
			}
		});

	}

	public void setInput(IEditorInput editorInput) {
		document = documentProvider.getDocument(editorInput);
		document.addDocumentListener(new IDocumentListener() {

			public void documentChanged(DocumentEvent event) {
				refreshInput();
			}

			public void documentAboutToBeChanged(DocumentEvent event) {
			}
		});
		refreshInput();
	}

	private void refreshInput() {
		Job job = new Job("Outline browser tree refresh") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String string = document.get();
				LineNumberingPushbackReader pushbackReader = new LineNumberingPushbackReader(
						new StringReader(string));
				Object EOF = new Object();
				ArrayList<Object> input = new ArrayList<Object>();
				Object result = null;
				while (true) {
					try {
						result = LispReader.read(pushbackReader, false, EOF, false);
						if(result == EOF) {
							break;
						}				
						input.add(result);
					} catch (ReaderException e) {
						// ignore, probably a syntax error
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				ClojureOutlinePage.this.input = input;
				setInputInUiThread();
				return Status.OK_STATUS;
			}
			@Override
			public boolean belongsTo(Object family) {
				return REFRESH_OUTLINE_JOB_FAMILY.equals(family);
			}
		};
		job.setSystem(true);
		Job.getJobManager().cancel(REFRESH_OUTLINE_JOB_FAMILY);
		job.schedule(500);		
	}

	protected void setInputInUiThread() {
		getControl().getDisplay().asyncExec(new Runnable() {
			
			public void run() {
				TreeViewer treeViewer = getTreeViewer();
				if (treeViewer != null) {
					treeViewer.getTree().setRedraw(false);
					treeViewer.setInput(input);
					treeViewer.getTree().setRedraw(true);
				}
			}
		});		
	}

	private void selectInEditor(ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection) selection;
		if (sel.size() == 0)
			return;

		Obj obj = (Obj) sel.getFirstElement();
		IMapEntry line = obj.meta().entryAt(KEYWORD_LINE);
		if (line != null && line.val() instanceof Number) {
			ClojureCore
					.gotoEditorLine(editor, ((Number) line.val()).intValue());
		}
	}
	private static String safeToString(Object value) {
		return value == null ? "N/A" : value.toString();
	}

}
