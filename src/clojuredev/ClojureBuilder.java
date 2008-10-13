package clojuredev;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ClojureBuilder extends IncrementalProjectBuilder {

	static public final String BUILDER_ID = "clojuredev.builder";

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		System.out.println("build:" + kind + " args:" + args);
		IResourceDelta delta = getDelta(getProject());
		incrementalBuild(delta, monitor);
		return null;
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
		if (delta != null && delta.getResource() instanceof IFile) {
			IFile deltaFile = (IFile)delta.getResource();
			monitor.beginTask("Evaluating " + delta.getFullPath().lastSegment(), IProgressMonitor.UNKNOWN);
			try {
				Object result = clojure.lang.Compiler.loadFile(deltaFile.getLocation().toFile().toString());
				// TODO: get warnings?
				System.out.println("eval " + delta.getFullPath().lastSegment() + ": " + result.toString());
				monitor.worked(1);
				monitor.done();
			}
			catch (Exception e) {
				// TODO: add to compiler errors
				System.out.println("eval " + delta.getFullPath().lastSegment() + " failed:");
				e.printStackTrace(System.out);
			}
		}
	}

}
