/*******************************************************************************
 * Copyright (c) 2009 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
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
