/*******************************************************************************
 * Copyright (c) 2009 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/

package ccw.util;

import java.util.Map;

import clojure.lang.Keyword;

/**
 * @author Laurent
 *
 */
public final class ClojureDocUtils {
	private ClojureDocUtils() {
		// Not intended to be subclassed
	}

	public static final Keyword KEYWORD_ARGLISTS = Keyword.intern(null, "arglists");
	public static final Keyword KEYWORD_DOC = Keyword.intern(null, "doc");

	public static String getVarDocInfo(Object varObject) {
		Map<?,?> element = (Map<?,?>) varObject;

		StringBuilder result = new StringBuilder();

		String args = (String) ((Map<?,?>) element).get(KEYWORD_ARGLISTS);
		if (args != null && !args.trim().equals("")) {
			result.append("Arguments List(s):\n");

			String[] argsLines = args.split("\n");
			boolean firstLine = true;
			for (String line: argsLines) {
				if (line.startsWith("("))
					line = line.substring(1);
				if (line.endsWith(")"))
					line = line.substring(0, line.length() - 1);
				if (firstLine) {
					firstLine = false;
				} else {
					result.append("<br/>");
				}
				result.append(line);
			}
		}
		String maybeDoc = (String) ((Map<?,?>) element).get(KEYWORD_DOC);
		if (maybeDoc != null) {
			if (result.length() > 0) {
				result.append("\n\n");
			}
			result.append("Documentation:\n");
			result.append(maybeDoc);
		}

		if (result.length() != 0) {
			return result.toString();
		} else {
			return "no documentation information";
		}
	}

	public static String getHtmlVarDocInfo(Object varObject) {
		Map<?,?> element = (Map<?,?>) varObject;

		StringBuilder result = new StringBuilder();
		String args = (String) (((Map<?,?>) element).get(ClojureDocUtils.KEYWORD_ARGLISTS));
		if (args != null && !args.trim().equals("")) {
			result.append("<p><b>Arguments List(s):</b><br/>");

			String[] argsLines = args.split("\n");
			boolean firstLine = true;
			for (String line: argsLines) {
				if (line.startsWith("("))
					line = line.substring(1);
				if (line.endsWith(")"))
					line = line.substring(0, line.length() - 1);
				if (firstLine)
					firstLine = false;
				else
					result.append("<br/>");
				result.append(line);
			}
			result.append("</p><br/>");
		}

		String docString = (String) (((Map<?,?>) element).get(ClojureDocUtils.KEYWORD_DOC));
		if (docString != null && !docString.trim().equals(""))
			result.append("<p><b>Documentation:</b><br/>").append(rawDocStringToHtml(docString)).append("</p>");

		return result.toString();
	}
	/** Formats correctly a raw docstring by adding real line breaks between
	 * 2 line breaks, and rejoining lines separated by simple line breaks
	 * @param docString
	 * @return
	 */
	private static String rawDocStringToHtml(String docString) {
		return docString.replaceAll("\n", "<br/>");
	}
}
