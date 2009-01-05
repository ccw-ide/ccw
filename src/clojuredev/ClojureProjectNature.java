package clojuredev;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.launching.JavaRuntime;

import clojure.util.ClojurePlugin;

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
        // IProjectDescription desc = project.getDescription();
        // ICommand[] commands = desc.getBuildSpec();
        // boolean found = false;
        //
        // for (int i = 0; i < commands.length; ++i) {
        // if (commands[i].getBuilderName().equals(ClojureBuilder.BUILDER_ID)) {
        // found = true;
        // break;
        // }
        // }
        // if (!found) {
        // // add builder to project
        // ICommand command = desc.newCommand();
        // command.setBuilderName(ClojureBuilder.BUILDER_ID);
        // ICommand[] newCommands = new ICommand[commands.length + 1];
        //
        // // Add it before other builders.
        // System.arraycopy(commands, 0, newCommands, 1, commands.length);
        // newCommands[0] = command;
        // desc.setBuildSpec(newCommands);
        // project.setDescription(desc, null);
        // }
        // Check that we actally have a project
        if (project == null) {
            ClojuredevPlugin.logError(
                    "Could not add Scala builder: project is null", null);
            return;
        }

        // closed clojure projects cannot be modified
        if (!project.isOpen()) {
            return;
        }

        // get project description
        IProjectDescription desc;
        try {
            desc = project.getDescription();
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError("Could not get project description", e);
            return;
        }

        // get project build specification
        ICommand[] spec = desc.getBuildSpec();

        int javaBuilderSpec = -1;
        // see if clojure builder is already there
        for (int i = 0; i < spec.length; i++) {
            // System.err.println("build: " + spec[i].getBuilderName());
            if (spec[i].getBuilderName().equals(ClojureBuilder.BUILDER_ID)) {
                return;
            }
            else if (spec[i].getBuilderName().equals(JavaCore.BUILDER_ID)) {
                javaBuilderSpec = i;
            }
        }
        ICommand newCommand = desc.newCommand();
        newCommand.setBuilderName(ClojureBuilder.BUILDER_ID);
        ICommand[] newSpec;
        if (javaBuilderSpec == -1) {
            System.err.println("Java Builder not found");
            newSpec = new ICommand[spec.length + 1];
            System.arraycopy(spec, 0, newSpec, 1, spec.length);
            newSpec[0] = newCommand;
        }
        else {
            newSpec = new ICommand[spec.length];
            System.arraycopy(spec, 0, newSpec, 0, spec.length);
            newSpec[javaBuilderSpec] = newCommand;
        }
        // set the new build spec
        desc.setBuildSpec(newSpec);

        // set the new description to the project
        try {
            project.setDescription(desc, IResource.FORCE, null);
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError("Could not set project description", e);
        }
        { // setup the .classpath file!
            ClojureProject clojureProject = ClojureCore
                    .getClojureProject(project);
            JavaProject javaProject = (JavaProject) clojureProject
                    .getJavaProject();
            IClasspathEntry[] entriesOld = javaProject.getRawClasspath();
            IClasspathEntry[] entriesNew = new IClasspathEntry[entriesOld.length + 2];
            System.arraycopy(entriesOld, 0, entriesNew, 0, entriesOld.length);

            for (int i = 0; i < entriesOld.length; i++) {
                if (entriesOld[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    IFolder src = project.getFolder("src");
                    if (!src.exists())
                        src.create(true, true, null);
                    entriesNew[i] = JavaCore.newSourceEntry(src.getFullPath());
                }
            }

            entriesNew[entriesOld.length + 0] = JavaCore.newLibraryEntry(Path
                    .fromPortableString(ClojurePlugin.getDefault().binPath()), null, null);
            entriesNew[entriesOld.length + 1] = JavaCore.newContainerEntry(Path
                    .fromPortableString(JavaRuntime.JRE_CONTAINER));

            javaProject.setRawClasspath(entriesNew, null);
            javaProject.save(null, true);
        }
    }

    public void deconfigure() throws CoreException {
        // Check that we actally have a project
        if (project == null) {
            ClojuredevPlugin.logError(
                    "Could not remove Scala builder: project is null", null);
            return;
        }

        // closed clojure projects cannot be modified
        if (!project.isOpen()) {
            return;
        }

        // get project description
        IProjectDescription desc;
        try {
            desc = project.getDescription();
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError("Could not get project description", e);
            return;
        }

        // look for clojure builder
        int index = -1;
        ICommand[] cmds = desc.getBuildSpec();
        for (int i = 0; i < cmds.length; i++) {
            if (cmds[i].getBuilderName().equals(
                    ClojureBuilder.BUILDER_ID)) {
                index = i;
                break;
            }
        }

        // builder was not found, so no need to remove it
        if (index == -1) {
            return;
        }

        // builder was found so remove it
        ArrayList<ICommand> list = new ArrayList<ICommand>();
        list.addAll(Arrays.asList(cmds));
        list.remove(index);
        ICommand[] newCmds = (ICommand[]) list
                .toArray(new ICommand[list.size()]);

        // set back the project description
        desc.setBuildSpec(newCmds);
        try {
            project.setDescription(desc, null);
        }
        catch (CoreException e) {
            ClojuredevPlugin.logError("Could not set project description", e);
        }
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

}
