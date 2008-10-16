/*                     __                                               *\
 **     ________ ___   / /  ___     Scala Plugin for Eclipse             **
 **    / __/ __// _ | / /  / _ |    (c) 2004-2005, LAMP/EPFL             **
 **  __\ \/ /__/ __ |/ /__/ __ |                                         **
 ** /____/\___/_/ |_/____/_/ | |                                         **
 **                          |/                                          **
 \*                                                                      */

// Created on 2004-10-25 by Thierry Monney
//$Id: ScalaCore.java,v 1.3 2006/02/03 12:41:22 mcdirmid Exp $
package clojuredev;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * This class acts as a Facade for all SDT core functionality.
 * 
 */
public class ClojureCore {

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
        return addNature(project, ClojureNature.NATURE_ID);
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
                    || !project.hasNature(ClojureNature.NATURE_ID))
                return null;
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError(e);
            return null;
        }
        IJavaProject p = (IJavaProject) javaProjects.get(project);
        if (p == null) {
            p = JavaCore.create(project);
            // setScalaExclusionFilter(p);
            String filters = p.getOption(EXCLUSION_FILTER_ID, true);
            if (filters != null && !filters.equals("")) {
                filters += ",";
            }
            filters += CLOJURE_FILE_FILTER;
            p.setOption(EXCLUSION_FILTER_ID, filters);
            javaProjects.put(project, p);
        }
        return p;
    }

    /**
     * Gets the Scala project associated to the given project
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
                    || !project.hasNature(ClojureNature.NATURE_ID))
                return null;
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError(e);
            return null;
        }
        p = new ClojureProject(project);
        return p;
    }

    /**
     * Gets all the Scala projects in the workspace
     * 
     * @return an array containing all the Scala projects
     */
    public static ClojureProject[] getClojureProjects() {
        return clojureProjects.values().toArray(new ClojureProject[] {});
    }

}