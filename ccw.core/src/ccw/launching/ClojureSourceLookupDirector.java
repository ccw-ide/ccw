package ccw.launching;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

/**
 * Source locator that not only locates java sources (via composition with JavaSourceLookupDirector)
 * @author lpetit
 *
 */
/*
 * TODO We could do better than just have copied the code from JavaSourceLookupDirector
 *      and rather have included a JavaSourceLookupDirector instance as a participant ?
 */
public class ClojureSourceLookupDirector extends AbstractSourceLookupDirector
		implements IPersistableSourceLocator {

	private static Set fFilteredTypes;
	
	static {
		fFilteredTypes = new HashSet();
		fFilteredTypes.add(ProjectSourceContainer.TYPE_ID);
		fFilteredTypes.add(WorkspaceSourceContainer.TYPE_ID);
		// can't reference UI constant
		fFilteredTypes.add("org.eclipse.debug.ui.containerType.workingSet"); //$NON-NLS-1$
	}
	
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] {new ClojureSourceLookupParticipant(), new JavaSourceLookupParticipant()});
	}

	public boolean supportsSourceContainerType(ISourceContainerType type) {
		return !fFilteredTypes.contains(type.getId());
	}

}
