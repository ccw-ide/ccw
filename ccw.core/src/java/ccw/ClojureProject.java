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
/*
 * Adapted from the...
**     ________ ___   / /  ___     Scala Plugin for Eclipse             **
**    / __/ __// _ | / /  / _ |    (c) 2004-2005, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
*
* by Casey Marshall for the Clojure plugin, ccw
\*                                                                      */

// Created on 2004-11-12 by Thierry Monney
// $Id: ScalaProject.java,v 1.4 2006/02/03 12:41:22 mcdirmid Exp $
package ccw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ClojureProject extends PlatformObject {
    private final IProject project;
    Character c;
    private IJavaProject javaProject;

    private long classpathUpdate = IResource.NULL_STAMP;

    public void checkClasspath() {
        IFile classpath = project.getProject().getFile(".classpath");
        if (!classpath.exists())
            return;
        if (classpathUpdate == IResource.NULL_STAMP) {
            classpathUpdate = classpath.getModificationStamp();
            return;
        }
        if (classpathUpdate == classpath.getModificationStamp())
            return;

        classpathUpdate = classpath.getModificationStamp();
    }

    public IFile getFileAny(IPath location) {
        IPath ppath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        if (location.matchingFirstSegments(ppath) != ppath.segmentCount())
            return null;
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
                location.removeFirstSegments(ppath.segmentCount()));
        if (file.exists())
            return file;
        else
            return null;
    }

    public IFile getFile(IPath location) {
        IPath ppath = project.getLocation();
        if (location.matchingFirstSegments(ppath) != ppath.segmentCount())
            return null;
        IFile file = project.getProject().getFile(
                location.removeFirstSegments(ppath.segmentCount()));
        if (file.exists())
            return file;
        else
            return null;
    }

    public IFile stateFile() {
        return project.getFile(".manager");
    }

    /**
     * Creates a new Clojure project backed by the given Eclipse project
     * 
     * @param project
     *            the Eclipse project
     */
    public ClojureProject(IProject project) {
        this.project = project;
        assert (project.exists());

        ClojureCore.clojureProjects.put(project, this);
    }

    public final HashSet<IPath> leftOver = new HashSet<IPath>();

    public IProject getProject() {
        return project;
    }

    public IJavaProject getJavaProject() {
        if (javaProject == null)
            javaProject = ClojureCore.getJavaProject(project);
        return javaProject;
    }

    public List<IJavaProject> dependencies() {
        List<IJavaProject> ret = new ArrayList<IJavaProject>();
        try {
            for (IPackageFragmentRoot root : getJavaProject()
                    .getAllPackageFragmentRoots())
                if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    // System.err.println("SOURCE: " + root + " " +
                    // root.getClass());
                    if (root.getResource() instanceof IProject) {
                        IProject p = (IProject) root.getResource();
                        if (!p.hasNature(JavaCore.NATURE_ID))
                            continue;
                        ret.add(JavaCore.create(p));
                        continue;
                    }
                }
        }
        catch (JavaModelException e) {
            CCWPlugin.logError(e);
        }
        catch (CoreException e) {
            CCWPlugin.logError(e);
        }
        return ret;
    }

    public List<IFolder> sourceFolders() {
        return sourceFolders(getJavaProject());
    }

    public static List<IFolder> sourceFolders(IJavaProject jp) {
        List<IFolder> ret = new ArrayList<IFolder>();
        try {
            for (IPackageFragmentRoot root : jp.getAllPackageFragmentRoots())
                if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    // System.err.println("SOURCE: " + root + " " +
                    // root.getClass());
                    if (root.getResource() instanceof IFolder
                            && root.getParent().getAdapter(IProject.class) == jp
                                    .getProject()) {
                        ret.add((IFolder) root.getResource());
                    }
                }
        }
        catch (JavaModelException e) {
            CCWPlugin.logError(e);
        }
        return ret;
    }

    // public SemanticTokens semantics() {
    // if (semanticCompiler == null)
    // return null;
    // return semanticCompiler.semantics();
    // }

    public String getOutpath() {
        try {
            IPath path = getJavaProject().getOutputLocation();
            IFolder fldr = ResourcesPlugin.getWorkspace().getRoot().getFolder(
                    path);
            if (!fldr.exists()) {
//                System.err.println("CREATE: " + fldr);
                fldr.create(true, true, null);
            }
            return fldr.getLocation().toOSString();
        }
        catch (JavaModelException e) {
            CCWPlugin.logError(e);
        }
        catch (CoreException e) {
            CCWPlugin.logError(e);
        }
        return project.getProject().getLocation().toOSString();
    }

    Runnable notifyBuilt;

    public void notifyBuilt(Runnable notifyBuilt) {
        this.notifyBuilt = notifyBuilt;
    }

    public void notifyBuilt() {
        if (notifyBuilt != null)
            notifyBuilt.run();
    }

}