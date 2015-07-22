package ccw.core;

public class MenuLabels {

    public static final String EDITING_CLOJURE_SOURCE = "Editing Clojure source code";
    public static final String GOTO_MATHCING_BRACKET = "Go to Matching Bracket";

    public static final String EXPAND_SELECTION_ENCLOSING = "Expand selection to include enclosing element";
    public static final String EXPAND_SELECTION_LEFT = "Expand selection to include element on the left";
    public static final String EXPAND_SELECTION_RIGHT = "Expand selection to include element on the right";
    public static final String RESTORE_LAST_SELECTION = "Restore Last Selection";
    public static final String FORWARD_SLURP = "Forward Slurp";
    public static final String BACKWARD_SLURP = "Backward Slurp";
    public static final String FORWARD_BARF = "Forward Barf";
    public static final String BACKWARD_BARF = "Backward Barf";

    public static final String RAISE_SELECTION = "Raise Selection";
    public static final String SPLICE_SEXP = "Splice S-expression";
    
    public static final String INDENT_LINE="Indent the current line";
    public static final String SPLIT_SEXP="Split the S-expression";
    public static final String JOIN_SEXP="Join the S-expressions";

    public static final String SWITCH_EDIT_MODE = "Switch Edit mode (unrestricted <-> strict/paredit)";
    public static final String SHOW_RAINBOW_PARENS="Show/hide rainbow parens";

    public static final String GOTO_NEXT_MEMBER = "Go to next member";
    public static final String GOTO_NEXT_PREVIOUS = "Go to previous member";
    
    public static final String SELECT_TOP_SEXP = "Select Top level S-Expression";
    public static final String EVALUATE_CURRENT_SELECTION = "Evaluate current selection or Top Level S-Expression";

    public static final String CLOJURE_SHORTCUT_RUN = "Clojure shortcut run";
    
    public static final String LOAD_FILE_IN_REPL="Load file in Clojure REPL";
    public static final String COMPILE_FILE_IN_REPL="Compile file in Clojure REPL";
    
    public static final String SWITCH_FILE_NAMESPACE="Switch REPL to File's Namespace";

    public static final String RUN_CLOJURE_JVM_FOR_FILE="Run Clojure new JVM for current file";
    public static final String OPEN_SYMBOL_DECLARATION="Open symbol declaration";

    public static final String RUN_TEST_IN_JVM="Run tests in active Clojure JVM";
    public static final String FORMAT_SOURCE_FILE="Format Clojure source file";
    public static final String CONNECT_TO_REPL="Connect to REPL";

    // Test
    public static final String TEST="Test";
    public static final String TEST_GENERATOR="Generator...";

    // Leiningen
    public static final String LEININGEN = "Leiningen";
    public static final String UPDATE_DEPENDENCIES = "Update dependencies";
    public static final String GENERIC_COMMAND_LINE = "Generic Leiningen Command Line";
    public static final String CONVERT_TO_LEIN_PROJECT = "Convert to Leiningen Project";
    public static final String RESET_PROJECT_CONFIGURATION = "Reset project configuration";
    public static final String REMOVE_LEIN_NATURE = "Remove Leiningen Support";
    public static final String LAUNCH_HEADLESS_REPL = "Launch Headless REPL for the project";
}
