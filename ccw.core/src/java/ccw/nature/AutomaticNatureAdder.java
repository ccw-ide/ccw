package ccw.nature;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;

public class AutomaticNatureAdder {

	private ClojurePackageElementChangeListener elementChangedListener;
	private LeiningenProjectResourceListener leiningenProjectResourceListener;

	public synchronized void start() {
		elementChangedListener = new ClojurePackageElementChangeListener();
		JavaCore.addElementChangedListener(elementChangedListener);
		elementChangedListener.performFullScan();
		startLeininingenAdapter();
	}

	public synchronized void stop() {
		if (elementChangedListener != null) {
			JavaCore.removeElementChangedListener(elementChangedListener);
			elementChangedListener = null;
			stopLeiningenAdapter();
		}
	}

	private void startLeininingenAdapter() {
		leiningenProjectResourceListener = new LeiningenProjectResourceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(leiningenProjectResourceListener);
		leiningenProjectResourceListener.performFullScan();
	}

	private void stopLeiningenAdapter() {
		if (leiningenProjectResourceListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(leiningenProjectResourceListener);
			leiningenProjectResourceListener = null;
		}
	}

}
