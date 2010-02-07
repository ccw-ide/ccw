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

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Defines the definition IDs for the Clojure editor actions.
 */
public interface IClojureEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {
    /**
     * Action definition ID of the edit -> go to matching bracket action (value
     * <code>"ccw.ui.edit.text.clojure.goto.matching.bracket"</code>).
     * 
     * @since 2.1
     */
    public static final String GOTO_MATCHING_BRACKET = "ccw.ui.edit.text.clojure.goto.matching.bracket"; //$NON-NLS-1$
    public static final String SELECT_TO_MATCHING_BRACKET = "ccw.ui.edit.text.clojure.select.to.matching.bracket";
    /**
     * Action definition ID of the edit -> go to next member action (value
     * <code>"ccw.ui.edit.text.clojure.goto.matching.bracket"</code>).
     * 
     * @since 2.1
     */
    public static final String GOTO_NEXT_MEMBER = "ccw.ui.edit.text.clojure.goto.next.member"; //$NON-NLS-1$
    /**
     * Action definition ID of the edit -> go to previous member action (value
     * <code>"ccw.ui.edit.text.clojure.goto.previous.member"</code>).
     * 
     * @since 2.1
     */
    public static final String GOTO_PREVIOUS_MEMBER = "ccw.ui.edit.text.clojure.goto.previous.member"; //$NON-NLS-1$
    /**
     * Action definition ID of the Clojure (or edit ?) -> select top level s
     * expression action
     */
    public static final String SELECT_TOP_LEVEL_S_EXPRESSION = "ccw.ui.edit.text.clojure.select.toplevel.s.expression";
    /**
     * Action definition ID of the Clojure (or edit ?) -> evaluate top level s
     * expression or region action
     */
    public static final String EVALUATE_TOP_LEVEL_S_EXPRESSION = "ccw.ui.edit.text.clojure.evaluate.toplevel.s.expression";
    /**
     * Action definition ID of the Clojure (or edit ?) -> load file action
     */
    public static final String LOAD_FILE = "ccw.ui.edit.text.clojure.load.file";
    /**
     * Action definition ID of the Clojure (or edit ?) -> compile lib action
     */
    public static final String COMPILE_LIB = "ccw.ui.edit.text.clojure.compile.lib";
    /**
     * Action definition ID of the Clojure (or edit ?) -> format action
     */
    public static final String FORMAT_CODE = "ccw.ui.edit.text.clojure.format.code";
    public static final String HIGHLIGHT_SAME_WORD = "ccw.ui.edit.text.clojure.highlight.same.word";
    public static final String RUN_TESTS = "ccw.ui.edit.text.clojure.run.tests";
}
