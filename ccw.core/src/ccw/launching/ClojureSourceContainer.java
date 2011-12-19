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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

/**
 * does not dispose delegatee resources
 * @author lpetit
 *
 */
public class ClojureSourceContainer implements
		ISourceContainer {

	private final ISourceContainer delegatee;

	public ClojureSourceContainer(ISourceContainer delegatee) {
		this.delegatee = delegatee;
	}

	public void dispose() {
		// Nothing yet
	}

	public Object[] findSourceElements(String name) throws CoreException {
		return delegatee.findSourceElements(name);
	}

	public String getName() {
		return "Clojure decorator source container on top of " + delegatee.getName();
	}

	public ISourceContainer[] getSourceContainers() throws CoreException {
		return delegatee.getSourceContainers();
	}

	public ISourceContainerType getType() {
		return delegatee.getType();
	}

	public void init(ISourceLookupDirector director) {
		delegatee.init(director);
	}

	public boolean isComposite() {
		return delegatee.isComposite();
	}

	public Object getAdapter(Class adapter) {
		return delegatee.getAdapter(adapter);
	}
}
