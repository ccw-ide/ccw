/*******************************************************************************
 * Copyright (c) 2008 Casey Marshal.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshal - initial API and implementation
 *    Laurent PETIT - evolution and maintenance
 *******************************************************************************/
package clojuredev.launching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import clojuredev.ClojuredevPlugin;

/**
 * Heavily adapted from JDT's java launcher tabs.
 * 
 * @author cmarshal, laurent.petit
 * 
 */
/* 
 * TODO incoming additions: Repl settings:
 *  * auto-attribute server port. user set server port becomes an option
 *  * repl variants: clojure.lang.Repl , clojure.main , clojure.contrib.repl_ln
 *  * warn-on-reflection
 *  * (NOT DONE HERE, though): auto-completion as with the editor 
 */
@SuppressWarnings("restriction")
public class ClojureMainTab extends AbstractJavaMainTab implements IJavaLaunchConfigurationConstants {
	
    protected boolean useREPL = true;

    protected TableViewer sourceFilesViewer;
    
    private Button installREPLChoice; 
    private Text serverPort;
    
    public String getName() {
        return "Clojure";
    }

    public void createControl(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, parent.getFont(),
                1, 1, GridData.FILL_BOTH);
        ((GridLayout) comp.getLayout()).verticalSpacing = 0;
        createProjectEditor(comp);
        createVerticalSpacer(comp, 1);
        createFileEditor(comp, "Clojure File");
        createVerticalSpacer(comp, 1);
        createReplServerControl(comp);
        setControl(comp);
    }

    private void createFileEditor(final Composite parent, String string) {
        Group section = SWTFactory.createGroup(parent, "Evaluate Clojure source file(s)",
                2, 1, GridData.FILL_BOTH);
        
        sourceFilesViewer = new TableViewer(section);
        sourceFilesViewer.setLabelProvider(new DecoratingLabelProvider(
        		new WorkbenchLabelProvider(),ClojuredevPlugin.getDefault().getWorkbench()
        						.getDecoratorManager().getLabelDecorator()));
        sourceFilesViewer.setContentProvider(new ArrayContentProvider());
        sourceFilesViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite buttonSection = SWTFactory.createComposite(section, parent.getFont(),
                1, 1, GridData.FILL_BOTH);
        
        Button chooseButton = new Button(buttonSection, SWT.PUSH);
        chooseButton.setText("Choose...");
        chooseButton.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                String currentProjName = fProjText.getText().trim();
                final IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(currentProjName);
                if (proj == null) {
                    return;
                }
                
                CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(parent.getShell(), new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setInput(proj.getWorkspace().getRoot());
                dialog.addFilter(new ClojureElementViewerFilter(proj));
                
                if (sourceFilesViewer.getInput() != null) {
                    dialog.setInitialSelections(
                            ((List)sourceFilesViewer.getInput()).toArray());
                }
                dialog.setTitle("Evaluate Clojure source file(s)");
                dialog.open();
                
                List<IFile> selectedFiles = new ArrayList<IFile>();
                Object[] dialogResult = dialog.getResult();
                if (dialogResult != null) {
	                for (Object o : dialogResult) {
	                    if (o instanceof IFile) {
	                        selectedFiles.add((IFile)o);
	                    }
	                }
                }
                sourceFilesViewer.setInput(selectedFiles);
                updateLaunchConfigurationDialog();
            }
            
        });
    }

    private void createReplServerControl(final Composite parent) {
        Group section = SWTFactory.createGroup(parent, "Repl settings",
                2, 1, 0);
        
        installREPLChoice = SWTFactory.createCheckButton(
        		section, "Install a REPL (see tooltip for detail)", null, true, 2);
        installREPLChoice.setToolTipText("If checked, the main class will be clojure.contrib.repl_ln, all files listed will be loaded"
        		+ " with the -i option.\n If unchecked, the main class will be clojure.main, all files listed but the last will be loaded"
        		+ " with the -i option, and the last file will be loaded as a script.");
        
        SWTFactory.createLabel(section, "Remote server must listen on port: ", 1);
        serverPort = SWTFactory.createSingleText(section, 0);

        installREPLChoice.addSelectionListener( new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				serverPort.setEnabled(installREPLChoice.getSelection());
				updateLaunchConfigurationDialog();
			}
		});
        
        serverPort.addModifyListener( new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
    }
    
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        IJavaElement javaElement = getContext();
        if (javaElement != null) {
            initializeJavaProject(javaElement, config);
        } else {
            config.setAttribute(ATTR_PROJECT_NAME, EMPTY_STRING);
        }
        
        try {
        	if (config.getAttribute(ATTR_MAIN_TYPE_NAME, (String) null) == null) {
        		config.setAttribute(ATTR_MAIN_TYPE_NAME, LaunchUtils.MAIN_CLASSNAME_FOR_REPL);
        	}
        	config.setAttribute(LaunchUtils.ATTR_CLOJURE_INSTALL_REPL, true);
        	config.setAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, LaunchUtils.DEFAULT_SERVER_PORT);
            
            config.doSave();
        }
        catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initializeFrom(ILaunchConfiguration config) {
        super.initializeFrom(config);

        String currentProjName = fProjText.getText().trim();
        IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(currentProjName);
        if (proj == null) {
            return;
        }
        
        try {
        	installREPLChoice.setSelection(config.getAttribute(
        			LaunchUtils.ATTR_CLOJURE_INSTALL_REPL, true));
            serverPort.setText(Integer.toString(config.getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, LaunchUtils.DEFAULT_SERVER_PORT)));
            serverPort.setEnabled(installREPLChoice.getSelection());
        } catch (CoreException e) {
        	ClojuredevPlugin.logError("error while initializing serverPort", e);
        	installREPLChoice.setSelection(true);
        	serverPort.setEnabled(true);
        	serverPort.setText("");
        }
        try {    
        	sourceFilesViewer.setInput(LaunchUtils.getFilesToLaunchList(config));
        } catch (CoreException e) {
        	ClojuredevPlugin.logError("error while initializing file list", e);
        	sourceFilesViewer.setInput(Collections.emptyList());
        }
    }

    @SuppressWarnings("unchecked")
    public void performApply(ILaunchConfigurationWorkingCopy config) {
    	
        config.setAttribute(ATTR_PROJECT_NAME, fProjText.getText().trim());

        LaunchUtils.setFilesToLaunchString(config, (List<IFile>) sourceFilesViewer.getInput());

    	config.setAttribute(ATTR_MAIN_TYPE_NAME, 
    			installREPLChoice.getSelection() 
    				? LaunchUtils.MAIN_CLASSNAME_FOR_REPL
    				: LaunchUtils.MAIN_CLASSNAME);

        config.setAttribute(LaunchUtils.ATTR_CLOJURE_INSTALL_REPL, installREPLChoice.getSelection());
        config.setAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, Integer.valueOf(serverPort.getText()));
        
        mapResources(config);
    }
}
