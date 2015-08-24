/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Andrea RICHIARDI - Initial creation
 *******************************************************************************/
package ccw.editors.clojure.text;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

import ccw.editors.clojure.IClojureEditor;

/**
 * The Clojure Reconciler, which extends MonoReconciler and adds some part life-cycle logic
 */
public class ClojureReconciler extends MonoReconciler {

    /** The reconciler's editor */
    private IClojureEditor editor;
    /** The part listener */
    private IPartListener partListener;
    /** The shell listener */
    private ShellListener activationListener;

    /** Is the editor active? */
    private AtomicBoolean isEditorActive;
    
    public ClojureReconciler(IClojureEditor editor, IReconcilingStrategy strategy, boolean isIncremental) {
        super(strategy, isIncremental);
        this.editor= editor;
        isEditorActive = new AtomicBoolean();
    }

    @Override
    protected synchronized void startReconciling() {
        if (isEditorActive.get()) {
            super.startReconciling();
        }
    }
    
    @Override
    public void install(ITextViewer textViewer) {
        super.install(textViewer);
        
        partListener= new PartListener();
        IWorkbenchPartSite site = ((ITextEditor) editor).getSite();
        IWorkbenchWindow window= site.getWorkbenchWindow();
        window.getPartService().addPartListener(partListener);

        activationListener = new ActivationListener(textViewer.getTextWidget());
        Shell shell= window.getShell();
        shell.addShellListener(activationListener);
        
    }

    @Override
    public void uninstall() {
        super.uninstall();

        IWorkbenchPartSite site = ((ITextEditor) editor).getSite();
        IWorkbenchWindow window= site.getWorkbenchWindow();
        window.getPartService().removePartListener(partListener);
        partListener= null;

        Shell shell= window.getShell();
        if (shell != null && !shell.isDisposed()) {
            shell.removeShellListener(activationListener);
        }
        activationListener= null;
    }

    /**
     * From org.eclipse.jdt, part listener for activating the reconciler.
     */
    private class PartListener implements IPartListener {

        /*
         * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
         */
        @Override
        public void partActivated(IWorkbenchPart part) {
            if (part == editor) {
                isEditorActive.set(true);
                startReconciling();
            }
        }

        /*
         * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
         */
        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
        }

        /*
         * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
         */
        @Override
        public void partClosed(IWorkbenchPart part) {
        }

        /*
         * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
         */
        @Override
        public void partDeactivated(IWorkbenchPart part) {
            if (part == editor) {
                isEditorActive.set(false);
            }
            
        }

        /*
         * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
         */
        @Override
        public void partOpened(IWorkbenchPart part) {
        }
    }

    /**
     * From org.eclipse.jdt, shell activation listener for activating the reconciler.
     */
    private class ActivationListener extends ShellAdapter {

        private Control fControl;

        public ActivationListener(Control control) {
            fControl= control;
        }

        /*
         * @see org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events.ShellEvent)
         */
        @Override
        public void shellActivated(ShellEvent e) {
            if (!fControl.isDisposed() && fControl.isVisible()) {
                isEditorActive.set(true);
                startReconciling();
            }
        }

        /*
         * @see org.eclipse.swt.events.ShellListener#shellDeactivated(org.eclipse.swt.events.ShellEvent)
         */
        @Override
        public void shellDeactivated(ShellEvent e) {
            if (!fControl.isDisposed() && fControl.getShell() == e.getSource()) {
                isEditorActive.set(false);
            }
        }
    }
}
