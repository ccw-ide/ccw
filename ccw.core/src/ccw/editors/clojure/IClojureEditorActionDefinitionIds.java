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
package ccw.editors.clojure;

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
     * Action definition ID of the Clojure -> select last (selection)
     * expression action
     */
    public static final String SELECT_LAST = "ccw.ui.edit.text.clojure.select.last";

    /**
     * Action definition ID of the Clojure -> raise selection
     * expression action
     */
    public static final String RAISE_SELECTION = "ccw.ui.edit.text.clojure.select.raise";

    /**
     * Action definition ID of the Clojure -> indent selection
     * expression action
     */
    public static final String INDENT_SELECTION = "ccw.ui.edit.text.clojure.indent.selection";

    /**
     * Action definition ID of the Clojure -> split sexpr
     * expression action
     */
    public static final String SPLIT_SEXPR = "ccw.ui.edit.text.clojure.split.sexpr";

    /**
     * Action definition ID of the Clojure -> join sexpr
     * expression action
     */
    public static final String JOIN_SEXPR = "ccw.ui.edit.text.clojure.join.sexpr";

    /**
     * Action definition ID of the Clojure -> Switch structural edition mode
     * expression action
     */
    public static final String SWITCH_STRUCTURAL_EDITION_MODE = "ccw.ui.edit.text.clojure.switch.structuraledition.mode";
    public static final String SWITCH_STRUCTURAL_EDITION_MODE2 = "SwitchStructuralEditionModeAction";

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
     * Action definition ID of the Clojure (or edit ?) -> switch REPL to file's namespace
     */
    public static final String SWITCH_NS = "ccw.ui.edit.text.clojure.switchNamespace";
    /**
     * Action definition ID of the Clojure (or edit ?) -> compile lib action
     */
    public static final String COMPILE_LIB = "ccw.ui.edit.text.clojure.compile.lib";
    /**
     * Action definition ID of the Clojure (or edit ?) -> format action
     */
    public static final String FORMAT_CODE = "ccw.ui.edit.text.clojure.format.code";
    public static final String EXPAND_SELECTION_UP = "ccw.ui.edit.text.clojure.expand.selection.up";
    public static final String EXPAND_SELECTION_LEFT = "ccw.ui.edit.text.clojure.expand.selection.left";
    public static final String EXPAND_SELECTION_RIGHT = "ccw.ui.edit.text.clojure.expand.selection.right";
    public static final String LAUNCH_REPL = "ccw.ui.edit.text.clojure.launch.repl";
    public static final String HIGHLIGHT_SAME_WORD = "ccw.ui.edit.text.clojure.highlight.same.word";
    public static final String RUN_TESTS = "ccw.ui.edit.text.clojure.run.tests";
    public static final String OPEN_DECLARATION = "ccw.ui.edit.text.clojure.open.declaration";
    public static final String RELOAD_CLOJURE = "ccw.ui.edit.text.clojure.reload";
}
