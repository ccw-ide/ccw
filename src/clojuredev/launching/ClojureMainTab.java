package clojuredev.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

/**
 * Heavily adapted from JDT's java launcher tabs.
 * 
 * @author cmarshal
 * 
 */
@SuppressWarnings("restriction")
public class ClojureMainTab extends AbstractJavaMainTab implements IJavaLaunchConfigurationConstants {

    protected boolean useREPL = true;
//    protected Button useReplBtn;
//    protected Button useMainBtn;
//    protected Text entryFileText;
//    protected Text entrySymbolText;
    
    public void createControl(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, parent.getFont(),
                1, 1, GridData.FILL_BOTH);
        ((GridLayout) comp.getLayout()).verticalSpacing = 0;
        createProjectEditor(comp);
        createVerticalSpacer(comp, 1);
        createFileEditor(comp, "Clojure File");
        setControl(comp);
    }

    private void createFileEditor(Composite parent, String string) {
//        Composite section = SWTFactory.createComposite(parent, parent.getFont(),
//                1, 1, GridData.FILL_BOTH);
//        useReplBtn = new Button(section, SWT.RADIO);
//        useReplBtn.setText("Interactive REPL");
//        
//        useMainBtn = new Button(section, SWT.RADIO);
//        useMainBtn.setText("Entry Point");
//        
//        final Composite entryPointSection = SWTFactory.createComposite(section, section.getFont(),
//                3, 1, GridData.FILL_BOTH);
//        
//        new Label(entryPointSection, SWT.NULL).setText("Entry file");
//        entryFileText = new Text(entryPointSection, SWT.BORDER);
//        entryFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        
//        final Button entryFileSelect = new Button(entryPointSection, SWT.PUSH);
//        entryFileSelect.setText("Browse...");
//        
//        new Label(entryPointSection, SWT.NULL).setText("Entry point symbol");
//        entrySymbolText = new Text(entryPointSection, SWT.BORDER);
//        entrySymbolText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        
//        useReplBtn.addSelectionListener(new SelectionAdapter(){
//
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                useREPL = true;
//                entryPointSection.setEnabled(false);
//            }
//            
//        });
//        useMainBtn.addSelectionListener(new SelectionAdapter(){
//
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                useREPL = false;
//                entryPointSection.setEnabled(true);
//            }
//            
//        });
//        
//        useReplBtn.setSelection(useREPL);
    }

    public String getName() {
        return "Clojure";
    }

    public void performApply(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(ATTR_PROJECT_NAME, fProjText.getText().trim());
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
        }
        else {
            config.setAttribute(ATTR_PROJECT_NAME, EMPTY_STRING);
        }
        
        Bundle bundle = Platform.getBundle("clojure");
        String[] locSplit = bundle.getLocation().split(":");
        
        try {
            List classpath = config.getAttribute(ATTR_CLASSPATH, new ArrayList());
            
            File clojurePath = new File(locSplit[2]);
            if (clojurePath.getName().endsWith(".jar")) {
                classpath.add(clojurePath.toString());
            }
            else {
                classpath.add(new File(clojurePath, "bin").toString());
            }
            
            config.setAttribute(ATTR_CLASSPATH, classpath);
            
            config.setAttribute(ATTR_MAIN_TYPE_NAME, "clojure.lang.Repl");
            
            config.doSave();
        }
        catch (CoreException e) {
            throw new RuntimeException(e);
        }
       
    }

}
