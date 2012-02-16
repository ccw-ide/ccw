package ccw.util;

public final class StringUtils {
	private StringUtils () { /* Not intended to be instanciated */ }
	public static boolean isEmpty(String s) {
		return s == null
			|| s.length() == 0;
	}
}
