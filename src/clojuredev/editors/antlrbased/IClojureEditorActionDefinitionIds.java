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
package clojuredev.editors.antlrbased;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Defines the definition IDs for the Clojure editor actions.
 */
public interface IClojureEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {

	/**
	 * Action definition ID of the edit -> go to matching bracket action
	 * (value <code>"clojuredev.ui.edit.text.clojure.goto.matching.bracket"</code>).
	 *
	 * @since 2.1
	 */
	public static final String GOTO_MATCHING_BRACKET= "clojuredev.ui.edit.text.clojure.goto.matching.bracket"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> go to next member action
	 * (value <code>"clojuredev.ui.edit.text.clojure.goto.matching.bracket"</code>).
	 *
	 * @since 2.1
	 */
	public static final String GOTO_NEXT_MEMBER= "clojuredev.ui.edit.text.clojure.goto.next.member"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> go to previous member action
	 * (value <code>"clojuredev.ui.edit.text.clojure.goto.previous.member"</code>).
	 *
	 * @since 2.1
	 */
	public static final String GOTO_PREVIOUS_MEMBER= "clojuredev.ui.edit.text.clojure.goto.previous.member"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the Clojure (or edit ?) -> select top level s expression action 
	 */
	public static final String SELECT_TOP_LEVEL_S_EXPRESSION = "clojuredev.ui.edit.text.clojure.select.toplevel.s.expression";
	
	/**
	 * Action definition ID of the Clojure (or edit ?) -> evaluate top level s expression action 
	 */
	public static final String EVALUATE_TOP_LEVEL_S_EXPRESSION = "clojuredev.ui.edit.text.clojure.evaluate.toplevel.s.expression";
	
	/**
	 * Action definition ID of the Clojure (or edit ?) -> evaluate region action 
	 */
	public static final String EVALUATE_REGION = "clojuredev.ui.edit.text.clojure.evaluate.region";
	
}
