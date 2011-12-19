/*******************************************************************************
 * Copyright (c) 2009 Christophe Grand.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Christophe Grand - initial API and implementation
 *******************************************************************************/
package ccw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;

public class StorageMarkerAnnotationModel extends AbstractMarkerAnnotationModel {
	public final static String STORAGE_ID = "ccw.markers.STORAGE_ID";

	class ResourceChangeListener implements IResourceChangeListener {

		public void resourceChanged(IResourceChangeEvent e) {
			IResourceDelta delta = e.getDelta();
			if (delta != null) {
				IResourceDelta child = delta.findMember(ResourcesPlugin
						.getWorkspace().getRoot().getFullPath());
				if (child != null)
					update(child.getMarkerDeltas());
			}
		}

	}

	public final IStorage fStorage;

	public final IResourceChangeListener resourceChangeListener = new ResourceChangeListener();

	public StorageMarkerAnnotationModel(IStorage storage) {
		super();
		this.fStorage = storage;
	}

	protected void update(IMarkerDelta[] markerDeltas) {
		Set<IMarker> removed = new HashSet<IMarker>();
		Set<IMarker> modified = new HashSet<IMarker>();

		for (IMarkerDelta markerDelta : markerDeltas) {
			IMarker marker = markerDelta.getMarker();
			if (marker.exists() && !isAcceptable(marker)) continue;

			switch (markerDelta.getKind()) {
			case IResourceDelta.ADDED:
				addMarkerAnnotation(marker);
				break;

			case IResourceDelta.REMOVED:
				removed.add(marker);
				break;

			case IResourceDelta.CHANGED:
				modified.add(marker);
				break;
			}
		}
		for (IMarker marker : modified) {
			modifyMarkerAnnotation(marker);
		}
		for (IMarker marker : removed) {
			removeMarkerAnnotation(marker);
		}
		fireModelChanged();
	}

	public static void addAttribute(Map attributes, IStorage storage) {
		attributes.put(STORAGE_ID, storageId(storage));
	}

	public static void associate(IMarker marker, IStorage storage) {
		try {
			marker.setAttribute(STORAGE_ID, storageId(storage));
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void deleteMarkers(IMarker[] markers) throws CoreException {
		ResourcesPlugin.getWorkspace().deleteMarkers(markers);
	}

	@Override
	protected boolean isAcceptable(IMarker marker) {
		try {
			return storageId(fStorage).equals(marker.getAttribute(STORAGE_ID));
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private static String storageId(IStorage storage) {
		return storage.getFullPath().toPortableString();
	}

	@Override
	protected void listenToMarkerChanges(boolean listen) {
		if (listen)
			ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
		else
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);

	}

	@Override
	protected IMarker[] retrieveMarkers() throws CoreException {
		IMarker[] allMarkers = ResourcesPlugin.getWorkspace().getRoot()
				.findMarkers(null, true, IResource.DEPTH_ZERO);
		ArrayList<IMarker> markers = new ArrayList<IMarker>();
		for (IMarker marker : allMarkers)
			if (isAcceptable(marker))
				markers.add(marker);
		return markers.toArray(new IMarker[0]);
	}

}
