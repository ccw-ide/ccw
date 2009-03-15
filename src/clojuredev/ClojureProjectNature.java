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
package clojuredev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;

import clojuredev.builder.ClojureBuilder;

/**
 * Borrowing heavily from the old Scala Eclipse plugin.
 * 
 * @author cmarshal
 * 
 */
public class ClojureProjectNature implements IProjectNature {

    static public final String NATURE_ID = "clojuredev.nature";

    private IProject project;

    public void configure() throws CoreException {
        IProjectDescription desc = getProjectDescription();
        
        if (desc == null) {
        	return;
        }

        ICommand[] spec = desc.getBuildSpec();	// get project build specification

        if (builderPresent(spec, ClojureBuilder.BUILDER_ID)) {
        	return;
        }
        
        if (!builderPresent(spec, JavaCore.BUILDER_ID)) {
        	ClojuredevPlugin.logError("Java Builder not found");
        	return;
        }
        
        ICommand clojureCommand = desc.newCommand();
        clojureCommand.setBuilderName(ClojureBuilder.BUILDER_ID);
        
        // Add clojure builder before all other builders (thus before
        // Java builder if present)
        ICommand[] newSpec = new ICommand[spec.length + 1];
        newSpec[0] = clojureCommand;
        System.arraycopy(spec, 0, newSpec, 1, spec.length);

        desc.setBuildSpec(newSpec);

        project.setDescription(desc, IResource.FORCE, null);
        
        setupClojureProjectClassPath();
    }
    
    private void setupClojureProjectClassPath() throws CoreException {
    	// idea: ensure that clojure and clojure-contrib are present if not
    	// add classes if not present
        ClojureProject clojureProject = ClojureCore.getClojureProject(project);
        IJavaProject javaProject = clojureProject.getJavaProject();
        
        if (!alreadyHasClojureLibOnClasspath(javaProject)) {
        	addClojureLibOnClasspath(javaProject);
        }
        if (!alreadyHasClojureContribLibOnClasspath(javaProject)) {
        	addClojureContribLibOnClasspath(javaProject);
        }
        if (!alreadyHasClassesDirectory(javaProject)) {
        	addClassesDirectory(javaProject);
        }
    }
    
	private void addClojureLibOnClasspath(IJavaProject javaProject) throws CoreException {
		addLibOnClasspath(javaProject, getDefaultClojureLib());
	}

	private void addLibOnClasspath(IJavaProject javaProject, File lib) throws CoreException {
		if (lib == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		addLibOnClasspath(javaProject, Path.fromOSString(lib.getAbsolutePath()));
	}
	
	private void addLibOnClasspath(IJavaProject javaProject, IPath lib) throws CoreException {
		if (lib == null)
			throw new CoreException(Status.CANCEL_STATUS);

	    IClasspathEntry[] entriesOld = javaProject.getRawClasspath();
	    
	    // Verify that lib not already in the classpath
	    for (IClasspathEntry cpe: entriesOld) {
	    	if (cpe.getPath().equals(lib)) {
	    		return;
	    	}
	    }
	    
	    IClasspathEntry[] entriesNew = new IClasspathEntry[entriesOld.length + 1];
	    
	    System.arraycopy(entriesOld, 0, entriesNew, 0, entriesOld.length);
	
    	entriesNew[entriesOld.length] = JavaCore.newLibraryEntry(lib, null, null);
	    
	    javaProject.setRawClasspath(entriesNew, null);
	    javaProject.save(null, true);
	}
	
	private void addClojureContribLibOnClasspath(IJavaProject javaProject) throws CoreException {
		addLibOnClasspath(javaProject, getDefaultClojureContribLib());
	}
	
	private void addClassesDirectory(IJavaProject javaProject) throws CoreException {
	    IFolder classesFolder = javaProject.getProject().getFolder("classes");
	    if (!classesFolder.exists()) {
	    	classesFolder.create(true, true, null);
	    }
	    
	    addLibOnClasspath(javaProject, classesFolder.getFullPath());
	}
	private boolean alreadyHasClojureLibOnClasspath(IJavaProject javaProject) throws JavaModelException {
    	return javaProject.findElement(new Path("clojure/lang")) != null;
    }

	private boolean alreadyHasClojureContribLibOnClasspath(IJavaProject javaProject) throws JavaModelException {
    	return javaProject.findElement(new Path("clojure/contrib")) != null;
    }

	private boolean alreadyHasClassesDirectory(IJavaProject javaProject) throws JavaModelException {
		return javaProject.findPackageFragmentRoot(javaProject.getProject().getFolder("classes").getFullPath()) != null;
    }

    private File getDefaultClojureLib() {
    	return getJarInsidePlugin("clojure", "clojure");
    }
    
    private File getDefaultClojureContribLib() {
    	return getJarInsidePlugin("clojurecontrib", "clojure-contrib");
    }
    
    private File getJarInsidePlugin(String pluginName, String jarName) {
    	try {
	        Bundle bundle = Platform.getBundle(pluginName);
	        File clojureBundlePath = FileLocator.getBundleFile(bundle);
	        if (clojureBundlePath.isFile()) {
	        	ClojuredevPlugin.logError(pluginName + " plugin should be deployed as a directory");
	        	return null;
	        }
	        
	        File clojureLibEntry = new File(clojureBundlePath, jarName + ".jar");
    		if (!clojureLibEntry.exists()) {
    			ClojuredevPlugin.logError("Unable to locate " + jarName + " jar in " + pluginName + " plugin");
    			return null;
	    	}
    		return clojureLibEntry;
    	} catch (IOException e) {
    		ClojuredevPlugin.logError("Unable to find " + pluginName + " plugin");
    		return null;
    	}
    }
    
    public void deconfigure() throws CoreException {
        IProjectDescription desc = getProjectDescription();
        
        if (desc == null) {
        	return;
        }
        
        ICommand[] spec = desc.getBuildSpec();
        
        if (!builderPresent(spec, ClojureBuilder.BUILDER_ID)) {
        	// builder was not found, so no need to remove it
        	return;
        }

        // builder was found so remove it
        ArrayList<ICommand> newSpec = new ArrayList<ICommand>(spec.length - 1);
        for (ICommand command: spec) {
        	if (!command.getBuilderName().equals(ClojureBuilder.BUILDER_ID)) {
        		newSpec.add(command);
        	}
        }
        
        // set back the project description
        desc.setBuildSpec(newSpec.toArray(new ICommand[0]));
        try {
            project.setDescription(desc, null);
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError("Could not set project description", e);
        }
    }

    private boolean builderPresent(ICommand[] builders, String builderName) {
	    for (ICommand builder: builders) {
	    	if (builder.getBuilderName().equals(builderName)) {
	            return true;
	        }
	    }
	    return false;
    }

    
    /**
     * @return the project description or null if the project
     *         is null, closed, or an error occured while getting description
     */
    private IProjectDescription getProjectDescription() {
        if (project == null) {
            ClojuredevPlugin.logError(
                    "Could not add or remove clojure nature: project is null", null);
            return null;
        }
        
        
        // closed clojure projects cannot be modified
        if (!project.isOpen()) {
            ClojuredevPlugin.logWarning(
                    "Nature modification asked on a closed project!");
            return null;
        }
        
        try {
            return project.getDescription();
        } catch (CoreException e) {
            ClojuredevPlugin.logError("Could not get project description", e);
            return null;
        }
    }
    
    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

}
