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


import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.ui.IEditorPart;

public class BreakpointAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) adaptableObject;
			if (ClojureLineBreakpointAdapter.isCljFile(editorPart)) {
				return new ClojureLineBreakpointAdapter();
			}
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IToggleBreakpointsTarget.class };
	}

}
