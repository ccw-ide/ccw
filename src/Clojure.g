/* reader form clojure page errors
 * symbols can contain chars < >  = $ ($ is mandatory for member class resolving, and is used in boot.clj) in the implementation (and in fact functions < and > are defined in clojure core !!)
 * Metadata must be Symbol,Keyword,String or Map : add this precision to the documentation ?
 */
grammar Clojure;
/*
options {
// TODO : try to refactor the grammar to get rid of backtrack=true or to minimize the backtracking
backtrack=true;
rewrite=true;
}
*/
//options {output=template; rewrite=true;}

@members {
boolean inLambda=false;
int syntaxQuoteDepth = 0;

java.util.List symbols = new java.util.ArrayList();
public List getCollectedSymbols() { return symbols; }
// TODO envisage to remove this when the grammar is fully tested ?
//public void recover(IntStream input, RecognitionException re) {
//	throw new RuntimeException("Not recovering from RecognitionException, na!", re);
//}
//}

//@lexer::members {
java.util.Map parensMatching = new java.util.HashMap(); 
public Integer matchingParenForPosition(Integer position) {
  return (Integer) parensMatching.get(position);
}
public void clearParensMatching() { parensMatching.clear(); }
}

/*
 * Lexer part
 */
 
OPEN_PAREN: '('
 	;
CLOSE_PAREN: ')'
	;
AMPERSAND: '&'
        ;
LEFT_SQUARE_BRACKET: '['
        ;
RIGHT_SQUARE_BRACKET: ']'
        ;
LEFT_CURLY_BRACKET: '{'
        ; 
RIGHT_CURLY_BRACKET: '}'
        ;
BACKSLASH: '\\'
        ;
CIRCUMFLEX: '^'
        ;
COMMERCIAL_AT: '@'
        ;
NUMBER_SIGN: '#'
        ;
APOSTROPHE: '\''
        ;
        
// TODO complete this list
SPECIAL_FORM: 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' |
            'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' |
            'new' | 'set!' | '.'
    ;

// TODO is this sufficient ?
STRING: '"' ( ~'"' | (BACKSLASH '"') )* '"'
    ;

// TODO get the real definition from a java grammar.
// FIXME for the moment, allow just positive integers to start playing with the grammar
NUMBER: '-'? '0'..'9'+ ('.' '0'..'9'+)? (('e'|'E') '-'? '0'..'9'+)?
    ;

CHARACTER:
        '\\newline'
    |   '\\space'
    |   '\\tab'
    |   '\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
    |   BACKSLASH .   // TODO : is it correct to allow anything ?
    ;

HEXDIGIT:
        '0'..'9' | 'a'..'f' | 'A'..'F';
        
NIL:    'nil'
    ;
    
BOOLEAN:
        'true'
    |   'false'
    ;

SYMBOL:
        '/' // The division function FIXME is it necessary to hardcode this ?
    |   NAME ('/' NAME)?
    ;

METADATA_TYPEHINT:
		NUMBER_SIGN CIRCUMFLEX NAME
	;
	
fragment
NAME:   SYMBOL_HEAD SYMBOL_REST* (':' SYMBOL_REST+)*
    ;

fragment
SYMBOL_HEAD:   
        'a'..'z' | 'A'..'Z' | '*' | '+' | '!' | '-' | '_' | '?' | '>' | '<' | '=' | '$'
        // other characters will be allowed eventually, but not all macro characters have been determined
    ;

fragment
SYMBOL_REST:
        SYMBOL_HEAD
    |   '0'..'9' // Done this because a strange cannot find matchRange symbol occured when compiling the parser
    |   '.' // multiple successive points is allowed by the reader (but will break at evaluation)
    |   NUMBER_SIGN // FIXME normally # is allowed only in syntax quote forms, in last position
    ;

literal:
        STRING //-> template(it={$STRING.text}) "<span style='color: red ; '>$it$</span>" 
    |   NUMBER
    |   CHARACTER
    |   NIL
    |   BOOLEAN
    |   KEYWORD
    ;    

KEYWORD:
        ':' SYMBOL
    ;

SYNTAX_QUOTE:
        '`'
    ;
    
UNQUOTE_SPLICING:
        '~@'
    ;
    
UNQUOTE:    
        '~'
    ;
    
COMMENT:
        ';' ~('\r' | '\n')* ('\r'? '\n')? {$channel=HIDDEN;}  //{skip();} // FIXME should use NEWLINE but NEWLINE has a problem I don't understand for the moment
    ;

SPACE:  (' '|'\t'|','|'\r'|'\n')+ {$channel=HIDDEN;} // FIXME should use NEWLINE but NEWLINE has a problem I don't understand for the moment
    ;

// TODO how many
LAMBDA_ARG:
        '%' '1'..'9' '0'..'9'*
    |   '%&'
    |   '%'
    ;
    
/*
 * Parser part
 */

file:   
        ( form  { System.out.println("form found"); }  )*
    ;
    
// Note : dispatch macros are hardwired in clojure
form	:	   
	 {this.inLambda}? LAMBDA_ARG
    |    literal // Place literal first to make nil and booleans take precedence over symbol (impossible to 
                // name a symbol nil, true or false)
    |	COMMENT
    |   AMPERSAND
    |   metadataForm? ( SPECIAL_FORM | s=SYMBOL { symbols.add(s.getText()); } | list | vector | map )
    |   macroForm
    |   dispatchMacroForm
    |   set
    ;
        
macroForm:   
        quoteForm
    |   metaForm
    |   derefForm
    |   syntaxQuoteForm
    |	{ this.syntaxQuoteDepth > 0 }? unquoteSplicingForm
    |	{ this.syntaxQuoteDepth > 0 }? unquoteForm
    ;
    
dispatchMacroForm:   
        regexForm
    |   varQuoteForm
    |   {!this.inLambda}? lambdaForm // contraction for anonymousFunction
    ;
    
list:   o=OPEN_PAREN form * c=CLOSE_PAREN { parensMatching.put(Integer.valueOf(o.getTokenIndex()), Integer.valueOf(c.getTokenIndex())); parensMatching.put(Integer.valueOf(c.getTokenIndex()), Integer.valueOf(o.getTokenIndex())); }
    ;
    
vector:  LEFT_SQUARE_BRACKET form* RIGHT_SQUARE_BRACKET
    ;
    
map:    LEFT_CURLY_BRACKET (form form)* RIGHT_CURLY_BRACKET
    ;
    
quoteForm
@init  { this.syntaxQuoteDepth++; }
@after { this.syntaxQuoteDepth--; }
    :  APOSTROPHE form
    ;

metaForm:   CIRCUMFLEX form
    ;
    
derefForm:  COMMERCIAL_AT form
    ;
    
syntaxQuoteForm
@init  { this.syntaxQuoteDepth++; }
@after { this.syntaxQuoteDepth--; }
    :
        SYNTAX_QUOTE form
    ;
    
unquoteForm
@init  { this.syntaxQuoteDepth--; }
@after { this.syntaxQuoteDepth++; }
    :
        UNQUOTE form
    ;
    
unquoteSplicingForm
@init  { this.syntaxQuoteDepth--; }
@after { this.syntaxQuoteDepth++; }
    :
        UNQUOTE_SPLICING form
    ;
    
set:    NUMBER_SIGN LEFT_CURLY_BRACKET form* RIGHT_CURLY_BRACKET
    ;

regexForm:  NUMBER_SIGN STRING
    ;
    
metadataForm:
        NUMBER_SIGN CIRCUMFLEX (map | SYMBOL|KEYWORD|STRING)
    ;

varQuoteForm:
        NUMBER_SIGN APOSTROPHE form
    ;

lambdaForm
@init {
this.inLambda = true;
}
@after {
this.inLambda = false;
}
    : NUMBER_SIGN list
    ;
