lexer grammar Clojure;

T23 : '&' ;
T24 : '[' ;
T25 : ']' ;
T26 : '{' ;
T27 : '}' ;
T28 : '\'' ;
T29 : '^' ;
T30 : '@' ;
T31 : '#' ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 46
OPEN_PAREN: '('
 	;
 // $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 48
CLOSE_PAREN: ')' { }
	;
	
// TODO complete this list
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 52
SPECIAL_FORM: 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' |
            'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' |
            'new' | 'set!'
    ;


// TODO is this sufficient ?
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 59
STRING: '"' ( ~'"' | ('\\' '"') )* '"'
    ;

// TODO get the real definition from a java grammar.
// FIXME for the moment, allow just positive integers to start playing with the grammar
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 64
NUMBER: '-'? '0'..'9'+ ('.' '0'..'9'+)? (('e'|'E') '-'? '0'..'9'+)?
    ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 67
CHARACTER:
        '\\newline'
    |   '\\space'
    |   '\\tab'
    |   '\\' .   // TODO : is it correct to allow anything ?
    ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 74
NIL:    'nil'
    ;
    
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 77
BOOLEAN:
        'true'
    |   'false'
    ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 82
SYMBOL:
	'.'
    |   '/' // The division function FIXME is it necessary to hardcode this ?
    |   NAME ('/' NAME)?
    ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 88
fragment
NAME:   SYMBOL_HEAD SYMBOL_REST* (':' SYMBOL_REST+)*
    ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 92
fragment
SYMBOL_HEAD:   
        'a'..'z' | 'A'..'Z' | '*' | '+' | '!' | '-' | '_' | '?' | '>' | '<' | '=' | '$'
        // other characters will be allowed eventually, but not all macro characters have been determined
    ;
    
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 98
fragment
SYMBOL_REST:
        SYMBOL_HEAD
    |   '0'..'9' // Done this because a strange cannot find matchRange symbol occured when compiling the parser
    |   '.' // multiple successive points is allowed by the reader (but will break at evaluation)
    |   '#' // FIXME normally # is allowed only in syntax quote forms, in last position
    ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 115
KEYWORD:
        ':' SYMBOL
    ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 119
SYNTAX_QUOTE:
        '`'
    ;
    
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 123
UNQUOTE_SPLICING:
        '~@'
    ;
    
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 127
UNQUOTE:    
        '~'
    ;
    
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 131
COMMENT:
        ';' ~('\r' | '\n')* ('\r'? '\n')? {$channel=HIDDEN;}  //{skip();} // FIXME should use NEWLINE but NEWLINE has a problem I don't understand for the moment
    ;

// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 135
SPACE:  (' '|'\t'|','|'\r'|'\n')+ {$channel=HIDDEN;} // FIXME should use NEWLINE but NEWLINE has a problem I don't understand for the moment
    ;

// TODO how many
// $ANTLR src "o:/clojure/basic-clojure-grammar/src/Clojure.g" 139
LAMBDA_ARG:
        '%' '1'..'9' '0'..'9'*
    |   '%&'
    |   '%'
    ;
    
/*
 * Parser part
 */

/* This is really just an exercise for the moment. symbols can be more complex, no reader capability, ... */
// TODO allow spaces and newlines to be inserted anywhere
// TODO how to definitely make commas considered whitespace when not in comments or strings ?
// TODO answer the question : should I create multiple pass by first recognizing valid sexprs, and then 

