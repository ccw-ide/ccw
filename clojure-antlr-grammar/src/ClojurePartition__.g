lexer grammar ClojurePartition;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/ClojurePartition.g" 9
PARTITION_STRING: '"' ( ~'"' | ('\\' '"') )* '"'
    ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/ClojurePartition.g" 12
PARTITION_COMMENT:
        (';' ~('\r' | '\n')* '\r'? '\n')+ //{$channel=HIDDEN;}  //{skip();} // FIXME should use NEWLINE but NEWLINE has a problem I don't understand for the moment
    ;
    
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/ClojurePartition.g" 16
PARTITION_CODE:
		~('"' | ';')+
	;
//UNDEFINED: 
  
//SPACE:  (' '|'\t'|','|'\r'|'\n')+ {$channel=HIDDEN;} // FIXME should use NEWLINE but NEWLINE has a problem I don't understand for the moment
//    ;
    
/*
 * Parser part
 */

