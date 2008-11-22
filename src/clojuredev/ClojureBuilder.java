package clojuredev;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import clojure.lang.PersistentList;

public class ClojureBuilder extends IncrementalProjectBuilder {

    static public final String BUILDER_ID = "clojuredev.builder";

    public ClojureBuilder() {
    }
    
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
            IFile deltaFile = (IFile) delta.getResource();
            monitor.beginTask(
                    "Evaluating " + deltaFile.getName(),
                    IProgressMonitor.UNKNOWN);
            Reader fr = null;
            try {
            	fr = new InputStreamReader(deltaFile.getContents());
                ParseIterator parser = new ParseIterator(fr);

                while (parser.hasNext()) {
                    Object exp = parser.next();
                    if (exp instanceof PersistentList) {
                        PersistentList l = (PersistentList) exp;
                        System.out.println("first of exp => " + l.first());
                    }
                }
                monitor.worked(1);
                monitor.done();
            }
            catch (Exception e) {
                // TODO: add to compiler errors
                System.out.println("eval " + delta.getFullPath().lastSegment()
                        + " failed:");
                e.printStackTrace(System.out);
            } finally {
            	if (fr != null) {
            		try {
						fr.close();
					} catch (IOException e) {}
            	}
            }
        }
    }

}
