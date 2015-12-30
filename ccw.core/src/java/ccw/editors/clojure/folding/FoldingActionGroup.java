/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrea RICHIARDI - CCW adjustments
 *******************************************************************************/
package ccw.editors.clojure.folding;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.editors.text.IFoldingCommandIds;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.TextOperationAction;

import ccw.CCWPlugin;
import ccw.editors.clojure.IClojureEditor;
import ccw.preferences.PreferenceConstants;
import ccw.util.ClojureInvoker;

/**
 * Groups the JDT folding actions (from org.eclipse.jdt).
 */
public class FoldingActionGroup extends ActionGroup {

    private final ClojureInvoker foldingSupport = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.editors.clojure.folding-support");

    //    private class FoldingAction extends PreferenceAction {
    //
    //        FoldingAction(ResourceBundle bundle, String prefix) {
    //            super(bundle, prefix, IAction.AS_PUSH_BUTTON);
    //        }
    //
    //        @Override
    //        public void update() {
    //            setEnabled(FoldingActionGroup.this.isEnabled() && fViewer.isProjectionMode());
    //        }
    //
    //    }

    private final @NonNull ITextEditor fEditor;
    private final @NonNull ProjectionViewer fViewer;
    private final @NonNull IPreferenceStore fPreferenceStore;

    private final ResourceAction fToggle;
    private final TextOperationAction fExpand;
    private final TextOperationAction fCollapse;
    private final TextOperationAction fExpandAll;
    private final TextOperationAction fCollapseAll;

    // TODO private final PreferenceAction fRestoreDefaults;
    // TODO private final FoldingAction fCollapseComments;

    private final IProjectionListener fProjectionListener;
    private final IPropertyChangeListener fPreferenceListener;

    /**
     * Creates a new projection action group for <code>editor</code>. If the
     * supplied viewer is not an instance of <code>ProjectionViewer</code>, the
     * action group is disabled.
     *
     * @param editor the text editor to operate on
     * @param viewer the viewer of the editor
     */
    public FoldingActionGroup(@NonNull ITextEditor editor, @NonNull ITextViewer viewer, @NonNull IPreferenceStore store) {
        fEditor = editor;
        fViewer = (ProjectionViewer) viewer;
        fPreferenceStore = store;
        
        fToggle = new ResourceAction(FoldingMessages.getResourceBundle(), "Projection_Toggle_", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
            @Override
            public void run() {
                if (fEditor instanceof IClojureEditor) {
                    IPreferenceStore store = CCWPlugin.getDefault().getPreferenceStore();
                    boolean current = store.getBoolean(PreferenceConstants.EDITOR_FOLDING_PROJECTION_ENABLED);
                    store.setValue(PreferenceConstants.EDITOR_FOLDING_PROJECTION_ENABLED, !current);
                }
            }
        };

        fToggle.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);
        editor.setAction("FoldingToggle", fToggle); //$NON-NLS-1$

        fExpandAll= new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection_ExpandAll_", editor, ProjectionViewer.EXPAND_ALL, true); //$NON-NLS-1$
        fExpandAll.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND_ALL);
        editor.setAction("FoldingExpandAll", fExpandAll); //$NON-NLS-1$

        fCollapseAll= new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection_CollapseAll_", editor, ProjectionViewer.COLLAPSE_ALL, true); //$NON-NLS-1$
        fCollapseAll.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE_ALL);
        editor.setAction("FoldingCollapseAll", fCollapseAll); //$NON-NLS-1$

        fExpand= new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection_Expand_", editor, ProjectionViewer.EXPAND, true); //$NON-NLS-1$
        fExpand.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND);
        editor.setAction("FoldingExpand", fExpand); //$NON-NLS-1$

        fCollapse= new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection_Collapse_", editor, ProjectionViewer.COLLAPSE, true); //$NON-NLS-1$
        fCollapse.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE);
        editor.setAction("FoldingCollapse", fCollapse); //$NON-NLS-1$

//        fRestoreDefaults= new FoldingAction(FoldingMessages.getResourceBundle(), "Projection_Restore_") { //$NON-NLS-1$
//            @Override
//            public void run() {
//                if (editor instanceof JavaEditor) {
//                    JavaEditor javaEditor= (JavaEditor) editor;
//                    javaEditor.resetProjection();
//                }
//            }
//        };
//        fRestoreDefaults.setActionDefinitionId(IFoldingCommandIds.FOLDING_RESTORE);
//        editor.setAction("FoldingRestore", fRestoreDefaults); //$NON-NLS-1$
//
//        fCollapseComments= new FoldingAction(FoldingMessages.getResourceBundle(), "Projection_CollapseComments_") { //$NON-NLS-1$
//            @Override
//            public void run() {
//                if (editor instanceof JavaEditor) {
//                    JavaEditor javaEditor= (JavaEditor) editor;
//                    javaEditor.collapseComments();
//                }
//            }
//        };
//        fCollapseComments.setActionDefinitionId(IJavaEditorActionDefinitionIds.FOLDING_COLLAPSE_COMMENTS);
//        editor.setAction("FoldingCollapseComments", fCollapseComments); //$NON-NLS-1$

        fProjectionListener = new IProjectionListener() {
            @Override
            public void projectionEnabled() {
                update();
            }

            @Override
            public void projectionDisabled() {
                update();
            }
        };
        fViewer.addProjectionListener(fProjectionListener);

        fPreferenceListener = new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(PreferenceConstants.EDITOR_FOLDING_PROJECTION_ENABLED)) {
                    update();
                }
            }
        };
        fPreferenceStore.addPropertyChangeListener(fPreferenceListener);
    }

    
    private boolean projectionEnabled() {
        return CCWPlugin.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.EDITOR_FOLDING_PROJECTION_ENABLED);
    }

    private boolean canProjectAndFold(ITextEditor editor) {
        Boolean descriptorsEnabled = (Boolean) foldingSupport._("any-descriptor-enabled?");
        return (descriptorsEnabled && projectionEnabled());
    }

    /*
     * @see org.eclipse.ui.actions.ActionGroup#dispose()
     */
    @Override
    public void dispose() {
        fViewer.removeProjectionListener(fProjectionListener);
        fPreferenceStore.removePropertyChangeListener(fPreferenceListener);
        super.dispose();
    }

    /**
     * Updates the actions if the group is enabled.
     */
    protected void update() {
        boolean canProjectAndFold = canProjectAndFold(fEditor);

        fToggle.setChecked(projectionEnabled());

        fExpand.update();
        fExpand.setEnabled(canProjectAndFold);
        fExpandAll.update();
        fExpandAll.setEnabled(canProjectAndFold);
        fCollapse.update();
        fCollapse.setEnabled(canProjectAndFold);
        fCollapseAll.update();
        fCollapseAll.setEnabled(canProjectAndFold);
        // fRestoreDefaults.update();
        // fCollapseComments.update();
    }

    /**
     * Fills the menu with all folding actions.
     *
     * @param manager the menu manager for the folding submenu
     */
    public void fillMenu(IMenuManager manager) {
        if (projectionEnabled()) {
            update();
            manager.add(fToggle);
            manager.add(fExpandAll);
            manager.add(fExpand);
            manager.add(fCollapse);
            manager.add(fCollapseAll);
            // manager.add(fRestoreDefaults);
            // manager.add(fCollapseComments);
        }
    }

    /*
     * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
     */
    @Override
    public void updateActionBars() {
        update();
    }
}
