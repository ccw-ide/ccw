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

// Created on 2004-10-25 by Thierry Monney
//$Id: ScalaCore.java,v 1.3 2006/02/03 12:41:22 mcdirmid Exp $
package ccw;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.LocalFileStorageEditorInput;
import org.eclipse.jdt.internal.debug.ui.ZipEntryStorageEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import ccw.editors.clojure.ClojureEditor;
import ccw.repl.REPLView;
import clojure.lang.RT;
import clojure.lang.Var;

/**
 * This class acts as a Facade for all SDT core functionality.
 * 
 */
public final class ClojureCore {
	
	static public final String NATURE_ID = "ccw.nature";
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
        return CCWPlugin.getDefault().getPluginPreferences();
    }

    private static boolean addNature(IProject project, String natureID) {
        IProjectDescription desc;
        try {
            desc = project.getDescription();
        }
        catch (CoreException e) {
            CCWPlugin.logError("Could not get project description", e);
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
            CCWPlugin.logError("Could not set project description", e);
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
        return addNature(project, NATURE_ID);
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
                    || !project.hasNature(NATURE_ID))
                return null;
        }
        catch (CoreException e) {
            CCWPlugin.logError(e);
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
                    || !project.hasNature(NATURE_ID))
                return null;
        }
        catch (CoreException e) {
            CCWPlugin.logError(e);
            return null;
        }
        p = new ClojureProject(project);
        return p;
    }

    /**
     * Gets all the Clojure projects in the workspace
     * 
     * @return an array containing all the Scala projects
     */
    public static ClojureProject[] getClojureProjects() {
        return clojureProjects.values().toArray(new ClojureProject[] {});
    }
    
	/*
     *  TODO Still 1 more case to handle:
     *  - when a LIBRARY does not have source file in its classpath, then search attached source files folder/archive 
     */
    public static void openInEditor(String searchedNS, String searchedFileName, int line) {
		try {
		    REPLView replView = REPLView.activeREPL.get();
		    if (replView != null) {
    			String projectName = replView.getLaunch().getLaunchConfiguration().getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
    	    	openInEditor(searchedNS, searchedFileName, line, projectName, false);
		    }
		} catch (CoreException e) {
			CCWPlugin.logError("error while trying to obtain project's name from configuration, while trying to show source file of a symbol", e);
		}
	}
    
    private static boolean openInEditor(String searchedNS, final String initialSearchedFileName, int line, String projectName, boolean onlyExportedEntries) throws PartInitException {
    	if (initialSearchedFileName==null) {
    		return false;
    	}
    	String searchedFileName = initialSearchedFileName;
    	// TODO at some point in time, remove the second behaviour
    	// (when I estimate everybody is using the newer clojure version
    	if (searchedFileName.contains("/")) {
    		// new clojure version with namespace in searchedFileName
    		//searchedNS = searchedFileName.substring(0, searchedFileName.lastIndexOf('.')).replace('/', '.');
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
			CCWPlugin.logError("error getting classpathEntries of javaProject " + javaProject.getPath() 
					+ " while trying to localize for editor opening : " 
					+ searchedNS + " " + searchedFileName, e);
			return false;
		}

		for (IClasspathEntry cpe: classpathEntries) {
			switch (cpe.getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY:
				if (onlyExportedEntries && !cpe.isExported())
					continue;
				cpe = JavaCore.getResolvedClasspathEntry(cpe); // resolve if there are vars
				IPath sourcePath = cpe.getSourceAttachmentPath();
				if (sourcePath != null) {
					sourcePath = toOSAbsoluteIPath(sourcePath);
					File sourceFile = sourcePath.toFile();
					if (sourceFile.isDirectory()) {
						// find whether it's in there or not
						File maybeSourceFile = new File(sourceFile, initialSearchedFileName);
						if (maybeSourceFile.exists()) {
							LocalFileStorageEditorInput editorInput = new LocalFileStorageEditorInput(
									new LocalFileStorage(maybeSourceFile));
							IEditorPart editor = IDE.openEditor(CCWPlugin.getActivePage(), editorInput, ClojureEditor.ID);
							gotoEditorLine(editor, line);
							return true;
						} else {
							// let's fall through the CPE_SOURCE CASE
						}
					} else {
						try {
							ZipFile zipFile = new ZipFile(sourceFile, ZipFile.OPEN_READ);
							ZipEntry zipEntry = zipFile.getEntry(initialSearchedFileName);
							if (zipEntry != null) {
								ZipEntryStorageEditorInput editorInput = new ZipEntryStorageEditorInput(
										new ZipEntryStorage(zipFile, zipEntry));
								IEditorPart editor = IDE.openEditor(CCWPlugin.getActivePage(), editorInput, ClojureEditor.ID);
								gotoEditorLine(editor, line);
								return true;
							} else {
								// let's fall through the CPE_SOURCE CASE
							}
						} catch (IOException e) {
							CCWPlugin.logError("error while trying to open " + sourceFile + " for entry " + initialSearchedFileName, e);
						}
					}
				} else {
					// 	no source attachment path case, fall through CPE_SOURCE like case
				}
			case IClasspathEntry.CPE_SOURCE:
				IPackageFragmentRoot[] libPackageFragmentRoots = javaProject.findPackageFragmentRoots(cpe);
				for (IPackageFragmentRoot pfr: libPackageFragmentRoots) {
					try {
						if ("".equals(getPackageNameFromNamespaceName(searchedNS))) {
							try {
								if (tryNonJavaResources(pfr.getNonJavaResources(), searchedFileName, line)) {
									return true;
								}
							} catch (JavaModelException e) {
								CCWPlugin.logError("error with packageFragment " + pfr.getPath() 
										+ " while trying to localize for editor opening : " 
										+ searchedNS + " " + searchedFileName, e);
							}
						}
						for (IJavaElement javaElement: pfr.getChildren()) {
							if (!javaElement.getElementName().equals(getPackageNameFromNamespaceName(searchedNS)))
								continue;
							if (! (javaElement instanceof IPackageFragment))
								continue;
							IPackageFragment packageFragment = (IPackageFragment) javaElement;
							try {
								if (tryNonJavaResources(packageFragment.getNonJavaResources(), searchedFileName, line)) {
									return true;
								}
							} catch (JavaModelException e) {
								CCWPlugin.logError("error with packageFragment " + packageFragment.getPath() 
										+ " while trying to localize for editor opening : " 
										+ searchedNS + " " + searchedFileName, e);
							}
						}
					} catch (JavaModelException e) {
						CCWPlugin.logError("error with packageFragmentRoot " + pfr.getPath() 
								+ " while trying to localize for editor opening : " 
								+ searchedNS + " " + searchedFileName, e);
					}
				}
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
    
    public static IPath toOSAbsoluteIPath(IPath path) {
		if (ClojureCore.isWorkspaceRelativeIPath(path)) {
			boolean isFolder = path.getFileExtension() == null;
			if (isFolder) {
				path = ResourcesPlugin.getWorkspace().getRoot().getFolder(path).getLocation();
			} else {
				path = ResourcesPlugin.getWorkspace().getRoot().getFile(path).getLocation();
			}
		}
		return path;
    }
    
    public static boolean isWorkspaceRelativeIPath(IPath path) {
    	return ResourcesPlugin.getWorkspace().getRoot().exists(path);
	}

	public static String getPackageNameFromNamespaceName(String nsName) {
    	return (nsName.lastIndexOf(".") < 0) ? "" : nsName.substring(0, nsName.lastIndexOf(".")).replace('-', '_');
    }

	public static String getNamespaceNameFromPackageName(String packageName) {
		return packageName.toString().replace('/', '.').replace('_', '-');
	}
	
	private final static Pattern SEARCH_DECLARING_NAMESPACE_PATTERN
		= Pattern.compile("\\(\\s*(?:in-)?ns\\s+([^\\s\\)#\\[\\'\\{]+)"); 

	public static String findDeclaringNamespace(String sourceText) {
		Var sexp = RT.var("paredit.parser", "sexp");
		try {
			return (String) findDeclaringNamespace((Map) sexp.invoke(sourceText));
		} catch (Exception e) {
			return null;
		}
	}
	public static String findDeclaringNamespace(Map tree) {
		Var findNamespace = RT.var("ccw.static-analysis", "find-namespace");
		try {
			return (String) findNamespace.invoke(tree);
		} catch (Exception e) {
			return null;
		}
	}
	
	private final static Pattern HAS_NS_CALL_PATTERN = Pattern.compile("^\\s*\\(ns(\\s.*|$)", Pattern.MULTILINE);
	/**
	 * @return true if a ns call is detected by a regex-based heuristic
	 */
	private static boolean hasNsCall(String sourceCode) {
		Matcher matcher = HAS_NS_CALL_PATTERN.matcher(sourceCode);
		return matcher.find(); 
	}

	/**
	 * Get the file's namespace name if the file is a lib.
	 * <p>
	 * Checks:
	 * <ul>
	 *   <li>if the file is in the classpath</li>
	 *   <li>if the file ends with .clj</li>
	 *   <li>if the file contains a ns call(*)</li>
	 * </ul>
	 * If check is ko, returns nil, the file does not correspond to a 'lib'
	 * <br/>
	 * If check is ok, deduce the 'lib' name from the file path, e.g. a file
	 * path such as <code>/projectName/src/foo/bar_baz/core.clj</code> will 
	 * return "foo.bar-baz.core".
	 * </p>
	 * <p>
	 * (*): based on a simplistic regex based heuristic for maximum speed
	 * </p>
	 * @param file
	 * @return null if not a lib, String with lib namespace name if a lib
	 */
    public static String findMaybeLibNamespace(IFile file) {
    	try {
    		IJavaProject jProject = JavaCore.create(file.getProject());
    		IPackageFragmentRoot[] froots = jProject.getAllPackageFragmentRoots();
    		for (IPackageFragmentRoot froot: froots) {
    			if (froot.getPath().isPrefixOf(file.getFullPath())) {
    				String ret = findMaybeLibNamespace(file, froot.getPath());
    				if (ret != null) {
    					return ret;
    				} else {
    					continue;
    				}
    			}
    		}
		} catch (JavaModelException e) {
			CCWPlugin.logError("unable to determine the fragment root of the file " + file, e);
		}
		return null;
    }
    /**
     * @see <code>findMaybeLibNamespace(IFile file)</code>
     */
    public static String findMaybeLibNamespace(IFile file, IPath sourcePath) {
    	if (!CLOJURE_FILE_EXTENSION.equals(file.getFileExtension())) {
    		return null;
    	}
		IPath path = file.getFullPath().removeFirstSegments(sourcePath.segmentCount()).removeFileExtension();
		if (path == null) {
			// file is not on the classpath
			return null;
		} else {
			String sourceCode = getFileText(file);
			if (hasNsCall(sourceCode)) {
//				System.out.println("path.toPortableString()" + path.toPortableString());
				return getNamespaceNameFromPackageName(path.toPortableString());
			} else {
//				System.out.println("did not find a ns call in source code of " + file);
				return null;
			}
		}
    }
    
    private static String getFileText(IFile file) {
    	try {
			return (String) RT.var("clojure.core", "slurp").invoke(file.getLocation().toOSString());
		} catch (Exception e) {
			CCWPlugin.logError("error while getting text from file " + file, e);
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

	public static void gotoEditorLine(Object editor, int line) {
		if (ITextEditor.class.isInstance(editor)) {
			ITextEditor textEditor = (ITextEditor) editor;
			IRegion lineRegion;
			try {
				lineRegion = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).getLineInformation(line - 1);
				textEditor.selectAndReveal(lineRegion.getOffset(), lineRegion.getLength());
			} catch (BadLocationException e) {
				// TODO popup for a feedback to the user ?
				CCWPlugin.logError("unable to select line " + line + " in the file", e);
			}
		}
	}

}
