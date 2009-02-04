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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;


public class ClojureVisitor implements IResourceVisitor {
	private transient IFolder currentSrcFolder;
	
	private static final String CLOJURE_EXTENSION = "clj";
	private List<IFile> clojureFiles = new ArrayList<IFile>();
	private List<String> clojureLibs = new ArrayList<String>();

	public boolean visit(IResource resource) throws CoreException {
		if(resource instanceof IFile){
			IFile file = (IFile) resource;
			String extension = file.getFileExtension();
			if(extension != null && extension.equals(CLOJURE_EXTENSION)){
				clojureFiles.add(file);
				System.out.println("found clojure file:" + file);
				IPath maybeLibPath = file.getFullPath().removeFirstSegments(currentSrcFolder.getFullPath().segmentCount()).removeFileExtension();
				String maybeLibName = maybeLibPath.toString().replace('/', '.');
				clojureLibs.add(maybeLibName);
				System.out.println("found clojure maybe lib:" + maybeLibName);
				file.touch(null);
			}
		}
		return true;
	}
	
	public IFile[] getClojureFiles(){
		return clojureFiles.toArray(new IFile[clojureFiles.size()]);
	}
	public String[] getClojureLibs() {
		return clojureLibs.toArray(new String[clojureLibs.size()]);
	}

	/**
	 * @param srcFolder
	 */
	public void setSrcFolder(IFolder srcFolder) {
		this.currentSrcFolder = srcFolder;
	}

}
