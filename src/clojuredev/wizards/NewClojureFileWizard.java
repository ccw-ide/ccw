/*******************************************************************************
 * Copyright (c) 2009 Casey Marshall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshall - initial API and implementation
 *******************************************************************************/
package clojuredev.wizards;

import java.io.StringBufferInputStream;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import clojuredev.ClojuredevPlugin;
import clojuredev.util.StringUtils;

public class NewClojureFileWizard extends BasicNewResourceWizard implements INewWizard {

    public String kind() { return "File"; }

    public String adjective() {
        return "";
    }

    protected String kind(boolean toLower) {
        if (toLower)
            return adjective().toLowerCase() + kind().toLowerCase();
        else
            return adjective() + kind();
    }

    class Page extends WizardPage {
        protected Page() {
            super("New Clojure " + kind());
        }

        IContainer dest = null;
        Text text;
        String packageName = "";

        public void createControl(Composite parent) {
            Composite topLevel = new Composite(parent, SWT.NONE);
            GridLayout topLayout = new GridLayout();
            topLayout.verticalSpacing = 0;
            topLevel.setLayout(topLayout);

            topLevel.setFont(parent.getFont());

            // Show description on opening
            setErrorMessage(null);
            setMessage(null);
            setControl(topLevel);

            boolean fail = false;
            if (getSelection().size() == 1) {
                Object sel = getSelection().getFirstElement();
                if (sel instanceof IPackageFragmentRoot) {
                	dest = (IContainer) ((IPackageFragmentRoot)sel).getResource();
                }
                else if (sel instanceof IPackageFragment) {
                    dest = (IContainer)((IPackageFragment)sel).getResource();
                    packageName = ((IPackageFragment)sel).getElementName();
                }
                else {
                	final String JAVA_SOURCE_ERROR = "Cannot create Clojure file outside a java source folder";
                    ClojuredevPlugin.logError("Wrong selection type: " + sel.getClass() + ". " + JAVA_SOURCE_ERROR);
                    mainPage.setErrorMessage(JAVA_SOURCE_ERROR);
                    fail = true;
                }
                
                if (dest != null) {
                    setDescription("Create new Clojure " + kind(true)
                            + " in \"" + dest.getFullPath().toString()
                            + "\" of project \""
                            + dest.getProject().getName() + "\"");
                }
            }
            else {
                IProject project = null;
                for (Iterator i = getSelection().iterator(); i.hasNext();) {
                    IResource res;
                    Object e = i.next();
                    if (e instanceof IResource)
                        res = (IResource) e;
                    else if (e instanceof IAdaptable)
                        res = (IResource) ((IAdaptable) e)
                                .getAdapter(IResource.class);
                    else
                        res = null;
                    if (res == null)
                        continue;
                    if (res.getProject() == null)
                        continue;
                    if (project == null)
                        project = res.getProject();
                    else {
                        project = null;
                        break;
                    }
                }
                if (project != null) {
                    dest = project;
                    setDescription("Create new top-level Clojure " + kind(true) + ".");
                }
                else if (project == null) {
                    mainPage.setErrorMessage("Cannot create top-level Clojure "
                            + kind(true)
                            + " without project selection.");
                    fail = true;
                }
            }
            if (!fail) {
                Group group = label(topLevel, kind() + " name:");
                text = new Text(group, SWT.LEFT + SWT.BORDER);
                addToGroup(group, text);
            }
        }
    }

    private Page mainPage;

    private Group label(Composite parent, String label) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(label);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setFont(parent.getFont());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);
        return group;
    }

    private void addToGroup(Group group, Control control) {
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        control.setLayoutData(gd);
        control.setFont(group.getFont());
    }

    public NewClojureFileWizard() {
        super();
    }

    /**
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages();
        mainPage = new Page();//$NON-NLS-1$
        mainPage.setTitle("New Clojure " + kind(false));
        mainPage.setDescription("Create new top-level Clojure " + kind(true));
        addPage(mainPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setWindowTitle("New Clojure  " + kind(false));
        setNeedsProgressMonitor(false);
    }

    protected void initializeDefaultPageImageDescriptor() {
        super.initializeDefaultPageImageDescriptor();
    }

    protected String name() {
        String name = mainPage.text.getText().trim();
        if (name.endsWith(".clj"))
            name = name.substring(0, name.length() - (".clj").length());
        return name;
    }

    protected IContainer dest() {
        return mainPage.dest;
    }

    /**
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        IContainer dest = dest();
        if (dest == null) {
            mainPage.setErrorMessage("Must select an existing destination folder.");
            return false;
        }
        String name = name();
        if (name.length() == 0) {
            mainPage.setErrorMessage("Empty file name.");
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            mainPage.setErrorMessage("Invalid Clojure resource character \'"
                    + name.charAt(0) + "\' at index " + 0 + " in " + name);
            return false;
        }
        for (int i = 1; i < name.length(); i++)
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                mainPage
                        .setErrorMessage("Invalid Clojure resource character \'"
                                + name.charAt(i)
                                + "\' at index "
                                + i
                                + " in "
                                + name);
                return false;
            }

        // check if file already exists.
        IFile file;
        if (mainPage.dest instanceof IProject) {
            file = ((IProject)mainPage.dest).getFile(name + ".clj");
        }
        else if (mainPage.dest instanceof IFolder) {
            file = ((IFolder)mainPage.dest).getFile(name + ".clj");
        }
        else {
            return false;
        }
        
        if (file.exists()) {
            mainPage.setErrorMessage("File with same name already exists.");
            return false;
        }

        try {
        	String namespace = ((StringUtils.isEmpty(mainPage.packageName) ? "" 
        			: mainPage.packageName + ".") + name).replaceAll("_", "-");
        	String contents = "(ns " + namespace + ")\n\n";
            file.create(new StringBufferInputStream(contents), true, null);
            IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError(e);
            return false;
        }

        return true;
    }

    protected String body() {
        return "";
    }

}