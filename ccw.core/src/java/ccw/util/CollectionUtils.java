package ccw.util;

import java.util.Collection;

public final class CollectionUtils {
	private CollectionUtils() {}
	
	public static String join(Collection<String> c, String sep) {
		StringBuilder sb = new StringBuilder();
		boolean seenFirst = false;
		for (String e: c) {
			if (seenFirst) {
				sb.append(sep);
			} else {
				seenFirst = true;
			}
			sb.append(e);
		}
		return sb.toString();
	}
}
