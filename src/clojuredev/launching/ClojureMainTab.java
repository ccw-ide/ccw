package clojuredev.launching;

import java.util.ArrayList;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import clojuredev.ClojuredevPlugin;

/**
 * Heavily adapted from JDT's java launcher tabs.
 * 
 * @author cmarshal
 * 
 */
@SuppressWarnings("restriction")
public class ClojureMainTab extends AbstractJavaMainTab implements IJavaLaunchConfigurationConstants {
	
    protected boolean useREPL = true;

    protected TableViewer sourceFilesViewer;
    
    public void createControl(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, parent.getFont(),
                1, 1, GridData.FILL_BOTH);
        ((GridLayout) comp.getLayout()).verticalSpacing = 0;
        createProjectEditor(comp);
        createVerticalSpacer(comp, 1);
        createFileEditor(comp, "Clojure File");
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
                getLaunchConfigurationDialog().updateButtons();
            }
            
        });
    }

    public String getName() {
        return "Clojure";
    }

    @SuppressWarnings("unchecked")
    public void performApply(ILaunchConfigurationWorkingCopy config) {
    	
        config.setAttribute(ATTR_PROJECT_NAME, fProjText.getText().trim());

        LaunchUtils.setFilesToLaunchString(config, (List<IFile>) sourceFilesViewer.getInput());
        
        mapResources(config);
        try {
            config.doSave();
        }
        catch (CoreException e) {
            throw new RuntimeException(e);
        }
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
        		config.setAttribute(ATTR_MAIN_TYPE_NAME, LaunchUtils.MAIN_CLASSNAME);
        	}
            
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
            sourceFilesViewer.setInput(LaunchUtils.getFilesToLaunchList(config));
        }
        catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
