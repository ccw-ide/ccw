/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.editors.clojure;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

/**
 * Common base class for action contributors for Clojure editors.
 */
public class BasicClojureEditorActionContributor extends BasicTextEditorActionContributor {

	private List<RetargetAction> partListeners= new ArrayList<RetargetAction>();

	private RetargetTextEditorAction gotoPreviousMember;
	private RetargetTextEditorAction gotoNextMember;

	public BasicClojureEditorActionContributor() {
		super();

		ResourceBundle b= ClojureEditorMessages.getBundleForConstructedKeys();

		gotoNextMember= new RetargetTextEditorAction( b, "GotoNextMember_"); //$NON-NLS-1$
		gotoNextMember.setActionDefinitionId(IClojureEditorActionDefinitionIds.GOTO_NEXT_MEMBER);

		gotoPreviousMember= new RetargetTextEditorAction( b, "GotoPreviousMember_"); //$NON-NLS-1$
		gotoPreviousMember.setActionDefinitionId(IClojureEditorActionDefinitionIds.GOTO_PREVIOUS_MEMBER);
	}

	protected final void markAsPartListener(RetargetAction action) {
		partListeners.add(action);
	}

	public void init(IActionBars bars, IWorkbenchPage page) {
		for (RetargetAction a: partListeners)
			page.addPartListener(a);

		super.init(bars, page);
	}

	public void contributeToMenu(IMenuManager menu) {

		super.contributeToMenu(menu);

//		IMenuManager gotoMenu= menu.findMenuUsingPath("navigate/goTo"); //$NON-NLS-1$
//		if (gotoMenu != null) {
//			gotoMenu.add(new Separator("additions2"));  //$NON-NLS-1$
//			gotoMenu.appendToGroup("additions2", gotoMatchingBracket); //$NON-NLS-1$
//		}
	}

	public void setActiveEditor(IEditorPart part) {

		super.setActiveEditor(part);

		ITextEditor textEditor= null;
		if (part instanceof ITextEditor)
			textEditor= (ITextEditor)part;

		IActionBars actionBars= getActionBars();
		IStatusLineManager manager= actionBars.getStatusLineManager();
		manager.setMessage(null);
		manager.setErrorMessage(null);

		/** The global actions to be connected with editor actions */
		IAction action= getAction(textEditor, ITextEditorActionConstants.NEXT);
		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, action);
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.NEXT, action);
		action= getAction(textEditor, ITextEditorActionConstants.PREVIOUS);
		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, action);
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.PREVIOUS, action);
	}

	public void dispose() {

		for (RetargetAction a: partListeners)
			getPage().removePartListener(a);

		partListeners.clear();

		setActiveEditor(null);
		super.dispose();
	}
}
