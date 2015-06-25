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
package ccw.wizards;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.util.CollectionUtils;
import ccw.util.PlatformUtil;
import ccw.util.StringUtils;

public class NewClojureNamespaceWizard extends BasicNewResourceWizard implements INewWizard {

    public String kind() { return "Namespace"; }

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

        IContainer javaSourceFolder;
        Text text;
        private String initialPackageName;

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
            if (getSelection().size() > 0) {
                Object sel = getSelection().getFirstElement();
                if (sel instanceof IPackageFragmentRoot) {
                	javaSourceFolder = (IContainer) ((IPackageFragmentRoot)sel).getResource();
                	initialPackageName = "";
                } else if (sel instanceof IPackageFragment) {
                    javaSourceFolder = (IContainer) getPackageFragmentRoot((IPackageFragment)sel).getResource();
                    initialPackageName = ((IPackageFragment)sel).getElementName();
                } else {
                	IResource res = PlatformUtil.getAdapter(sel, IResource.class);
                	if (res.getType() == IResource.FILE) {
                		res = res.getParent();
                	}
                    if (res.getProject() == null) {
                    	final String JAVA_SOURCE_ERROR = "Cannot create Clojure namespace outside a java source folder";
                        CCWPlugin.logError("Wrong selection type: " + sel.getClass() + ". " + JAVA_SOURCE_ERROR);
                        mainPage.setErrorMessage(JAVA_SOURCE_ERROR);
                        fail = true;
                    } else {
                    	IProject project = res.getProject();
                    	IJavaProject jproject = JavaCore.create(project);
                    	IPackageFragmentRoot[] roots;
						try {
							roots = jproject.getPackageFragmentRoots();
							IPackageFragmentRoot firstSourceFragmentRoot = null;
							for (IPackageFragmentRoot r: roots) {
								if (r.getKind() == IPackageFragmentRoot.K_SOURCE) {
									// Take the first sourceFolder in the list
									if (firstSourceFragmentRoot == null) {
										firstSourceFragmentRoot = r;
									}
									IContainer rc = (IContainer) r.getResource();
									if (rc.getFullPath().isPrefixOf(res.getFullPath())) {
										javaSourceFolder = rc;
										initialPackageName = res.getFullPath().makeRelativeTo(rc.getFullPath()).toPortableString();
										break;
									}
								}
							}
							if (javaSourceFolder == null) {
								if (firstSourceFragmentRoot != null) {
									// selected resource not in a package fragment root, take the first encountered
									javaSourceFolder = (IContainer) firstSourceFragmentRoot.getResource();
									initialPackageName = "";
								}
								mainPage.setErrorMessage("You must create a java source path before adding namespaces");
								fail = true;
							}
						} catch (JavaModelException e) {
							CCWPlugin.logError(e);
							mainPage.setErrorMessage("Error while trying to find a java source folder to create the namespace into.");
							fail = true;
						}
                    }
                }
                
                if (javaSourceFolder != null) {
                    setDescription("Create new Clojure " + kind(true)
                            + " in \"" + javaSourceFolder.getFullPath().toString()
                            + "\" of project \""
                            + javaSourceFolder.getProject().getName() + "\"");
                }
            } else {
                mainPage.setErrorMessage("Cannot determine in which project to create Clojure namespace.");
                fail = true;
            }

            if (!fail) {
                Group group = label(topLevel, kind() + " name:");
                text = new Text(group, SWT.LEFT + SWT.BORDER);
                String initText = ClojureCore.getNamespaceNameFromPackageName(initialPackageName);
                if (!StringUtils.isBlank(initText)) {
                	initText += ".";
                }
                text.setText(initText);
                addToGroup(group, text);
                text.setSelection(initText.length());
            }
        }
        
        private IPackageFragmentRoot getPackageFragmentRoot(IPackageFragment f) {
        	return (IPackageFragmentRoot) f.getAncestor(IPackageFragment.PACKAGE_FRAGMENT_ROOT);
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

    public NewClojureNamespaceWizard() {
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
        String name = mainPage.text.getText().trim().replaceAll("-", "_"); // FIXME should call clojure.core/munge
        if (name.endsWith(".clj"))
            name = name.substring(0, name.length() - (".clj").length());
        else if (name.endsWith(".cljs")) {
        	name = name.substring(0, name.length() - (".cljs").length());
        }
        else if (name.endsWith(".clja")) {
        	name = name.substring(0, name.length() - (".clja").length());
        }
        else if (name.endsWith(".cljc")) {
        	name = name.substring(0, name.length() - (".cljc").length());
        }
        else if (name.endsWith(".cljx")) {
        	name = name.substring(0, name.length() - (".cljx").length());
        }
        return name;
    }

    protected String suffix() {
        String name = mainPage.text.getText().trim().replaceAll("-", "_"); // FIXME should call clojure.core/munge
        int index = name.lastIndexOf(".");
        if (index >= 0) {
        	return name.substring(index);
        } else {
        	return ".clj";
        }
    }

    /**
     * 
     * @param parts the parts. Can be modified if extension is extracted
     * @return the extension
     */
    private String extractExtension(List<String> parts) {
    	String extension;
    	final String s = parts.get(parts.size() - 1);
    	 if (s.equals("clj") || s.equals("cljs")) {
     		extension = parts.remove(parts.size() - 1);
    	 } else {
    		 extension = "clj";
    	 }
    	 return extension;
    }

    private String checkNamespaceSegment(String name) {
    	if (StringUtils.isBlank(name)) {
    		return "Empty namespace segment found";
    	}
        if (!Character.isJavaIdentifierStart(name.charAt(0))
        		&& !Character.isDigit(name.charAt(0)) // digits accepted
        		) {
            return "Invalid character \'" + name.charAt(0) + "\' at index " + 0
            		+ " for Clojure namespace file \'" + name + "'";
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return "Invalid character \'"
                        + name.charAt(i)
                        + "\' at index "
                        + i
                        + " in namespace segment \'"
                        + name + "'";
            }
        }
    	return null;
    }
    
    private String checkNamespacename(List<String> parts) {
    	
    	if (parts.size() == 0) {
    		return "Empty namespace name";
    	}
    	
    	for (String part: parts) {
    		String msg = checkNamespaceSegment(part);
    		if (!StringUtils.isBlank(msg)) {
    			return msg;
    		}
    	}
    	return null;
    }
    
    private String mungePart(String p) {
    	return p.replaceAll("-", "_");  // FIXME should call clojure.core/munge
    }
    
    private List<String> mungeParts(List<String> l) {
    	List<String> ret = new ArrayList<String>(l.size());
    	for (String p: l) {
    		ret.add(mungePart(p));
    	}
    	return ret;
    }

    /**
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
    	final String userInput = mainPage.text.getText().trim();
    	
    	final List<String> unmungedParts = new ArrayList<String>(Arrays.asList(userInput.split("\\.")));
    	
    	final String extension = extractExtension(unmungedParts);
    	final String namespace = CollectionUtils.join(unmungedParts, ".");
    	
    	final List<String> parts = mungeParts(unmungedParts); 

        final String msg = checkNamespacename(parts);
        if (msg != null) {
        	mainPage.setErrorMessage(msg);
        	return false;
        }
        
        final IPath path = ccw.util.ResourceUtil.createPathFromList(parts).addFileExtension(extension);
        final IFile file = mainPage.javaSourceFolder.getFile(path);

        if (file.exists()) {
            mainPage.setErrorMessage("Namespace " + namespace + " already exists.");
            return false;
        }

        try {
        	final String contents = "(ns " + namespace + ")\n\n";
        	ccw.util.ResourceUtil.createMissingParentFolders(file);
            file.create(stringToStream(contents, ResourcesPlugin.getEncoding()), true, null);
            IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    TextEditor editor = (TextEditor) IDE.openEditor(page, file, true);
                    editor.selectAndReveal(contents.length(), 0);
                }
            }
        }
        catch (CoreException e) {
            CCWPlugin.logError(e);
            return false;
        }

        return true;
    }
    
    private ByteArrayInputStream stringToStream(String s, String encoding) {
    	try {
			return new ByteArrayInputStream(s.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			try {
				return new ByteArrayInputStream(s.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				CCWPlugin.logError("Unable to encode '" + s + "' in UTF-8", e1);
				return new ByteArrayInputStream(s.getBytes());
			}
		}
    }

    protected String body() {
        return "";
    }

}