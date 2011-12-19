/*******************************************************************************
 * Copyright (c) 2009 Christophe Grand and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Christophe Grand  - initial API and implementation
 *******************************************************************************/
package ccw.debug;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;

import ccw.StorageMarkerAnnotationModel;

public class ClojureLineBreakpointAdapter implements IToggleBreakpointsTarget {

	public boolean canToggleLineBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		if (part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			return isCljFile(editor);
		}
		return false;
	}

	public boolean canToggleMethodBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		return false;
	}

	public boolean canToggleWatchpoints(IWorkbenchPart part,
			ISelection selection) {
		return false;
	}

	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
			throws CoreException {
		TextSelection textSelection = (TextSelection) selection;
		int lineNumber = textSelection.getStartLine();
		IBreakpoint[] breakpoints = DebugPlugin.getDefault()
				.getBreakpointManager().getBreakpoints();

		if (part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			IResource resource = (IResource) editor.getEditorInput()
					.getAdapter(IResource.class);

			if (resource != null) {
				for (int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint = breakpoints[i];
					if (resource.equals(breakpoint.getMarker().getResource())) {
						if (((ILineBreakpoint) breakpoint).getLineNumber() == (lineNumber + 1)) {
							breakpoint.delete();
							return;
						}
					}
				}
				JDIDebugModel.createStratumBreakpoint(resource, "Clojure", resource.getName(), null, null, lineNumber + 1, -1, -1, 0, true, null);
			} else {
				// Do it "the hard way" by using the WorkspaceRoot as the host for our breakpoint
				// ... quick analysis seems to indicate it's done this way by the JDT "itself" !
				IStorageEditorInput input = (IStorageEditorInput) editor.getEditorInput();
				IStorage storage = input.getStorage();

				for (int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint = breakpoints[i];
					if (breakpoint instanceof IJavaStratumLineBreakpoint) {
						IJavaStratumLineBreakpoint stratumBreakpoint = (IJavaStratumLineBreakpoint) breakpoint;
						if (storage.getFullPath().toPortableString().equals(stratumBreakpoint.getSourcePath())) {
							if (((ILineBreakpoint) breakpoint).getLineNumber() == (lineNumber + 1)) {
								breakpoint.delete();
								return;
							}
						}
					}
				}
				Map attributes = new HashMap();
				StorageMarkerAnnotationModel.addAttribute(attributes, storage);
				JDIDebugModel.createStratumBreakpoint(ResourcesPlugin.getWorkspace().getRoot(),
						"Clojure", storage.getName(), storage.getFullPath().toPortableString(),
						null, lineNumber + 1, -1, -1, 0, true, attributes);
			}
		}
	}

	public void toggleMethodBreakpoints(IWorkbenchPart part,
			ISelection selection) throws CoreException {
	}

	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection)
			throws CoreException {
	}

	public static boolean isCljFile(IEditorPart editorPart) {
		IResource resource = (IResource) editorPart.getEditorInput()
				.getAdapter(IResource.class);
		if (resource != null && "clj".equals(resource.getFileExtension())) {
			return true;
		}

		if (editorPart.getEditorInput() instanceof IStorageEditorInput) {
			try {
				IStorageEditorInput input = (IStorageEditorInput) editorPart.getEditorInput();
				if (input.getStorage().getName().endsWith(".clj")) {
					return true;
				}
			} catch (CoreException e) {
				// Nothing more to do :-(
			}
		}

		return false;
	}

}
