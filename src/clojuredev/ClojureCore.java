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
* by Casey Marshall for the Clojure plugin, clojuredev
\*                                                                      */

// Created on 2004-10-25 by Thierry Monney
//$Id: ScalaCore.java,v 1.3 2006/02/03 12:41:22 mcdirmid Exp $
package clojuredev;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import clojure.lang.RT;
import clojuredev.debug.ClojureClient;

/**
 * This class acts as a Facade for all SDT core functionality.
 * 
 */
public final class ClojureCore {

    /**
     * Clojure file extension
     */
    static public final String CLOJURE_FILE_EXTENSION = "clj";

    /**
     * Clojure source file filter (used to avoid the Java builder to copy
     * Clojure sources to the output folder
     */
    static public final String CLOJURE_FILE_FILTER = "*."
            + CLOJURE_FILE_EXTENSION;

    static final Map<IProject, ClojureProject> clojureProjects = new HashMap<IProject, ClojureProject>();

    static final Map<IProject, IJavaProject> javaProjects = new HashMap<IProject, IJavaProject>();

    /**
     * Gets the SDT core preferences
     * 
     * @return the preferences root node
     */
    public Preferences getPreferences() {
        return ClojuredevPlugin.getDefault().getPluginPreferences();
    }

    private static boolean addNature(IProject project, String natureID) {
        IProjectDescription desc;
        try {
            desc = project.getDescription();
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError("Could not get project description", e);
            return false;
        }
        String[] ids = desc.getNatureIds();
        String[] newIDs = new String[ids.length + 1];
        System.arraycopy(ids, 0, newIDs, 1, ids.length);
        newIDs[0] = natureID;
        desc.setNatureIds(newIDs);
        try {
            project.setDescription(desc, null);
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError("Could not set project description", e);
            return false;
        }
        return true;
    }

    /**
     * Adds tha Scala nature to the given project
     * 
     * @param project
     *            the project
     * @return <code>true</code> if the nature was correctly added,
     *         <code>false</code> otherwise
     */
    public static boolean addClojureNature(IProject project) {
        return addNature(project, ClojureProjectNature.NATURE_ID);
    }

    /**
     * Adds tha Java nature to the given project
     * 
     * @param project
     *            the project
     * @return <code>true</code> if the nature was correctly added,
     *         <code>false</code> otherwise
     */
    public static boolean addJavaNature(IProject project) {
        return addNature(project, JavaCore.NATURE_ID);
    }

    // public static final String[] SCALA_JARS = getScalaJars();

    private static final String EXCLUSION_FILTER_ID = "org.eclipse.jdt.core.builder.resourceCopyExclusionFilter";

    /**
     * Gets the Java project associated to the given project
     * 
     * @param project
     *            the Eclipse project
     * @return the associated Java project
     */
    public static IJavaProject getJavaProject(IProject project) {
        if (project == null)
            return null;
        try {
            if (!project.exists()
                    || !project.hasNature(ClojureProjectNature.NATURE_ID))
                return null;
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError(e);
            return null;
        }
        IJavaProject p = (IJavaProject) javaProjects.get(project);
        if (p == null) {
            p = JavaCore.create(project);
            javaProjects.put(project, p);
        }
        return p;
    }

    /**
     * Gets the Clojure project associated to the given project
     * 
     * @param project
     *            the Eclipse project
     * @return the associated Scala project
     */
    public static ClojureProject getClojureProject(IProject project) {
        ClojureProject p = clojureProjects.get(project);
        if (p != null)
            return p;
        try {
            if (!project.exists() || !project.isOpen()
                    || !project.hasNature(ClojureProjectNature.NATURE_ID))
                return null;
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError(e);
            return null;
        }
        p = new ClojureProject(project);
        return p;
    }
    
    public static ClojureClient getProjectClojureClient(IProject project) {
    	return null;
    }

    /**
     * Gets all the Clojure projects in the workspace
     * 
     * @return an array containing all the Scala projects
     */
    public static ClojureProject[] getClojureProjects() {
        return clojureProjects.values().toArray(new ClojureProject[] {});
    }
    
	/**
	 * Currently very basic: uses a regexp
	 * TODO: should also work with in-ns calls ?
	 * @return
	 */
	public static String getDeclaringNamespace(String sourceText) {
			String searchRegexp = "\\(\\s*ns\\s+([^\\s\\)#\\[\\'\\{]+)";
			Matcher matcher = Pattern.compile(searchRegexp).matcher(sourceText);
			
			String result = null;
			while (matcher.find()) {
				result = matcher.group(1);
			}
			
			return result;
	}
	
    /*
     *  TODO Still 1 more case to handle:
     *  - when a LIBRARY does not have source file in its classpath, then search attached source files folder/archive 
     */
    public static void openInEditor(String searchedNS, String searchedFileName, int line) {
		try {
			org.eclipse.debug.ui.console.IConsole console = (org.eclipse.debug.ui.console.IConsole) ClojureClient.findActiveReplConsole();
			if (console == null) {
				return;
			}
			String projectName = console.getProcess().getLaunch().getLaunchConfiguration().getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
	    	openInEditor(searchedNS, searchedFileName, line, projectName, false);
		} catch (CoreException e) {
			ClojuredevPlugin.logError("error while trying to obtain project's name from configuration, while trying to show source file of a symbol", e);
		}
	}
    
    private static boolean openInEditor(String searchedNS, String searchedFileName, int line, String projectName, boolean onlyExportedEntries) throws PartInitException {
    	if (searchedFileName==null) {
    		return false;
    	}
    	// TODO at some point in time, remove the second behaviour
    	// (when I estimate everybody is using the newer clojure version
    	if (searchedFileName.contains("/")) {
    		// new clojure version with namespace in searchedFileName
    		searchedNS = searchedFileName.substring(0, searchedFileName.lastIndexOf('.')).replace('/', '.');
    		searchedFileName = searchedFileName.substring(1 + searchedFileName.lastIndexOf('/'));
    	} else {
    		// old clojure version without namespace in searchedFileName
    		// let it be
    	}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] classpathEntries;
		try {
			classpathEntries = javaProject.getResolvedClasspath(true);
		} catch (JavaModelException e) {
			ClojuredevPlugin.logError("error getting classpathEntries of javaProject " + javaProject.getPath() 
					+ " while trying to localize for editor opening : " 
					+ searchedNS + " " + searchedFileName, e);
			return false;
		}

		for (IClasspathEntry cpe: classpathEntries) {
			switch (cpe.getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY:
				if (onlyExportedEntries && !cpe.isExported())
					continue;
				// fall through normal case
			case IClasspathEntry.CPE_SOURCE:
				IPackageFragmentRoot[] libPackageFragmentRoots = javaProject.findPackageFragmentRoots(cpe);
				for (IPackageFragmentRoot pfr: libPackageFragmentRoots) {
					try {
						if ("".equals(getNsPackageName(searchedNS))) {
							try {
								if (tryNonJavaResources(pfr.getNonJavaResources(), searchedFileName, line)) {
									return true;
								}
							} catch (JavaModelException e) {
								ClojuredevPlugin.logError("error with packageFragment " + pfr.getPath() 
										+ " while trying to localize for editor opening : " 
										+ searchedNS + " " + searchedFileName, e);
							}
						}
						for (IJavaElement javaElement: pfr.getChildren()) {
							if (!javaElement.getElementName().equals(getNsPackageName(searchedNS)))
								continue;
							if (! (javaElement instanceof IPackageFragment))
								continue;
							IPackageFragment packageFragment = (IPackageFragment) javaElement;
							try {
								if (tryNonJavaResources(packageFragment.getNonJavaResources(), searchedFileName, line)) {
									return true;
								}
							} catch (JavaModelException e) {
								ClojuredevPlugin.logError("error with packageFragment " + packageFragment.getPath() 
										+ " while trying to localize for editor opening : " 
										+ searchedNS + " " + searchedFileName, e);
							}
						}
					} catch (JavaModelException e) {
						ClojuredevPlugin.logError("error with packageFragmentRoot " + pfr.getPath() 
								+ " while trying to localize for editor opening : " 
								+ searchedNS + " " + searchedFileName, e);
					}
				}
//					if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
//						// Last try : search in attached source folder, if any
//						IPath sourcePath = cpe. getSourceAttachmentPath();
//
//						if (sourcePath == null)
//							break;
//						
//						IFileStore sourcePathStore = EFS.getLocalFileSystem().fromLocalFile(sourcePath.toFile());
//						if (sourcePathStore == null)
//							break;
//						System.out.println("worked : "+sourcePathStore);
//						IFileStore maybeSearchedFileStore = sourcePathStore.getFileStore(packageQualifiedFilePath);
//						System.out.println("packageQualifiedFilePath:"+packageQualifiedFilePath);
//						System.out.println("maybeSearchedFileStore:"+maybeSearchedFileStore+ " exists:"+sourcePathStore.fetchInfo().exists());
////						FileLocator.toFileURL(url)
////						ZipEntryStorageEditorInput
//						try {
//							File f = maybeSearchedFileStore.toLocalFile(EFS.CACHE, null);
//							if (f != null) {
//								IEditorPart editor = IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
//								maybeSearchedFileStore);
//								gotoEditorLine(editor, line);
//								return true;
//							}
//							if (maybeSearchedFileStore.fetchInfo().exists()) {
//								System.out.println("FFFFFOOOOOUUUUUUUUUNNNNNNNNNNNNNDDDDDDDD!");
//								IEditorPart editor = IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
//										maybeSearchedFileStore);
//								gotoEditorLine(editor, line);
//								return true;
//								
//							} else {
//								System.out.println("Ohhhhhhhhhhhhhhhhhhhhhhh bouhhhhhhhhhhhhhhh!");
//								break;
//							}
//						} catch (CoreException e) {
//							ClojuredevPlugin.logError("non existing file in store : " 
//									+ searchedNS + " " + searchedFileName, e);
//							break;
//						}
//					}
				break;
			case IClasspathEntry.CPE_PROJECT:
				String dependentProjectName = cpe.getPath().lastSegment();
				if (openInEditor(searchedNS, searchedFileName, line, dependentProjectName, true))
					return true;
				break;
			default:
				break;
			}
		}
		return false;
	}
    
    public static String getNsPackageName(String ns) {
    	return (ns.lastIndexOf(".") < 0) ? "" : ns.substring(0, ns.lastIndexOf(".")).replace('-', '_');
    }
    
    // Currently based on file name convention
    // Should be based, later, on static code analysis (and/or dynamic
    // report of created namespace)
    public static String getDeclaredNamespace(IFile file) {
    	try {
    		IJavaProject jProject = JavaCore.create(file.getProject());
    		IPackageFragmentRoot[] froots = jProject.getAllPackageFragmentRoots();
    		IPath path = null;
    		for (IPackageFragmentRoot froot: froots) {
    			if (froot.getPath().isPrefixOf(file.getFullPath())) {
    				path = file.getFullPath().removeFirstSegments(froot.getPath().segmentCount()).removeFileExtension();
    				break;
    			}
    		}
    		if (path == null) {
    			// file is not on the classpath
    			return null;
    		} else {
//    			return path.toString().replace('/', '.').replace('_', '-');
    			String ns = getDeclaringNamespace(getFileText(file));
				return ns;
    		}
			
		} catch (JavaModelException e) {
			ClojuredevPlugin.logError("unable to determine the fragment root of the file " + file, e);
			return null;
		}
    }
    
    private static String getFileText(IFile file) {
    	try {
			return (String) RT.var("clojure.core", "slurp").invoke(file.getLocation().toOSString());
		} catch (Exception e) {
			ClojuredevPlugin.logError("error while getting text from file " + file, e);
			return null;
		}
    }
    
    private static boolean tryNonJavaResources(Object[] nonJavaResources, String searchedFileName, int line) throws PartInitException {
		for (Object nonJavaResource: nonJavaResources) {
			String nonJavaResourceName = null;
			if (IFile.class.isInstance(nonJavaResource)) {
				nonJavaResourceName = ((IFile) nonJavaResource).getName();
			} else if (IJarEntryResource.class.isInstance(nonJavaResource)) {
				nonJavaResourceName = ((IJarEntryResource) nonJavaResource).getName();
			}
			if (searchedFileName.equals(nonJavaResourceName)) {
				IEditorPart editor = EditorUtility.openInEditor(nonJavaResource);
				gotoEditorLine(editor, line);
				return true;
			}
		}
		return false;
    }

	public static void gotoEditorLine(IEditorPart editor, int line) {
		if (ITextEditor.class.isInstance(editor)) {
			ITextEditor textEditor = (ITextEditor) editor;
			IRegion lineRegion;
			try {
				lineRegion = textEditor.getDocumentProvider().getDocument(editor.getEditorInput()).getLineInformation(line - 1);
				textEditor.selectAndReveal(lineRegion.getOffset(), lineRegion.getLength());
			} catch (BadLocationException e) {
				// TODO popup for a feedback to the user ?
				ClojuredevPlugin.logError("unable to select line " + line + " in the file", e);
			}
		}
	}

}
