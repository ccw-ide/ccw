/*
 * ScalaColorConstants.java
 * 
 * Created on 04.11.2004
 *
 * Status: done
 */
package clojuredev.editors;

/**
 * @author Marc Moser
 *
 * This defines constants to help identify colors and styles.
 */
public interface ClojureColorConstants {
		public static final String CLOJURE_COLOR_PREFIX 			= "color_clojure_";
		public static final String CLOJURE_ISBOLD_APPENDIX	 	= "_bold";
		public static final String CLOJURE_ISITALICS_APPENDIX	 	= "_italics";
		public static final String CLOJURE_ISUNDERLINE_APPENDIX	 	= "_underline";
		public static final String CLOJURE_ISSTRIKETHROUGH_APPENDIX	 	= "_strikethrough";
		
		public static final String[] APPENDIX = {
			CLOJURE_ISBOLD_APPENDIX, CLOJURE_ISITALICS_APPENDIX, CLOJURE_ISUNDERLINE_APPENDIX, CLOJURE_ISSTRIKETHROUGH_APPENDIX
		};
		
		public static final String CLOJURE_DEFAULT 				= "color_clojure_default";
		
		public static final String CLOJURE_KEYWORD 				= "color_clojure_keyword";
		public static final String CLOJURE_STRING 				= "color_clojure_string";
		public static final String CLOJURE_XML	 				= "color_clojure_xml";
		
		public static final String CLOJURE_VAR	 			= "color_clojure_var";
		public static final String CLOJURE_VAL	 			= "color_clojure_val";
		public static final String CLOJURE_DEF	 			= "color_clojure_def";
		public static final String CLOJURE_ARG	 			= "color_clojure_arg";

		public static final String CLOJURE_CLASS	 	 = "color_clojure_class";
		public static final String CLOJURE_OBJECT	 	 = "color_clojure_object";
		public static final String CLOJURE_INTERFACE = "color_clojure_interface";
		public static final String CLOJURE_TPARAM    = "color_clojure_type_parameter";
		
		
		public static final String CLOJURE_NUMBER				= "color_clojure_number";
		public static final String CLOJURE_PATTERN				= "color_clojure_pattern";

		public static final String CLOJURE_MULTI_LINE_COMMENT	= "color_clojure_multiline_comment";
		public static final String CLOJURE_SINGLE_LINE_COMMENT	= "color_clojure_singleline_comment";
		public static final String CLOJURE_DOC_COMMENT			= "color_clojure_doc_comment";
		
		public static final String CLOJURE_EDITOR_BACKGROUND_DEFAULT	= "editor_background_default_color";
		public static final String CLOJURE_EDITOR_BACKGROUND_COLOR	= "editor_background_custom_color";
}
