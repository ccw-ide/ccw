package clojuredev.outline;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import clojure.lang.Keyword;
import clojuredev.ClojuredevPlugin;
import clojuredev.debug.IClojureClientProvider;

public class ClojureNSOutlinePage extends ContentOutlinePage {
	private final IClojureClientProvider clojureClientProvider;
	
	private static final Keyword KEYWORD_NAME = Keyword.intern(null, "name");
	private static final Keyword KEYWORD_CHILDREN = Keyword.intern(null, "children");
	private static final Keyword KEYWORD_TYPE = Keyword.intern(null, "type");
    private static final Keyword KEYWORD_PRIVATE = Keyword.intern(null, "private");
    private static final Keyword KEYWORD_DOC = Keyword.intern(null, "doc");
    private static final Keyword KEYWORD_ARGLISTS = Keyword.intern(null, "arglists");
    
	
	
	public ClojureNSOutlinePage(IClojureClientProvider clojureClientProvider) {
		this.clojureClientProvider = clojureClientProvider;
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
        ColumnViewerToolTipSupport.enableFor(getTreeViewer());
        
		getTreeViewer().setContentProvider(new ContentProvider());
		getTreeViewer().setLabelProvider(new LabelProvider());
		
		getTreeViewer().setSorter(new NSSorter());
		getTreeViewer().setComparer(new IElementComparer() {
			public boolean equals(Object a, Object b) {
				if (a == b) {
					return true;
				}
				if ( (a==null && b!=null) || (b==null && a!=null) ) {
					return false;
				}

				if (a instanceof Map && b instanceof Map
				        && ((Map) a).get(KEYWORD_NAME)!=null && ((Map) b).get(KEYWORD_NAME)!=null) {
				    return ((Map) a).get(KEYWORD_NAME).equals(((Map) b).get(KEYWORD_NAME));
				} else {
					return a.equals(b);
				}
			}

			public int hashCode(Object element) {
				if (element == null) {
					return 0;
				}
				if ( element instanceof Map && ((Map) element).get(KEYWORD_NAME)!=null) {
					return ((Map) element).get(KEYWORD_NAME).hashCode();
				} else {
					return element.hashCode();
				}
			}
		});

		Object remoteTree = getRemoteNsTree();
//		getTreeViewer().setInput(remoteTree);
	}
	
	private static class ContentProvider implements ITreeContentProvider {
		private Object input; 
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.input = newInput;
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
            StringBuilder result = new StringBuilder();
            
            
            Object maybeArglist = ((Map) element).get(KEYWORD_ARGLISTS);
            if (maybeArglist != null) {
                result.append("arglists: ");
                result.append(maybeArglist);
            }
            
            Object maybeDoc = ((Map) element).get(KEYWORD_DOC);
            if (maybeDoc != null) {
                if (result.length() > 0) {
                    result.append("\n\n");
                }
                result.append(maybeDoc);
            }
            
            if (result.length() != 0) {
                return result.toString();
            } else {
                return "no documentation information";
            }
        }
	    
        public Point getToolTipShift(Object object) {
            return new Point(5,15);
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
		Object result = clojureClientProvider.getClojureClient().invokeStr("(clojuredev.debug.serverrepl/namespaces-info)");
		System.out.println("invokeStr called");
		return (Map<String, List<String>>) result;
	}

    public void refresh() {
    	if (getTreeViewer() == null) {
    		return;
    	}
    	
		Object oldInput = getTreeViewer().getInput();
    	final Object newInput = getRemoteNsTree();
		if (oldInput!=null && oldInput.equals(newInput)) {
			return;
		}
    	
        if (Display.getCurrent() == null) {
            final Display display = PlatformUI.getWorkbench().getDisplay();
            display.asyncExec(new Runnable() {
                public void run() {
                	refreshTreeViewer(newInput);
                }
            });
        } else {
        	refreshTreeViewer(newInput);
        }
    }
    private void refreshTreeViewer(Object newInput) {
    	ISelection sel = getTreeViewer().getSelection();
    	TreePath[] expandedTreePaths = getTreeViewer().getExpandedTreePaths();

        getTreeViewer().setInput(newInput);

        getTreeViewer().setExpandedTreePaths(expandedTreePaths);
        getTreeViewer().setSelection(sel);
    }
}
