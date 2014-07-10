package ccw.util;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

import ccw.CCWPlugin;

public final class StyledTextUtil {

	private StyledTextUtil() {}
	
    public static void lightenStyledTextColors(StyledText st, double pct) {
    	StyleRange[] srs = st.getStyleRanges();
    	Color defaultFGColor = CCWPlugin.getColor(RGBUtil.lighten(st.getForeground().getRGB(), pct));
    	for (int i = 0; i < srs.length; i++) {
    		StyleRange oldSR = srs[i];
    		StyleRange newSR = newStyleRange(oldSR);
    		Color lightForeground = (oldSR.foreground == null) 
		             ? defaultFGColor 
		             : CCWPlugin.getColor(RGBUtil.lighten(oldSR.foreground.getRGB(), pct));
    		newSR.foreground = lightForeground;
    		st.setStyleRange(newSR);
    	}
    	st.setForeground(defaultFGColor);
    }

    public static StyleRange newStyleRange(StyleRange from) {
    	StyleRange r = new StyleRange(from);
    	r.start = from.start;
    	r.length = from.length;
    	return r;
    }

}
