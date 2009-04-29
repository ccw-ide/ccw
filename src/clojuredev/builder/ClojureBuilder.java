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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import clojuredev.ClojuredevPlugin;
import clojuredev.debug.ClojureClient;

/*
 * gaetan.morice:
 * "
 * Note that this code is just a prototype and there is still lots of problems to fix. Among then :
 * Incremental build : I only implement full build.
 *  Synchronization with JDT build : as clojure and java files could depend on each others, the two builders
 *  need to be launch several time to resolve all the dependencies. 
 */
public class ClojureBuilder extends IncrementalProjectBuilder {
	public static final String CLOJURE_COMPILER_PROBLEM_MARKER_TYPE = "clojuredev.markers.problemmarkers.compilation";
    
    static public final String BUILDER_ID = "clojuredev.builder";
    
    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
    	if (getProject()==null) {
    		return null;
    	}
    	
    	if (kind == AUTO_BUILD || kind == INCREMENTAL_BUILD) {
	    	if (onlyClassesFolderRelatedDelta()) {
	    		return null;
	    	}
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
    	if (getDelta(getProject())==null) {
    		return false;
    	}
    	
    	IPath classesFolderFullPath = getClassesFolder().getFullPath(); 

		for (IResourceDelta d: getDelta(getProject()).getAffectedChildren()) {
			System.out.println("affected children for build:" + d.getFullPath());
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
        
        ClojureClient clojureClient = ClojuredevPlugin.getDefault().getProjectClojureClient(getProject());
        if (clojureClient == null) {
        	return;
        }
        
        deleteMarkers();

        ClojureVisitor visitor = new ClojureVisitor(clojureClient);
        visitor.visit(getSrcFolders());
        
        getClassesFolder().refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 0));
    }
    
    private IFolder getClassesFolder() {
    	return getProject().getFolder("classes");
    }

    private Map<IFolder, IFolder> getSrcFolders() throws CoreException {
        Map<IFolder, IFolder> srcFolders = new HashMap<IFolder, IFolder>();
        
        final IProject project = getProject();
        IJavaProject jProject = JavaCore.create(project);
        IClasspathEntry[] entries = jProject.getResolvedClasspath(true);
        IPath defaultOutputFolder = jProject.getOutputLocation();
        for(IClasspathEntry entry : entries){
            switch (entry.getEntryKind()) {
            case IClasspathEntry.CPE_SOURCE:
                IFolder folder = project.getWorkspace().getRoot().getFolder(entry.getPath());
                IFolder outputFolder = project.getWorkspace().getRoot().getFolder(
                		(entry.getOutputLocation()==null) ? defaultOutputFolder : entry.getOutputLocation());
                srcFolders.put(folder, outputFolder);
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
        return srcFolders;
    }
    
    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
    	if (monitor==null) {
    		monitor = new NullProgressMonitor();
    	}
    	
    	try {
    		getClassesFolder().delete(true, monitor);
    	} catch (CoreException e) {
    		ClojuredevPlugin.logError("Unable to clean classes folder", e);
    	}
    	if (!getClassesFolder().exists()) {
    		getClassesFolder().create(true, true, monitor);
    	}
        getClassesFolder().refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 0));

        deleteMarkers();
    }
    
    private void deleteMarkers() throws CoreException {
        for (IFolder srcFolder: getSrcFolders().keySet()) {
        	srcFolder.deleteMarkers(CLOJURE_COMPILER_PROBLEM_MARKER_TYPE, true, IFile.DEPTH_INFINITE);
        }
    }
    
}
