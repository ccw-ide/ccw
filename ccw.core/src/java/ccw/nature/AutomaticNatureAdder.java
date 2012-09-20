package ccw.nature;

import org.eclipse.jdt.core.JavaCore;

public class AutomaticNatureAdder {
	
	private ClojurePackageElementChangeListener elementChangedListener;
	
	public synchronized void start() {
		elementChangedListener = new ClojurePackageElementChangeListener();
		JavaCore.addElementChangedListener(elementChangedListener);
		elementChangedListener.performFullScan();
	}
	
	public synchronized void stop() {
		if (elementChangedListener != null) {
			JavaCore.removeElementChangedListener(elementChangedListener);
			elementChangedListener = null;
		}
	}

}
