package clojuredev.outline;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import clojure.lang.Keyword;
import clojuredev.ClojureCore;
import clojuredev.ClojuredevPlugin;
import clojuredev.debug.ClojureClient;
import clojuredev.util.ClojureDocUtils;

public class NamespaceBrowser extends ViewPart implements ISelectionProvider, ISelectionChangedListener {
	/**
	 * The plugin prefix.
	 */
	public static final String PREFIX = ClojuredevPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	/**
	 * Help context id used for the content outline view (value
	 * <code>"org.eclipse.ui.content_outline_context"</code>).
	 */
	public static final String CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID = PREFIX + "content_outline_context";//$NON-NLS-1$

	private static final String NS_BROWSER_REFRESH_FAMILY = "clojuredev.outline.job.refresh";
	private static final Keyword KEYWORD_NAME = Keyword.intern(null, "name");
	private static final Keyword KEYWORD_CHILDREN = Keyword.intern(null, "children");
	private static final Keyword KEYWORD_TYPE = Keyword.intern(null, "type");
	public static final Keyword KEYWORD_PRIVATE = Keyword.intern(null, "private");
	public static final Keyword KEYWORD_NS = Keyword.intern(null, "ns");
	private static final Keyword KEYWORD_FILE = Keyword.intern(null, "file");
	private static final Keyword KEYWORD_LINE = Keyword.intern(null, "line");

	private ListenerList selectionChangedListeners = new ListenerList();

	private TreeViewer treeViewer;

	private ClojureClient clojureClient;

	private Composite control;
	private Text filterText;
	private String patternString = "";
	private Pattern pattern;
	private boolean searchInName = true;
	private boolean searchInDoc = false;
	private ISelection selectionBeforePatternSearchBegan;
	private Object[] expandedElementsBeforeSearchBegan;

	// /**
	// * Message to show on the default page.
	// */
	// private String defaultText =
	// ContentOutlineMessages.ContentOutline_noOutline;

	/**
	 * Creates a content outline view with no content outline pages.
	 */
	public NamespaceBrowser() {
		super();
	}

	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.setSelectionProvider(this);
	}

	/**
	 * The <code>PageBookView</code> implementation of this
	 * <code>IWorkbenchPart</code> method creates a <code>PageBook</code>
	 * control with its default page showing.
	 */
	public void createPartControl(Composite theParent) {
		control = new Composite(theParent, SWT.NONE);

		GridLayout gl = new GridLayout();
		gl.numColumns = 4;
		control.setLayout(gl);

		Label l = new Label(control, SWT.NONE);
		l.setText("Find :");
		l
				.setToolTipText("Enter an expression on which the browser will filter, based on name and doc string of symbols");
		GridData gd = new GridData();
		gd.verticalAlignment = SWT.CENTER;
		l.setLayoutData(gd);

		filterText = new Text(control, SWT.FILL | SWT.BORDER);
		filterText.setTextLimit(10);
		filterText
				.setToolTipText("Enter here a word to search. It can be a regexp. e.g. \"-map$\" (without double quotes) for matching strings ending with -map");
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.CENTER;
		filterText.setLayoutData(gd);
		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				patternString = ((Text) e.getSource()).getText();
				if ("".equals(patternString.trim())) {
					if (pattern != null) {
						// user stops search, we restore the previous state of
						// the tree
						pattern = null;
						delayedRefresh(false);
						treeViewer.setExpandedElements(expandedElementsBeforeSearchBegan);
						treeViewer.setSelection(selectionBeforePatternSearchBegan);
						selectionBeforePatternSearchBegan = null;
						expandedElementsBeforeSearchBegan = null;
					}
				} else {
					pattern = Pattern.compile(patternString.trim());
					if (selectionBeforePatternSearchBegan == null) {
						// user triggers search, we save the current state of
						// the tree
						selectionBeforePatternSearchBegan = treeViewer.getSelection();
						expandedElementsBeforeSearchBegan = treeViewer.getExpandedElements();
					}
					delayedRefresh(false);
					treeViewer.expandAll();
				}
			}
		});

		Button inName = new Button(control, SWT.CHECK);
		gd = new GridData();
		gd.verticalAlignment = SWT.CENTER;
		inName.setLayoutData(gd);
		inName.setText("in name");
		inName.setToolTipText("Press to enable the search in the name");
		inName.setSelection(true);
		inName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchInName = ((Button) e.getSource()).getSelection();
				treeViewer.refresh(false);
			}
		});

		Button inDoc = new Button(control, SWT.CHECK);
		gd = new GridData();
		gd.verticalAlignment = SWT.CENTER;
		inDoc.setLayoutData(gd);
		inDoc.setText("in doc");
		inDoc.setToolTipText("Press to enable the search in the documentation string");
		inDoc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchInDoc = ((Button) e.getSource()).getSelection();
				treeViewer.refresh(false);
			}
		});

		treeViewer = new TreeViewer(control, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.addSelectionChangedListener(this);
		gd = new GridData();// SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.horizontalSpan = 4;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		treeViewer.getControl().setLayoutData(gd);

		ColumnViewerToolTipSupport.enableFor(treeViewer);

		treeViewer.setContentProvider(new ContentProvider());
		treeViewer.setLabelProvider(new LabelProvider());

		treeViewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (patternString == null || patternString.trim().equals("")) {
					return true;
				}
				Map parent = (Map) parentElement;
				Map elem = (Map) element;
				if ("var".equals(elem.get(KEYWORD_TYPE))) {
					String name = (String) elem.get(KEYWORD_NAME);
					boolean nameMatches = searchInName && name != null && pattern.matcher(name).find();

					String doc = (String) elem.get(ClojureDocUtils.KEYWORD_DOC);
					boolean docMatches = searchInDoc && doc != null && pattern.matcher(doc).find();

					return nameMatches || docMatches;
				} else {
					return true;
				}
			}
		});

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.size() == 0)
					return;

				Map node = (Map) sel.getFirstElement();

				if (!"var".equals(node.get(KEYWORD_TYPE)))
					return;

				String searchedNS = ((String) node.get(KEYWORD_NS));
				String searchedFileName = (String) node.get(KEYWORD_FILE);
				int line = (node.get(KEYWORD_LINE) == null) ? -1 : Integer.valueOf((String) node.get(KEYWORD_LINE));
				ClojureCore.openInEditor(searchedNS, searchedFileName, line);
			}
		});
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getPageBook(),
		// CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID);
	}

	public Object getAdapter(Class key) {
		return super.getAdapter(key);
	}

	private static class ContentProvider implements ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			if (Map.class.isInstance(parentElement)) {
				Collection children = (Collection) ((Map) parentElement).get(KEYWORD_CHILDREN);
				if (children == null) {
					return new Object[0];
				} else {
					return children.toArray();
				}
			} else {
				return new Object[0];
			}
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object parentElement) {
			if (Map.class.isInstance(parentElement)) {
				return ((Map) parentElement).get(KEYWORD_CHILDREN) != null;
			} else {
				return false;
			}
		}
	}

	private static class LabelProvider extends CellLabelProvider {

		public String getToolTipText(Object element) {
			return ClojureDocUtils.getVarDocInfo(element);
		}

		public Point getToolTipShift(Object object) {
			return new Point(5, 15);
		}

		public int getToolTipDisplayDelayTime(Object object) {
			return 100;
		}

		public int getToolTipTimeDisplayed(Object object) {
			return 15000;
		}

		public void update(ViewerCell cell) {
			cell.setText(getText(cell.getElement()));
			cell.setImage(getImage(cell.getElement()));

		}

		private String getText(Object element) {
			if (Map.class.isInstance(element)) {
				return (String) ((Map) element).get(KEYWORD_NAME);
			} else {
				return element.toString();
			}
		}

		private Image getImage(Object element) {
			if (Map.class.isInstance(element)) {
				Map node = (Map) element;
				if ("ns".equals(node.get(KEYWORD_TYPE))) {
					return ClojuredevPlugin.getDefault().getImageRegistry().get(ClojuredevPlugin.NS);
				} else {
					if ("true".equals(node.get(KEYWORD_PRIVATE))) {
						return ClojuredevPlugin.getDefault().getImageRegistry().get(ClojuredevPlugin.PRIVATE_FUNCTION);
					} else {
						return ClojuredevPlugin.getDefault().getImageRegistry().get(ClojuredevPlugin.PUBLIC_FUNCTION);
					}
				}
			}
			return null;
		}
	}

	private static class NSSorter extends ViewerSorter {

	}

	private Map<String, List<String>> getRemoteNsTree() {
		if (clojureClient == null)
			return Collections.emptyMap();
		
		Map result = (Map) clojureClient.remoteLoadRead("(clojuredev.debug.serverrepl/namespaces-info)");
		if (result==null) {
			System.out.println("no result, connection problem");
			return null;
		}
		if (result.get("response-type").equals(0)) {
			return (Map<String, List<String>>) result.get("response");
		} else {
			// error detected
			Map error = (Map) result.get("response");
			System.out.println("error :"
					+ "line: " + error.get("line-number")
					+ "file: " + error.get("file-name")
					+ "message:" + error.get("message"));
			return null;
		}
//		
//		System.out.println("invokeStr called");
//		return (Map<String, List<String>>) result;
	}

	public void resetInput() {
		Job job = new Job("Namespace browser tree refresh") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (treeViewer == null) {
					return Status.CANCEL_STATUS;
				}

				Object oldInput = treeViewer.getInput();
				final Object newInput = getRemoteNsTree();
				if (oldInput != null && oldInput.equals(newInput)) {
					return Status.CANCEL_STATUS;
				}

				if (Display.getCurrent() == null) {
					final Display display = PlatformUI.getWorkbench().getDisplay();
					display.asyncExec(new Runnable() {
						public void run() {
							inUIResetInput(newInput);
						}
					});
				} else {
					inUIResetInput(newInput);
				}
				return Status.OK_STATUS;
			}
			@Override
			public boolean belongsTo(Object family) {
				return NS_BROWSER_REFRESH_FAMILY.equals(family);
			}
		};
		job.setSystem(true);
		Job.getJobManager().cancel(NS_BROWSER_REFRESH_FAMILY);
		job.schedule(200);
	}

	public void delayedRefresh(final boolean updateLabels) {
		Job job = new Job("Namespace browser tree refresh") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (treeViewer == null) {
					return Status.CANCEL_STATUS;
				}

				if (Display.getCurrent() == null) {
					final Display display = PlatformUI.getWorkbench().getDisplay();
					display.asyncExec(new Runnable() {
						public void run() {
							System.out.println("delayed refresh start");
							treeViewer.refresh(updateLabels);
							System.out.println("delayed refresh stop");
						}
					});
				} else {
					treeViewer.refresh(updateLabels);
				}
				return Status.OK_STATUS;
			}
			@Override
			public boolean belongsTo(Object family) {
				return NS_BROWSER_REFRESH_FAMILY.equals(family);
			}
		};
		job.setSystem(true);
		Job.getJobManager().cancel(NS_BROWSER_REFRESH_FAMILY);
		job.schedule(500);
	}

	private void inUIResetInput(Object newInput) {
		ISelection sel = treeViewer.getSelection();
		TreePath[] expandedTreePaths = treeViewer.getExpandedTreePaths();

		treeViewer.setInput(newInput);

		treeViewer.setExpandedTreePaths(expandedTreePaths);
		treeViewer.setSelection(sel);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	/**
	 * Fires a selection changed event.
	 * 
	 * @param selection
	 *            the new selection
	 */
	protected void fireSelectionChanged(ISelection selection) {
		// create an event
		final SelectionChangedEvent event = new SelectionChangedEvent(this, selection);

		// fire the event
		Object[] listeners = selectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					l.selectionChanged(event);
				}
			});
		}
	}

	public Control getControl() {
		if (control == null) {
			return null;
		}
		return control;
	}

	public ISelection getSelection() {
		if (treeViewer == null) {
			return StructuredSelection.EMPTY;
		}
		return treeViewer.getSelection();
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		fireSelectionChanged(event.getSelection());
	}

	/**
	 * Sets focus to a part in the page.
	 */
	public void setFocus() {
		if (treeViewer != null) {
			treeViewer.getControl().setFocus();
		}
	}

	public void setSelection(ISelection selection) {
		if (treeViewer != null) {
			treeViewer.setSelection(selection);
		}
	}

	/**
	 * @param clojureClient
	 */
	public static void setClojureClient(final ClojureClient clojureClient) {
		Display display = Display.getCurrent();
		if (display==null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					inUIThreadSetClojureClient(clojureClient);
				}
			});
		} else {
			inUIThreadSetClojureClient(clojureClient);
		}
	}
	
	private static void inUIThreadSetClojureClient(ClojureClient clojureClient) {
		IViewPart[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViews();			
		NamespaceBrowser co = null;
		for (IViewPart v: views) {
			if (NamespaceBrowser.class.isInstance(v)) {
				co = (NamespaceBrowser) v;
				break;
			}
		}
		if (co == null) {
			return;
		}

		if (clojureClient == null) {
			co.clojureClient = null;
			co.resetInput();
			return;
		}
		
		if (co.clojureClient!=null 
				&& co.clojureClient.getPort()==clojureClient.getPort()) {
			co.resetInput();
			return;
		}
		co.clojureClient = clojureClient;
		
		co.resetInput();
	}
	
}
