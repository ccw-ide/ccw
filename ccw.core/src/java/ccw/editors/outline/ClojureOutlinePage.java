/*******************************************************************************
 * Copyright (c) 2009 Manuel Woelker.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Manuel Woelker - initial prototype implementation
 *******************************************************************************/
package ccw.editors.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.editors.clojure.ClojureEditor;
import ccw.util.ClojureInvoker;
import ccw.util.DisplayUtil;
import clojure.lang.IMapEntry;
import clojure.lang.Keyword;
import clojure.lang.Obj;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class ClojureOutlinePage extends ContentOutlinePage {

	private final ClojureInvoker outline = ClojureInvoker.newInvoker(CCWPlugin.getDefault(), "ccw.editors.outline");

	private final class OutlineLabelProvider extends LabelProvider implements
			IStyledLabelProvider {

		private String getSymbol(List<?> list) {
			// TODO: smarter behavior when this is not a symbol
			String symbol = NOT_AVAILABLE;
			if (list.size() > 1) {
				symbol = safeToString(RT.second(list));
			}
			return symbol;
		}

		private String getKind(List<?> list) {
			// TODO: "smarter" behavior in general
			String kind = NOT_AVAILABLE;
			if (list.size() > 0) {
				kind = safeToString(RT.first(list));
			}
			return kind;
		}

		@Override
		public Image getImage(Object element) {
			// TODO: different images for macros, fns, etc.
		    // this is using results of reading analysis only... :-(
		    if (isPrivate((List)element)) {
		        return CCWPlugin.getDefault().getImageRegistry().get(CCWPlugin.PRIVATE_FUNCTION);
		    } else {
		        return CCWPlugin.getDefault().getImageRegistry().get(CCWPlugin.PUBLIC_FUNCTION);
		    }
		}

		@Override
		public StyledString getStyledText(Object element) {
			StyledString result;
			if (element instanceof List<?>) {
				List<?> list = (List<?>) element;
				result = new StyledString(getSymbol(list));
				StyledString kindString = new StyledString(
						" : " + getKind(list), //$NON-NLS-1$
						StyledString.QUALIFIER_STYLER);
				result.append(kindString);
			} else {
				// TODO: handle non-lists...
				result = new StyledString(safeToString(element));
			}
			return result;
		}

	}

	private final class DocumentChangedListener implements IDocumentListener {
		@Override
		public void documentChanged(DocumentEvent event) {
			refreshInput();
		}

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
	}

	private final class EditorSelectionChangedListener implements
			ISelectionChangedListener {

		private EditorSelectionChangedListener(TreeViewer viewer) {
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			selectInOutline(selection);

		}
	}

	private final class TreeSelectionChangedListener implements
			ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			if (isActivePart()) {
				// only when this is the active part, i.e. is user initiated
				selectInEditor(selection);
			}
		}
	}

	private static final String OUTLINE_VIEW_ID = "org.eclipse.ui.views.ContentOutline"; //$NON-NLS-1$
	private static final Keyword KEYWORD_LINE = Keyword.intern(null, "line"); //$NON-NLS-1$

	private final String NOT_AVAILABLE = "N/A"; //$NON-NLS-1$
	private final Object REFRESH_OUTLINE_JOB_FAMILY = new Object();
	private final IDocumentProvider documentProvider;
	private final ClojureEditor editor;

	private List<List> forms = new ArrayList<List>(0);
	private boolean sort = CCWPlugin.getDefault().getPreferenceStore().getBoolean("LexicalSortingAction.isChecked");

	private IDocument document;
	private TreeSelectionChangedListener treeSelectionChangedListener;
	private EditorSelectionChangedListener editorSelectionChangedListener;
	private DocumentChangedListener documentChangedListener;
	private ISelection lastSelection;
	private TreeViewer treeViewer;

	public ClojureOutlinePage(IDocumentProvider documentProvider,
			ClojureEditor editor) {
		this.documentProvider = documentProvider;
		this.editor = editor;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		treeViewer = getTreeViewer();
		treeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

			@Override
			public void dispose() {

			}

			@Override
			public Object[] getElements(Object input) {
				return ((List<?>) input).toArray();
			}

			@Override
			public boolean hasChildren(Object arg0) {
				return false;
			}

			@Override
			public Object getParent(Object arg0) {
				return null;
			}

			@Override
			public Object[] getChildren(Object arg0) {
				// TODO: handle children? Granularity, Bindings?
				return null;
			}
		});
		treeViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new OutlineLabelProvider()));
		treeViewer.addSelectionChangedListener(this);
		treeViewer.setInput(forms);
		treeSelectionChangedListener = new TreeSelectionChangedListener();
		treeViewer.addSelectionChangedListener(treeSelectionChangedListener);

		IPostSelectionProvider selectionProvider = (IPostSelectionProvider) editor
				.getSelectionProvider();
		editorSelectionChangedListener = new EditorSelectionChangedListener(
				treeViewer);
		selectionProvider.addPostSelectionChangedListener(editorSelectionChangedListener);
		ISelection selection = selectionProvider.getSelection();
		selectInOutline(selection);

		registerToolbarActions();
	}

	private void registerToolbarActions() {
		IActionBars actionBars = getSite().getActionBars();
		IToolBarManager toolBarManager= actionBars.getToolBarManager();
		toolBarManager.add(new LexicalSortingAction());
	}

	private class LexicalSortingAction extends Action {
		public LexicalSortingAction() {
			setText("Sort");
			setImageDescriptor(CCWPlugin.getDefault().getImageRegistry().getDescriptor(CCWPlugin.SORT));
			setToolTipText("Sort");
			setDescription("Sort alphabetically");
			setChecked(sort);
		}

		@Override
		public void run() {
		    CCWPlugin.getDefault().getPreferenceStore().setValue("LexicalSortingAction.isChecked", sort = isChecked());
		    setInputInUiThread(forms);
		}
	}

	private static Symbol symbol (Object o) {
	    return o instanceof Symbol ? (Symbol)o : null;
	}

	private static boolean isPrivate (List form) {
	    if (form.size() < 2) return false;
	    Symbol def = symbol(RT.first(form));
	    Symbol name = symbol(RT.second(form));
	    if (def == null || name == null) return false;
	    return def.getName().matches("(defn-|defvar-)") ||
	        (name.meta() != null && name.meta().valAt(Keyword.intern("private"), false).equals(Boolean.TRUE));
	}

	/**
	 * Find closest matching element to line
	 *
	 * @param toFind
	 *            line to find
	 * @return
	 */
	protected StructuredSelection findClosest(int toFind) {
		Object selected = null;
		for (Object o : forms) {
			if (o instanceof Obj) {
				Obj obj = (Obj) o;
				int lineNr = getLineNr(obj);
				if (lineNr >= 0 && lineNr <= toFind) {
					selected = obj;
				}

			}
		}
		if (selected != null) {
			return new StructuredSelection(selected);
		}
		return StructuredSelection.EMPTY;
	}

	public void setInput(IEditorInput editorInput) {
		document = documentProvider.getDocument(editorInput);
		documentChangedListener = new DocumentChangedListener();
		document.addDocumentListener(documentChangedListener);
		refreshInput();
		ISelection selection = editor.getSelectionProvider().getSelection();
		selectInOutline(selection);
	}

	private void refreshInput() {
		Job job = new Job("Outline browser tree refresh") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String string = document.get();
				try {
					ClojureOutlinePage.this.forms = (List<List>) outline._("read-forms", string);
					setInputInUiThread(ClojureOutlinePage.this.forms);
					return Status.OK_STATUS;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
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

	protected void setInputInUiThread (List<List> forms) {
	    if (sort) {
	        List<List> sorted = new ArrayList(forms);
            Collections.sort(sorted, new Comparator<List> () {
                @Override
				public int compare(List o1, List o2) {
                    Symbol s1 = symbol(RT.first(o1)),
                           s2 = symbol(RT.first(o2));

                    if (s1 == null && s2 == null) return 0; // mandatory for Comparator contract
                    if (s1 == null) return 1;
                    if (s2 == null) return -1;

                    if (s1.getName().equals("ns") && s2.getName().equals("ns")) {
                    	// fall through to order ns decls correctly
                    } else {
                    	if (s1.getName().equals("ns")) return -1;
                    	if (s2.getName().equals("ns")) return 1;
                    }

                    if (isPrivate(o1) != isPrivate(o2)) {
                        return isPrivate(o1) ? -1 : 1;
                    }

                    s1 = symbol(RT.second(o1));
                    s2 = symbol(RT.second(o2));

                    if (s1 == null && s2 == null) return 0; // mandatory for Comparator contract
                    if (s1 == null) return 1;
                    if (s2 == null) return -1;

                    return s1.getName().compareToIgnoreCase(s2.getName());
                }
            });
            forms = sorted;
	    }
	    final List<List> theForms = forms;

        DisplayUtil.asyncExec(new Runnable() {
            @Override
            public void run() {
                TreeViewer treeViewer = getTreeViewer();
                if (treeViewer != null) {
                    treeViewer.getTree().setRedraw(false);
                    treeViewer.setInput(theForms);
                    ISelection treeSelection = treeViewer.getSelection();
                    if (treeSelection == null || treeSelection.isEmpty()) {
                        selectInOutline(lastSelection);
                    }
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
		int lineNr = getLineNr(obj);
		if (lineNr >= 0) {
			ClojureCore.gotoEditorLine(editor, lineNr);
		}
	}

	private int getLineNr(Obj obj) {
		int lineNr = -1;
		if (obj.meta() == null) {
			return lineNr;
		}
		IMapEntry line = obj.meta().entryAt(KEYWORD_LINE);
		if (line != null && line.val() instanceof Number) {
			lineNr = ((Number) line.val()).intValue();
		}
		return lineNr;
	}

	private String safeToString(Object value) {
		try {
			return value == null ? NOT_AVAILABLE : value.toString();
		} catch (Exception e) {
			return NOT_AVAILABLE;
		}
	}

	protected boolean isActivePart() {
		IWorkbenchPart part = getSite().getPage().getActivePart();
		return part != null && OUTLINE_VIEW_ID.equals(part.getSite().getId());
	}

	@Override
	public void dispose() {
		try {
			if (document != null)
				document.removeDocumentListener(documentChangedListener);
		} catch (Throwable t) {}
		try {
			final TreeViewer viewer = getTreeViewer();
			if (viewer != null)
				viewer.removeSelectionChangedListener(this);
			if (viewer != null)
				viewer.removeSelectionChangedListener(treeSelectionChangedListener);
		} catch (Throwable t) {}
		try {
			IPostSelectionProvider selectionProvider = (IPostSelectionProvider) editor
					.getSelectionProvider();
			if (selectionProvider != null)
				selectionProvider
						.removePostSelectionChangedListener(editorSelectionChangedListener);
		} catch (Throwable t) {}
		super.dispose();
	}

	private void selectInOutline(ISelection selection) {
		TreeViewer viewer = getTreeViewer();
		lastSelection = selection;
		if (viewer != null && selection instanceof TextSelection) {
			TextSelection textSelection = (TextSelection) selection;
			int line = textSelection.getStartLine();
			StructuredSelection newSelection = findClosest(line + 1);
			ISelection oldSelection = viewer.getSelection();
			if (!newSelection.equals(oldSelection)) {
				viewer.setSelection(newSelection);
			}
		}
	}
}
