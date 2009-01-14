/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package clojuredev.outline;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.ViewsPlugin;
import org.eclipse.ui.internal.views.contentoutline.ContentOutlineMessages;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import clojuredev.ClojuredevPlugin;

/**
 * Main class for the Content Outline View.
 * <p>
 * This standard view has id <code>"org.eclipse.ui.views.ContentOutline"</code>.
 * </p>
 * When a <b>content outline view</b> notices an editor being activated, it 
 * asks the editor whether it has a <b>content outline page</b> to include
 * in the outline view. This is done using <code>getAdapter</code>:
 * <pre>
 * IEditorPart editor = ...;
 * IContentOutlinePage outlinePage = (IContentOutlinePage) editor.getAdapter(IContentOutlinePage.class);
 * if (outlinePage != null) {
 *    // editor wishes to contribute outlinePage to content outline view
 * }
 * </pre>
 * If the editor supports a content outline page, the editor instantiates
 * and configures the page, and returns it. This page is then added to the 
 * content outline view (a pagebook which presents one page at a time) and 
 * immediately made the current page (the content outline view need not be
 * visible). If the editor does not support a content outline page, the content
 * outline view shows a special default page which makes it clear to the user
 * that the content outline view is disengaged. A content outline page is free
 * to report selection events; the content outline view forwards these events 
 * along to interested parties. When the content outline view notices a
 * different editor being activated, it flips to the editor's corresponding
 * content outline page. When the content outline view notices an editor being
 * closed, it destroys the editor's corresponding content outline page.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when a Content
 * Outline view is needed for a workbench window. This class was not intended
 * to be instantiated or subclassed by clients.
 * </p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ContentOutline extends PageBookView implements ISelectionProvider,
        ISelectionChangedListener {



    /**
     * The plugin prefix.
     */
    public static final String PREFIX = ClojuredevPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

    /**
     * Help context id used for the content outline view
     * (value <code>"org.eclipse.ui.content_outline_context"</code>).
     */
    public static final String CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID = PREFIX
            + "content_outline_context";//$NON-NLS-1$

    /**
     * Message to show on the default page.
     */
    private String defaultText =ContentOutlineMessages.ContentOutline_noOutline; 

    /**
     * Creates a content outline view with no content outline pages.
     */
    public ContentOutline() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        getSelectionProvider().addSelectionChangedListener(listener);
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected IPage createDefaultPage(PageBook book) {
        MessagePage page = new MessagePage();
        initPage(page);
        page.createControl(book);
        page.setMessage(defaultText);
        return page;
    }

    /**
     * The <code>PageBookView</code> implementation of this <code>IWorkbenchPart</code>
     * method creates a <code>PageBook</code> control with its default page showing.
     */
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getPageBook(),
                CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID);
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected PageRec doCreatePage(IWorkbenchPart part) {
        // Try to get an outline page.
        Object obj = ViewsPlugin.getAdapter(part, ClojureNSOutlinePage.class, false);
        if (obj instanceof ClojureNSOutlinePage) {
            ClojureNSOutlinePage page = (ClojureNSOutlinePage) obj;
            if (page instanceof IPageBookViewPage) {
				initPage((IPageBookViewPage) page);
			}
            page.createControl(getPageBook());
            return new PageRec(part, page);
        }
        // There is no content outline
        return null;
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
        IContentOutlinePage page = (IContentOutlinePage) rec.page;
        page.dispose();
        rec.dispose();
    }

    /* (non-Javadoc)
     * Method declared on IAdaptable.
     */
    public Object getAdapter(Class key) {
        if (key == IContributedContentsView.class) {
			return new IContributedContentsView() {
                public IWorkbenchPart getContributingPart() {
                    return getCurrentContributingPart();
                }
            };
		}
        return super.getAdapter(key);
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected IWorkbenchPart getBootstrapPart() {
//        IWorkbenchPage page = getSite().getPage();
//        if (page != null) {
//			return page.getActiveEditor();
//		}

        return null;
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public ISelection getSelection() {
        // get the selection from the selection provider
        return getSelectionProvider().getSelection();
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     * We only want to track editors.
     */
    protected boolean isImportant(IWorkbenchPart part) {
    	return true;
//        //We only care about editors
//        return (part instanceof IEditorPart);
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     * Treat this the same as part activation.
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        partActivated(part);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        getSelectionProvider().removeSelectionChangedListener(listener);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionChangedListener.
     */
    public void selectionChanged(SelectionChangedEvent event) {
        getSelectionProvider().selectionChanged(event);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void setSelection(ISelection selection) {
        getSelectionProvider().setSelection(selection);
    }

    /**
     * The <code>ContentOutline</code> implementation of this <code>PageBookView</code> method
     * extends the behavior of its parent to use the current page as a selection provider.
     * 
     * @param pageRec the page record containing the page to show
     */
    protected void showPageRec(PageRec pageRec) {
        IPageSite pageSite = getPageSite(pageRec.page);
        ISelectionProvider provider = pageSite.getSelectionProvider();
        if (provider == null && (pageRec.page instanceof IContentOutlinePage)) {
			// This means that the page did not set a provider during its initialization 
            // so for backward compatibility we will set the page itself as the provider.
            pageSite.setSelectionProvider((IContentOutlinePage) pageRec.page);
		}
        super.showPageRec(pageRec);
    }
}
