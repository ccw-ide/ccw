package ccw.util;

import org.eclipse.swt.graphics.RGB;

public final class RGBUtil {
	private RGBUtil() {}
	
    public static RGB lighten(RGB c, double pct) {
    	return new RGB(
    			lighten(c.red, pct),
    			lighten(c.green, pct),
    			lighten(c.blue, pct));
    }
    
    private static int lighten(int c, double pct) {
    	double nc = c + (255 - c) * pct;
    	return Math.max(0,  Math.min(255, (int) nc));
    }
    

}
