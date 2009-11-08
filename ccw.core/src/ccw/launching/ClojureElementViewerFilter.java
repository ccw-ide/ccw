/*******************************************************************************
 * Copyright (c) 2009 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ClojureElementViewerFilter extends ViewerFilter {
	private final IProject project;
	
	public ClojureElementViewerFilter(IProject project) {
		this.project = project;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IResource resource = (IResource) element;
		IJavaProject javaProject = JavaCore.create(project);
		
		if (javaProject == null) {
			return true;
		}
		
		if (contains(project.getFolder("classes"), resource)) {
			return false;
		}
		
		IPackageFragmentRoot[] froots;
		try {
			froots = javaProject.getAllPackageFragmentRoots();
		} catch (JavaModelException e) {
			return true;
		}
		
		for (IPackageFragmentRoot froot: froots) {
			IResource frootResource = froot.getResource();
			if (frootResource == null) {
				continue; // Continue to test following fragment roots
			} else if (contains(resource, froot.getResource())) {
				return true;
			} else if (contains(froot.getResource(), resource)) {
				if (resource.getType() == IResource.FILE) {
					return ((IFile) resource).getFileExtension().equals("clj");
				} else {
					return true;
				}
			} else {
				continue; // Continue to test following fragment roots
			}
		}
		return false;
	
	}
	
	/** 
	 * @return true if <code>container</code> contains or is equal 
	 *         to <code>containee</code> 
	 */
	private boolean contains(IResource container, IResource containee) {
		return container.getFullPath().isPrefixOf(containee.getFullPath());
	}
}
