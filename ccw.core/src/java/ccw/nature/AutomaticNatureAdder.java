package ccw.nature;

import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.JavaCore;

public class AutomaticNatureAdder {
	
	private IElementChangedListener elementChangedListener;
	
	public synchronized void start() {
		elementChangedListener = new ClojurePackageElementChangeListener();
		JavaCore.addElementChangedListener(elementChangedListener);
	}
	
	public synchronized void stop() {
		if (elementChangedListener == null) {
			JavaCore.removeElementChangedListener(elementChangedListener);
		}
		elementChangedListener = null;
	}

}
