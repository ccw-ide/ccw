package ccw.util;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

public final class PlatformUtil {
	private PlatformUtil() {}

	/**
	 * First checks if <code>adaptable</code> is a direct instance of
	 * <code>adapterType</code>, then if it's an instance of
	 * <code>IAdaptable</code> and if this suffices to get the desired
	 * adaptation to <code>adapterType</code>, or else leverages the
	 * PlatformManager's discovery of adatapers
	 * @param adaptable
	 * @param adapterType
	 * @return
	 */
	public static Object getAdapter(Object adaptable, Class adapterType) {
		if (adaptable == null || adapterType == null) {
			return null;
		} else if (adapterType.isInstance(adaptable)) {
			return adaptable;
		} else {
			if (adaptable instanceof IAdaptable) {
				Object ret = ((IAdaptable) adaptable).getAdapter(adapterType);
				if (ret != null) {
					return ret;
				}
			}
			return Platform.getAdapterManager().getAdapter(adaptable, adapterType);
		}
	}
}
