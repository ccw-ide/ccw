/*******************************************************************************
 * Copyright (c) 2009 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gaetan Morice (Anyware Technologies) - initial implementation
 *     Laurent Petit (clojuredev) - adaptation to clojuredev
 *******************************************************************************/

package clojuredev.builder;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import clojuredev.ClojuredevPlugin;
import clojuredev.debug.ClojureClient;
import clojuredev.editors.antlrbased.CompileLibAction;

/*
 * gaetan.morice:
 * "
 * Note that this code is just a prototype and there is still lots of problems to fix. Among then :
 * Incremental build : I only implement full build.
 *  Synchronization with JDT build : as clojure and java files could depend on each others, the two builders
 *  need to be launch several time to resolve all the dependencies. 
 */
public class ClojureBuilder extends IncrementalProjectBuilder {
    
    static public final String BUILDER_ID = "clojuredev.builder";
    
    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
    	if (getProject()==null || getDelta(getProject()) == null) {
    		return null;
    	}
    	
    	if (onlyClassesFolderRelatedDelta()) {
    		return null;
    	}
    	
    	fullBuild(monitor);

    	
        // Commented out to not break svn
//        if(kind == FULL_BUILD){
//            fullBuild(monitor);
//        } else {
//            IResourceDelta delta = getDelta(getProject());
//            if(delta == null){
//                fullBuild(monitor);
//            } else {
//                fullBuild(monitor);
//                //incrementalBuild(delta, monitor);
//            }
//        }
        return null;
    }
    
    private boolean onlyClassesFolderRelatedDelta() {
    	
    	IPath classesFolderFullPath = getClassesFolder().getFullPath(); 

		for (IResourceDelta d: getDelta(getProject()).getAffectedChildren()) {
			if (classesFolderFullPath.isPrefixOf(d.getFullPath())) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
	

    protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
        // TODO Auto-generated method stub
        
    }

    protected void fullBuild(IProgressMonitor monitor) throws CoreException{
        
        if(monitor == null){
            monitor = new NullProgressMonitor();
        }
        
        final IProject project = getProject();
        IJavaProject jProject = JavaCore.create(project);
        
        ClojureClient clojureClient = ClojuredevPlugin.getDefault().getProjectClojureClient(project);
        if (clojureClient == null) {
        	return;
        }

        ArrayList<IFolder> srcFolders = new ArrayList<IFolder>();
        
        IClasspathEntry[] entries = jProject.getResolvedClasspath(true);
        try {
            for(IClasspathEntry entry : entries){
                switch (entry.getEntryKind()) {
                case IClasspathEntry.CPE_SOURCE:
                    IFolder folder = project.getWorkspace().getRoot().getFolder(entry.getPath());
                    srcFolders.add(folder);
                    break;
                case IClasspathEntry.CPE_LIBRARY:
                    break;
                case IClasspathEntry.CPE_PROJECT:
                	// TODO should compile here ?
                    break;
                case IClasspathEntry.CPE_CONTAINER:
                case IClasspathEntry.CPE_VARIABLE:
                	// Impossible cases, since entries are resolved
                default:
                    break;
                }
            }
        } catch (Exception e1) {
            throw new CoreException(new Status(IStatus.ERROR, ClojuredevPlugin.PLUGIN_ID, IStatus.OK, "Unable to add to ClassPath", e1));
        }
        
        ClojureVisitor visitor = new ClojureVisitor();
        for(IFolder srcFolder : srcFolders){
        	visitor.setSrcFolder(srcFolder);
            srcFolder.accept(visitor);
        }
        
        String[] clojureLibs = visitor.getClojureLibs();
        for (String libName: clojureLibs) {
        	clojureClient.remoteLoad(CompileLibAction.compileLibCommand(libName));
        }
        
        getClassesFolder().refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 0));
    }
    
    private IFolder getClassesFolder() {
    	return getProject().getFolder("classes");
    }
    
    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
    	if (monitor==null) {
    		monitor = new NullProgressMonitor();
    	}
    	getClassesFolder().delete(false, monitor);
    	if (!getClassesFolder().exists()) {
    		getClassesFolder().create(true, true, monitor);
    	}
    }
    
}
