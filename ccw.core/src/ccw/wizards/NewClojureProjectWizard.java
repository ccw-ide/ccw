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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import ccw.ClojureCore;
import ccw.CCWPlugin;

public class NewClojureProjectWizard extends BasicNewProjectResourceWizard {

    public NewClojureProjectWizard() {
        super();
    }

    /**
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages();
        getStartingPage().setDescription("Create a new Clojure project.");
        getStartingPage().setTitle("Clojure project");
        setWindowTitle("New Clojure project");
    }

    /**
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        if (super.performFinish()) {
            IProject project = getNewProject();

            if (ClojureCore.addJavaNature(project)) {
            	IJavaProject javaProject = JavaCore.create(project);
            	if (javaProject == null) {
            		return false;
            	} else {
            		try {
						setupJavaProjectClassPath(javaProject);
	            		return ClojureCore.addClojureNature(project);
					} catch (CoreException e) {
						CCWPlugin.logError(
								"Error(s) while creating new clojure project " + project.getName(), e);
						return false;
					}
            	}
            } else {
            	return false;
            }
        } else {
        	return false;
        }
    }
    
    private void setupJavaProjectClassPath(IJavaProject javaProject) throws CoreException {
        IClasspathEntry[] entriesOld = javaProject.getRawClasspath();
        IClasspathEntry[] entriesNew = new IClasspathEntry[entriesOld.length + 1];
        
        System.arraycopy(entriesOld, 0, entriesNew, 0, entriesOld.length);

        // Ensure a proper "src" directory is used for sources (and not the project)
        for (int i = 0; i < entriesOld.length; i++) {
            if (entriesOld[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                IFolder src = javaProject.getProject().getFolder("src");
                if (!src.exists())
                    src.create(true, true, null);
                entriesNew[i] = JavaCore.newSourceEntry(src.getFullPath());
            }
        }

        entriesNew[entriesOld.length] = JavaCore.newContainerEntry(Path
                .fromPortableString(JavaRuntime.JRE_CONTAINER));

        javaProject.setRawClasspath(entriesNew, null);
        javaProject.save(null, true);
    }
}
