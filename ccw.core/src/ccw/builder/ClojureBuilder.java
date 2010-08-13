/*******************************************************************************
 * Copyright (c) 2009 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gaetan Morice (Anyware Technologies) - initial implementation
 *     Laurent Petit (ccw) - adaptation to ccw
 *******************************************************************************/

package ccw.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import ccw.CCWPlugin;
import ccw.debug.ClojureClient;

/*
 * gaetan.morice:
 * "
 * Note that this code is just a prototype and there is still lots of problems to fix. Among then :
 * Incremental build : I only implement full build.
 *  Synchronization with JDT build : as clojure and java files could depend on each others, the two builders
 *  need to be launch several time to resolve all the dependencies. 
 */
public class ClojureBuilder extends IncrementalProjectBuilder {
	public static final String CLOJURE_COMPILER_PROBLEM_MARKER_TYPE = "ccw.markers.problemmarkers.compilation";
    
    static public final String BUILDER_ID = "ccw.builder";
    
    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
    	if (getProject()==null) {
    		return null;
    	}
    	
    	if (kind == AUTO_BUILD || kind == INCREMENTAL_BUILD) {
	    	if (onlyClassesOrOutputFolderRelatedDelta() && !onlyProjectTouched() ) {
	    		return null;
	    	}
    	}
    	
    	fullBuild(getProject(), monitor);

    	
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
    
    /** Only project touch is treated similarly to a full build request */
    private boolean onlyProjectTouched() {
        IResourceDelta delta = getDelta(getProject());
        return delta.getResource().equals(getProject()) && delta.getAffectedChildren().length == 0;
    }
    
    private boolean onlyClassesOrOutputFolderRelatedDelta() throws CoreException {
    	if (getDelta(getProject()) == null) {
    		return false;
    	}
    	
    	List<IPath> folders = new ArrayList<IPath>();
    	folders.add(getClassesFolder(getProject()).getFullPath());
		for (IFolder outputPath: getSrcFolders(getProject()).values()) {
			folders.add(outputPath.getFullPath());
		}
		
		delta_loop: for (IResourceDelta d: getDelta(getProject()).getAffectedChildren()) {
			for (IPath folder: folders) {
				if (folder.isPrefixOf(d.getFullPath())) {
					continue delta_loop;
				}
			}
			System.out.println("affected children for build:" + d.getFullPath());
			return false;
		}
		return true;
	}
	

    protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
        // TODO Auto-generated method stub
        
    }

    public static void fullBuild(IProject project, IProgressMonitor monitor) throws CoreException{
        
        if(monitor == null){
            monitor = new NullProgressMonitor();
        }
        
        ClojureClient clojureClient = CCWPlugin.getDefault().getProjectClojureClient(project);
        if (clojureClient == null) {
        	return;
        }
        
        deleteMarkers(project);

        ClojureVisitor visitor = new ClojureVisitor(clojureClient);
        visitor.visit(getSrcFolders(project));
        
        getClassesFolder(project).refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 0));
    }
    
    private static IFolder getClassesFolder(IProject project) {
    	return project.getFolder("classes");
    }

    private static Map<IFolder, IFolder> getSrcFolders(IProject project) throws CoreException {
        Map<IFolder, IFolder> srcFolders = new HashMap<IFolder, IFolder>();
        
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
    		getClassesFolder(getProject()).delete(true, monitor);
	    	if (!getClassesFolder(getProject()).exists()) {
	    		getClassesFolder(getProject()).create(true, true, monitor);
	    	}
	        getClassesFolder(getProject()).refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 0));
	        getClassesFolder(getProject()).setDerived(true); //, monitor); removed monitor argument, probably a 3.5/3.6 only stuff
    	} catch (CoreException e) {
    		CCWPlugin.logError("Unable to correctly clean classes folder", e);
    	}

        deleteMarkers(getProject());
    }
    
    private static void deleteMarkers(IProject project) throws CoreException {
        for (IFolder srcFolder: getSrcFolders(project).keySet()) {
        	srcFolder.deleteMarkers(CLOJURE_COMPILER_PROBLEM_MARKER_TYPE, true, IFile.DEPTH_INFINITE);
        }
    }
    
}
