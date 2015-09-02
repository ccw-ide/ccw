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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

import ccw.editors.clojure.folding.FoldingActionGroup;

/**
 * Common base class for action contributors for Clojure editors.
 */
public class BasicClojureEditorActionContributor extends BasicTextEditorActionContributor {

	private List<RetargetAction> partListeners= new ArrayList<RetargetAction>();

	private RetargetTextEditorAction gotoPreviousMember;
	private RetargetTextEditorAction gotoNextMember;
	private RetargetTextEditorAction showInformationAction;
	
	/**
	 * The active editor part.
	 */
	private IEditorPart fActiveEditorPart;

	/**
	 * Status field definition.
	 * @see BasicTextEditorActionContributor
	 */
	private static class StatusFieldDef {

		private String category;
		private String actionId;
		private boolean visible;
		private int widthInChars;

		private StatusFieldDef(String category, String actionId, boolean visible, int widthInChars) {
			Assert.isNotNull(category);
			this.category= category;
			this.actionId= actionId;
			this.visible= visible;
			this.widthInChars= widthInChars;
		}
	}

	private final static StatusFieldDef[] STATUS_FIELD_DEFS = {
		new StatusFieldDef(ClojureSourceViewer.STATUS_CATEGORY_STRUCTURAL_EDITION,
				ClojureSourceViewer.STATUS_CATEGORY_STRUCTURAL_EDITION,
				true,
				ClojureSourceViewer.STATUS_STRUCTURAL_EDITION_CHARS_WIDTH)
	};
	
	/**
	 * The map of status fields.
	 * @see BasicTextEditorActionContributor
	 */
	private Map fStatusFields;

	public BasicClojureEditorActionContributor() {
		super();

		ResourceBundle b= ClojureEditorMessages.getBundleForConstructedKeys();

		showInformationAction= new RetargetTextEditorAction(b, "ShowInformation_"); //$NON-NLS-1$
		showInformationAction.setActionDefinitionId(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
		
		gotoNextMember= new RetargetTextEditorAction( b, "GotoNextMember_"); //$NON-NLS-1$
		gotoNextMember.setActionDefinitionId(IClojureEditorActionDefinitionIds.GOTO_NEXT_MEMBER);

		gotoPreviousMember= new RetargetTextEditorAction( b, "GotoPreviousMember_"); //$NON-NLS-1$
		gotoPreviousMember.setActionDefinitionId(IClojureEditorActionDefinitionIds.GOTO_PREVIOUS_MEMBER);

		fStatusFields= new HashMap(3);
		for (int i= 0; i < STATUS_FIELD_DEFS.length; i++) {
			StatusFieldDef fieldDef= STATUS_FIELD_DEFS[i];
			fStatusFields.put(fieldDef, new StatusLineContributionItem(fieldDef.category, fieldDef.visible, fieldDef.widthInChars));
		}
	
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

		IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
		    editMenu.appendToGroup(ITextEditorActionConstants.GROUP_INFORMATION, showInformationAction);
		}

//		IMenuManager gotoMenu= menu.findMenuUsingPath("navigate/goTo"); //$NON-NLS-1$
//		if (gotoMenu != null) {
//			gotoMenu.add(new Separator("additions2"));  //$NON-NLS-1$
//			gotoMenu.appendToGroup("additions2", gotoMatchingBracket); //$NON-NLS-1$
//		}
	}
	
	public void setActiveEditor(IEditorPart part) {

		if (fActiveEditorPart == part)
			return;

		fActiveEditorPart = part;

		super.setActiveEditor(part);
		
		if (fActiveEditorPart instanceof ITextEditorExtension) {
			ITextEditorExtension extension= (ITextEditorExtension) fActiveEditorPart;
			for (int i= 0; i < STATUS_FIELD_DEFS.length; i++)
				extension.setStatusField(null, STATUS_FIELD_DEFS[i].category);
		}
		
		
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

	
		for (int i= 0; i < STATUS_FIELD_DEFS.length; i++) {
			if (fActiveEditorPart instanceof ITextEditorExtension) {
				StatusLineContributionItem statusField= (StatusLineContributionItem) fStatusFields.get(STATUS_FIELD_DEFS[i]);
				statusField.setActionHandler(getAction(textEditor, STATUS_FIELD_DEFS[i].actionId));
				ITextEditorExtension extension= (ITextEditorExtension) fActiveEditorPart;
				extension.setStatusField(statusField, STATUS_FIELD_DEFS[i].category);
			}
		}
		
		showInformationAction.setAction(getAction(textEditor, ITextEditorActionConstants.SHOW_INFORMATION));

        if (part instanceof ClojureEditor) {
            FoldingActionGroup foldingActions= ((ClojureEditor)part).getFoldingActionGroup();
            if (foldingActions != null) {
                foldingActions.updateActionBars();
            }
        }
	}
	
	@Override
	public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		super.contributeToStatusLine(statusLineManager);
		for (int i= 0; i < STATUS_FIELD_DEFS.length; i++)
			statusLineManager.add((IContributionItem)fStatusFields.get(STATUS_FIELD_DEFS[i]));
	}
	

	public void dispose() {

		for (RetargetAction a: partListeners)
			getPage().removePartListener(a);

		partListeners.clear();

		setActiveEditor(null);
		super.dispose();
	}
}
