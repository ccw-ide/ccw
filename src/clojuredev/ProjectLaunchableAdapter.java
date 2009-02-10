package clojuredev;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.ILaunchable;

public class ProjectLaunchableAdapter implements IAdapterFactory {
	private static final Class[] adapterList = new Class[] { ILaunchable.class }; 

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		return new ILaunchable() {};
	}

	public Class[] getAdapterList() {
		return adapterList;
	}

}
