/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.editors.antlrbased;

import java.util.ResourceBundle;
import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
final class ClojureEditorMessages extends NLS {
	private static final String BUNDLE_NAME= "ccw.editors.antlrbased.ClojureEditorMessages";

	private static ResourceBundle bundleForConstructedKeys= ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Returns the message bundle which contains constructed keys.
	 * @return the message bundle
	 */
	public static ResourceBundle getBundleForConstructedKeys() {
		return bundleForConstructedKeys;
	}

	private ClojureEditorMessages() {
		// Not intended to be instanciated
	}
	
	public static String GotoNextMember_label;
	public static String GotoPreviousMember_label;
	public static String SelectTopLevelSExpressionAction_label;
	public static String EvaluateTopLevelSExpressionAction_label;
	public static String LoadFileAction_label;
	public static String CompileLibAction_label;
	public static String GotoMatchingBracket_label;
	public static String GotoMatchingBracket_error_invalidSelection;
	public static String GotoMatchingBracket_error_noMatchingBracket;
	public static String GotoMatchingBracket_error_bracketOutsideSelectedElement;

	static {
		NLS.initializeMessages(BUNDLE_NAME, ClojureEditorMessages.class);
	}

}