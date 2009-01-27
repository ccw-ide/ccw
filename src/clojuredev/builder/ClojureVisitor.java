/*******************************************************************************
 * Copyright (c) 2009 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gaetan Morice (Anyware Technologies) - initial implementation
 *******************************************************************************/

package clojuredev.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;


public class ClojureVisitor implements IResourceVisitor {
	
	private static final String CLOJURE_EXTENSION = "clj";
	private List<IFile> clojureFiles = new ArrayList<IFile>();

	public boolean visit(IResource resource) throws CoreException {
		if(resource instanceof IFile){
			IFile file = (IFile) resource;
			String extension = file.getFileExtension();
			if(extension != null && extension.equals(CLOJURE_EXTENSION)){
				clojureFiles.add(file);
			}
		}
		return true;
	}
	
	public IFile[] getClojureFiles(){
		return clojureFiles.toArray(new IFile[clojureFiles.size()]);
	}

}
