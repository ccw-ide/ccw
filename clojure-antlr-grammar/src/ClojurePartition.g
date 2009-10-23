grammar ClojurePartition;
options {output=template; rewrite=true;}

/*
 * Lexer part
 */

// TODO is this sufficient ?
PARTITION_STRING: '"' ( ~'"' | ('\\' '"') )* '"'
    ;

PARTITION_COMMENT:
        (';' ~('\r' | '\n')* '\r'? '\n')+ //{$channel=HIDDEN;}  //{skip();} // FIXME should use NEWLINE but NEWLINE has a problem I don't understand for the moment
    ;
    
PARTITION_CODE:
		~('"' | ';')+
	;
//UNDEFINED: 
  
//SPACE:  (' '|'\t'|','|'\r'|'\n')+ {$channel=HIDDEN;} // FIXME should use NEWLINE but NEWLINE has a problem I don't understand for the moment
//    ;
    
/*
 * Parser part
 */

file:   
        (PARTITION_STRING | PARTITION_COMMENT | PARTITION_CODE)*// | UNDEFINED)*
    ;

