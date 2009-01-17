package util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {

	public static void safeClose(Closeable toClose) {
		if (toClose != null) {
			try {
				toClose.close();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}
}
