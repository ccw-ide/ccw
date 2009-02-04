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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
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
 * Synchronization with JDT clean : JDT builder clean all *.class files even the ones it does not made.
 * Class Loader : in order to use clojure compiler, project build path has to be added to the clojure class
 * loader, these class paths should be removed from clojure class loader after the build.
 * "
 * laurent.petit:
 * "
 * I'll use this as the basis for clojuredev own builder.
I think that for a first version, I'll too just make a full builder, and wait for
performance problems before writing an incremental one (thus saving development time
for adding other functionalities right now).

BUT I think I'll not follow the example in one place: I'll not use the clojure
environment of the plugin to compile files. For at least two reasons :
 * if there is malicious or problematic code in the compiled files, I don't want the
eclipse environment to hang, or to crash.
 * to decouple the version of clojure needed by the plugin own needs (for the parts
of the plugin written in clojure), from the version of clojure the user wants to use
: I'll use the version of clojure the user wants.

But this will lead to another problem : we can't, for performance reason I fear,
start a new JVM each time we want to compile a new file ! So there will be the need
to share an instance at the project level, and be sure this instance is still alive
(and in good conditions ...)
"
 */
public class ClojureBuilder extends IncrementalProjectBuilder {
    
    static public final String BUILDER_ID = "clojuredev.builder";
    
    private static final Var compilePath = RT.var("clojure.core", "*compile-path*");
    private static final Var compile = RT.var("clojure.core", "compile");

    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
    	System.out.println("full build required!");
//    	fullBuild(monitor);
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
    
    protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
        // TODO Auto-generated method stub
        
    }

    protected void fullBuild(IProgressMonitor monitor) throws CoreException{
        
        if(monitor == null){
            monitor = new NullProgressMonitor();
        }
        
        IProject project = getProject();
        IJavaProject jProject = JavaCore.create(project);
        
        ClojureClient clojureClient = ClojuredevPlugin.getDefault().getProjectClojureClient(project);
        if (clojureClient == null) {
        	return;
        }

        ArrayList<IFolder> srcFolders = new ArrayList<IFolder>();
        
//        jProject.get
//        
//        IFolder binFolder = project.getWorkspace().getRoot().getFolder(jProject.getOutputLocation());
//        Var.pushThreadBindings(RT.map(compilePath, binFolder.getLocation().toOSString()));
//        try {
//            RT.addURL("file:"+binFolder.getLocation().toOSString()+"/");
//        } catch (Exception e2) {
//            throw new CoreException(new Status(IStatus.ERROR, ClojuredevPlugin.PLUGIN_ID, IStatus.OK, "Unable to add to ClassPath", e2));
//        }
//        
        IClasspathEntry[] entries = jProject.getResolvedClasspath(true);
        try {
            for(IClasspathEntry entry : entries){
                switch (entry.getEntryKind()) {
                case IClasspathEntry.CPE_SOURCE:
                    IFolder folder = project.getWorkspace().getRoot().getFolder(entry.getPath());
                    srcFolders.add(folder);
//                    RT.addURL("file:"+folder.getLocation().toOSString()+"/");
                    break;
                case IClasspathEntry.CPE_LIBRARY:
                	// Nothing to compile here
                    break;
                case IClasspathEntry.CPE_PROJECT:
                	// TODO should compile here ?
//                    IProject p = project.getWorkspace().getRoot().getProject(entry.getPath().toString());
//                    IJavaProject jp = JavaCore.create(project);
//                    IFolder bf = p.getWorkspace().getRoot().getFolder(jp.getOutputLocation());
//                    RT.addURL("file:"+bf.getLocation().toOSString()+"/");
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
        
        // ClojureVisitor will do a lot of things:
        // - create a list of libs
        ClojureVisitor visitor = new ClojureVisitor();
        for(IFolder srcFolder : srcFolders){
        	visitor.setSrcFolder(srcFolder);
            srcFolder.accept(visitor);
        }
        
        String[] clojureLibs = visitor.getClojureLibs();
        for (String libName: clojureLibs) {
        	System.out.println(clojureClient.remoteLoad(CompileLibAction.compileLibCommand(libName)));
        }
        
        IFolder classesFolder = project.getFolder("classes");
        classesFolder.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 0));
//        
////        IFile[] clojureFiles = visitor.getClojureFiles();
////        for(IFile src : clojureFiles){
////            String[] segments = src.getFullPath().removeFileExtension().segments();
////            String[] elements = new String[segments.length-2];
////            System.arraycopy(segments, 2, elements, 0, elements.length);
////            String nameSpace = "";
////            for(int i=0; i<elements.length; i++){
////                if(i != elements.length-1) nameSpace += elements[i]+".";
////                else nameSpace += elements[i];
////            }
////            try {
////                compile.invoke(Symbol.intern(nameSpace));
//                binFolder.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 0));
//            } catch (Exception e) {
//                throw new CoreException(new Status(IStatus.ERROR, ClojuredevPlugin.PLUGIN_ID, IStatus.OK, "Unable to compile script : "+src.getFullPath().toOSString(), e));
//            }
//        }
        
        
    }

}
