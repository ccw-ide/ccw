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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

/**
 * Clojure code source lookup participant.
 *
 * Some JDT utility classes are not accessible.
 * So this class presumes that it is used with a sibling
 * JavaSourceLookupParticipant in a same SourceLookupDirector
 *
 * @note some code borrowed from the JDT
 * @author lpetit
 *
 */
public class ClojureSourceLookupParticipant extends
		AbstractSourceLookupParticipant implements ISourceLookupParticipant {

	/**
	 * Cache of sibling java lookup participant with performance considerations in mind.
	 * This object is necessary so that we can access certain hidden utility methods indirectly
	 * (e.g. have cachedSiblingJavaParticipant.getSourceName() do the job for us)
	 * */
	JavaSourceLookupParticipant cachedSiblingJavaParticipant;

	/**
	 * Map of delegate source containers.
	 * Everything possible is converted to plain FolderSourceContainer or ArchiveSourceContainer
	 * if possible.
	 * @note borrowed from the JDT (JavaSourceLookupParticipant)
	 */
	private Map<ISourceContainer, ISourceContainer> delegateContainers;


	public String getSourceName(Object object) throws CoreException {
		JavaSourceLookupParticipant javaParticipant = findSiblingJavaParticipant();
		if (javaParticipant == null) {
			return null;
		} else {
			return javaParticipant.getSourceName(object);
		}
	}

	private JavaSourceLookupParticipant findSiblingJavaParticipant() throws CoreException {
		if (cachedSiblingJavaParticipant == null) {
			for (ISourceLookupParticipant p: getDirector().getParticipants()) {
				if (p instanceof JavaSourceLookupParticipant) {
					this.cachedSiblingJavaParticipant = (JavaSourceLookupParticipant) p;
					break;
				}
			}
		}
		return cachedSiblingJavaParticipant;
	}

	public void init(ISourceLookupDirector director) {
		super.init(director);
		delegateContainers = new HashMap<ISourceContainer, ISourceContainer>();
	}

	protected ISourceContainer getDelegateContainer(ISourceContainer container) {
		ISourceContainer delegate = delegateContainers.get(container);
		if (delegate == null) {
			return container;
		}
		return delegate;
	}


	public void sourceContainersChanged(ISourceLookupDirector director) {
		disposeAndClearDelegateContainers(delegateContainers);

		for (ISourceContainer container: director.getSourceContainers()) {
			delegateContainers.put(container, new ClojureSourceContainer(container));
//			// TODO creer que des ArchiveSourceContainer pour que ca trouve les fichiers clojure
//			//      en cherchant qd mm a utiliser les archives de sources en priorite
//			if (container.getType().getId().equals(ArchiveSourceContainer.TYPE_ID)) {
//				IFile file = ((ArchiveSourceContainer)container).getFile();
//				IProject project = file.getProject();
//				IJavaProject javaProject = JavaCore.create(project);
//				if (javaProject.exists()) {
//					try {
//						IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
//						for (int j = 0; j < roots.length; j++) {
//							IPackageFragmentRoot root = roots[j];
//							if (file.equals(root.getUnderlyingResource())) {
//								IPath sourcePath = root.getSourceAttachmentPath();
//								if (sourcePath != null) {
//									delegateContainers.put(container, new ExternalArchiveSourceContainer(
//											sourcePath.toOSString(), true));
////								} else {
////									delegateContainers.put(container, new ArchiveSourceContainer(file, true));
//								}
//							} else {
//								IPath path = root.getSourceAttachmentPath();
//								if (path != null) {
//									if (file.getFullPath().equals(path)) {
//										delegateContainers.put(container, new ExternalArchiveSourceContainer(
//												path.toOSString(), true));
////									} else {
////										delegateContainers.put(container, new ArchiveSourceContainer(file, true));
//									}
//								}
//							}
//						}
//					} catch (JavaModelException e) {
//					}
//				}
//			}
		}
	}

	@Override
	public void dispose() {
		this.cachedSiblingJavaParticipant = null;

		disposeAndClearDelegateContainers(delegateContainers);
		delegateContainers = null;

		super.dispose();
	}

	private void disposeAndClearDelegateContainers(Map<ISourceContainer, ISourceContainer> delegateContainers) {
		if (delegateContainers != null) {
			for (ISourceContainer sc: delegateContainers.values()) {
				sc.dispose();
			}
		}
		delegateContainers.clear();
	}
}
