/*******************************************************************************
 * Copyright (c) 2009 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.util;

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
