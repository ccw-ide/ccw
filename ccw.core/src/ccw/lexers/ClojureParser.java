package ccw.lexers;
// $ANTLR 3.0 /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g 2010-10-09 00:00:57

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ClojureParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "OPEN_PAREN", "CLOSE_PAREN", "AMPERSAND", "LEFT_SQUARE_BRACKET", "RIGHT_SQUARE_BRACKET", "LEFT_CURLY_BRACKET", "RIGHT_CURLY_BRACKET", "BACKSLASH", "CIRCUMFLEX", "COMMERCIAL_AT", "NUMBER_SIGN", "APOSTROPHE", "SPECIAL_FORM", "EscapeSequence", "STRING", "REGEX_LITERAL", "HEXDIGIT", "UnicodeEscape", "OctalEscape", "NUMBER", "CHARACTER", "NIL", "BOOLEAN", "NAME", "SYMBOL", "METADATA_TYPEHINT", "SYMBOL_HEAD", "SYMBOL_REST", "KEYWORD", "SYNTAX_QUOTE", "UNQUOTE_SPLICING", "UNQUOTE", "COMMENT", "SPACE", "LAMBDA_ARG"
    };
    public static final int SYNTAX_QUOTE=33;
    public static final int KEYWORD=32;
    public static final int SYMBOL=28;
    public static final int METADATA_TYPEHINT=29;
    public static final int SYMBOL_HEAD=30;
    public static final int NUMBER=23;
    public static final int AMPERSAND=6;
    public static final int OPEN_PAREN=4;
    public static final int COMMERCIAL_AT=13;
    public static final int SPACE=37;
    public static final int EOF=-1;
    public static final int CHARACTER=24;
    public static final int RIGHT_CURLY_BRACKET=10;
    public static final int LEFT_SQUARE_BRACKET=7;
    public static final int RIGHT_SQUARE_BRACKET=8;
    public static final int LEFT_CURLY_BRACKET=9;
    public static final int NAME=27;
    public static final int BOOLEAN=26;
    public static final int NIL=25;
    public static final int UNQUOTE=35;
    public static final int UnicodeEscape=21;
    public static final int LAMBDA_ARG=38;
    public static final int NUMBER_SIGN=14;
    public static final int SPECIAL_FORM=16;
    public static final int CLOSE_PAREN=5;
    public static final int SYMBOL_REST=31;
    public static final int APOSTROPHE=15;
    public static final int REGEX_LITERAL=19;
    public static final int COMMENT=36;
    public static final int OctalEscape=22;
    public static final int EscapeSequence=17;
    public static final int UNQUOTE_SPLICING=34;
    public static final int CIRCUMFLEX=12;
    public static final int STRING=18;
    public static final int HEXDIGIT=20;
    public static final int BACKSLASH=11;

        public ClojureParser(TokenStream input) {
            super(input);
        }
        

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "/home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g"; }

    
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



    // $ANTLR start literal
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:153:1: literal : ( STRING | NUMBER | CHARACTER | NIL | BOOLEAN | KEYWORD );
    public final void literal() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:154:9: ( STRING | NUMBER | CHARACTER | NIL | BOOLEAN | KEYWORD )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:
            {
            if ( input.LA(1)==STRING||(input.LA(1)>=NUMBER && input.LA(1)<=BOOLEAN)||input.LA(1)==KEYWORD ) {
                input.consume();
                errorRecovery=false;
            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_literal0);    throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end literal


    // $ANTLR start file
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:196:1: file : ( form )* ;
    public final void file() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:197:9: ( ( form )* )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:197:9: ( form )*
            {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:197:9: ( form )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==OPEN_PAREN||(LA1_0>=AMPERSAND && LA1_0<=LEFT_SQUARE_BRACKET)||LA1_0==LEFT_CURLY_BRACKET||(LA1_0>=CIRCUMFLEX && LA1_0<=SPECIAL_FORM)||(LA1_0>=STRING && LA1_0<=REGEX_LITERAL)||(LA1_0>=NUMBER && LA1_0<=BOOLEAN)||LA1_0==SYMBOL||(LA1_0>=KEYWORD && LA1_0<=COMMENT)||LA1_0==LAMBDA_ARG) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:197:11: form
            	    {
            	    pushFollow(FOLLOW_form_in_file1297);
            	    form();
            	    _fsp--;

            	     System.out.println("form found"); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end file


    // $ANTLR start form
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:201:1: form : ({...}? LAMBDA_ARG | literal | COMMENT | AMPERSAND | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set );
    public final void form() throws RecognitionException {
        Token s=null;

        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:202:3: ({...}? LAMBDA_ARG | literal | COMMENT | AMPERSAND | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set )
            int alt4=8;
            switch ( input.LA(1) ) {
            case LAMBDA_ARG:
                {
                alt4=1;
                }
                break;
            case STRING:
            case NUMBER:
            case CHARACTER:
            case NIL:
            case BOOLEAN:
            case KEYWORD:
                {
                alt4=2;
                }
                break;
            case COMMENT:
                {
                alt4=3;
                }
                break;
            case AMPERSAND:
                {
                alt4=4;
                }
                break;
            case NUMBER_SIGN:
                {
                switch ( input.LA(2) ) {
                case CIRCUMFLEX:
                    {
                    alt4=5;
                    }
                    break;
                case OPEN_PAREN:
                case APOSTROPHE:
                    {
                    alt4=7;
                    }
                    break;
                case LEFT_CURLY_BRACKET:
                    {
                    alt4=8;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("201:1: form : ({...}? LAMBDA_ARG | literal | COMMENT | AMPERSAND | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set );", 4, 5, input);

                    throw nvae;
                }

                }
                break;
            case OPEN_PAREN:
            case LEFT_SQUARE_BRACKET:
            case LEFT_CURLY_BRACKET:
            case SPECIAL_FORM:
            case SYMBOL:
                {
                alt4=5;
                }
                break;
            case CIRCUMFLEX:
            case COMMERCIAL_AT:
            case APOSTROPHE:
            case SYNTAX_QUOTE:
            case UNQUOTE_SPLICING:
            case UNQUOTE:
                {
                alt4=6;
                }
                break;
            case REGEX_LITERAL:
                {
                alt4=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("201:1: form : ({...}? LAMBDA_ARG | literal | COMMENT | AMPERSAND | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set );", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:202:3: {...}? LAMBDA_ARG
                    {
                    if ( !(this.inLambda) ) {
                        throw new FailedPredicateException(input, "form", "this.inLambda");
                    }
                    match(input,LAMBDA_ARG,FOLLOW_LAMBDA_ARG_in_form1330); 

                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:203:10: literal
                    {
                    pushFollow(FOLLOW_literal_in_form1341);
                    literal();
                    _fsp--;


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:205:7: COMMENT
                    {
                    match(input,COMMENT,FOLLOW_COMMENT_in_form1367); 

                    }
                    break;
                case 4 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:206:9: AMPERSAND
                    {
                    match(input,AMPERSAND,FOLLOW_AMPERSAND_in_form1377); 

                    }
                    break;
                case 5 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:207:9: ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map )
                    {
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:207:9: ( metadataForm )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==NUMBER_SIGN) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:207:9: metadataForm
                            {
                            pushFollow(FOLLOW_metadataForm_in_form1387);
                            metadataForm();
                            _fsp--;


                            }
                            break;

                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:207:23: ( SPECIAL_FORM | s= SYMBOL | list | vector | map )
                    int alt3=5;
                    switch ( input.LA(1) ) {
                    case SPECIAL_FORM:
                        {
                        alt3=1;
                        }
                        break;
                    case SYMBOL:
                        {
                        alt3=2;
                        }
                        break;
                    case OPEN_PAREN:
                        {
                        alt3=3;
                        }
                        break;
                    case LEFT_SQUARE_BRACKET:
                        {
                        alt3=4;
                        }
                        break;
                    case LEFT_CURLY_BRACKET:
                        {
                        alt3=5;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("207:23: ( SPECIAL_FORM | s= SYMBOL | list | vector | map )", 3, 0, input);

                        throw nvae;
                    }

                    switch (alt3) {
                        case 1 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:207:25: SPECIAL_FORM
                            {
                            match(input,SPECIAL_FORM,FOLLOW_SPECIAL_FORM_in_form1392); 

                            }
                            break;
                        case 2 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:207:40: s= SYMBOL
                            {
                            s=(Token)input.LT(1);
                            match(input,SYMBOL,FOLLOW_SYMBOL_in_form1398); 
                             symbols.add(s.getText()); 

                            }
                            break;
                        case 3 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:207:81: list
                            {
                            pushFollow(FOLLOW_list_in_form1404);
                            list();
                            _fsp--;


                            }
                            break;
                        case 4 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:207:88: vector
                            {
                            pushFollow(FOLLOW_vector_in_form1408);
                            vector();
                            _fsp--;


                            }
                            break;
                        case 5 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:207:97: map
                            {
                            pushFollow(FOLLOW_map_in_form1412);
                            map();
                            _fsp--;


                            }
                            break;

                    }


                    }
                    break;
                case 6 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:208:9: macroForm
                    {
                    pushFollow(FOLLOW_macroForm_in_form1424);
                    macroForm();
                    _fsp--;


                    }
                    break;
                case 7 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:209:9: dispatchMacroForm
                    {
                    pushFollow(FOLLOW_dispatchMacroForm_in_form1434);
                    dispatchMacroForm();
                    _fsp--;


                    }
                    break;
                case 8 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:210:9: set
                    {
                    pushFollow(FOLLOW_set_in_form1444);
                    set();
                    _fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end form


    // $ANTLR start macroForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:213:1: macroForm : ( quoteForm | metaForm | derefForm | syntaxQuoteForm | {...}? unquoteSplicingForm | {...}? unquoteForm );
    public final void macroForm() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:214:9: ( quoteForm | metaForm | derefForm | syntaxQuoteForm | {...}? unquoteSplicingForm | {...}? unquoteForm )
            int alt5=6;
            switch ( input.LA(1) ) {
            case APOSTROPHE:
                {
                alt5=1;
                }
                break;
            case CIRCUMFLEX:
                {
                alt5=2;
                }
                break;
            case COMMERCIAL_AT:
                {
                alt5=3;
                }
                break;
            case SYNTAX_QUOTE:
                {
                alt5=4;
                }
                break;
            case UNQUOTE_SPLICING:
                {
                alt5=5;
                }
                break;
            case UNQUOTE:
                {
                alt5=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("213:1: macroForm : ( quoteForm | metaForm | derefForm | syntaxQuoteForm | {...}? unquoteSplicingForm | {...}? unquoteForm );", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:214:9: quoteForm
                    {
                    pushFollow(FOLLOW_quoteForm_in_macroForm1475);
                    quoteForm();
                    _fsp--;


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:215:9: metaForm
                    {
                    pushFollow(FOLLOW_metaForm_in_macroForm1485);
                    metaForm();
                    _fsp--;


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:216:9: derefForm
                    {
                    pushFollow(FOLLOW_derefForm_in_macroForm1495);
                    derefForm();
                    _fsp--;


                    }
                    break;
                case 4 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:217:9: syntaxQuoteForm
                    {
                    pushFollow(FOLLOW_syntaxQuoteForm_in_macroForm1505);
                    syntaxQuoteForm();
                    _fsp--;


                    }
                    break;
                case 5 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:218:7: {...}? unquoteSplicingForm
                    {
                    if ( !( this.syntaxQuoteDepth > 0 ) ) {
                        throw new FailedPredicateException(input, "macroForm", " this.syntaxQuoteDepth > 0 ");
                    }
                    pushFollow(FOLLOW_unquoteSplicingForm_in_macroForm1515);
                    unquoteSplicingForm();
                    _fsp--;


                    }
                    break;
                case 6 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:219:7: {...}? unquoteForm
                    {
                    if ( !( this.syntaxQuoteDepth > 0 ) ) {
                        throw new FailedPredicateException(input, "macroForm", " this.syntaxQuoteDepth > 0 ");
                    }
                    pushFollow(FOLLOW_unquoteForm_in_macroForm1525);
                    unquoteForm();
                    _fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end macroForm


    // $ANTLR start dispatchMacroForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:222:1: dispatchMacroForm : ( REGEX_LITERAL | varQuoteForm | {...}? lambdaForm );
    public final void dispatchMacroForm() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:223:9: ( REGEX_LITERAL | varQuoteForm | {...}? lambdaForm )
            int alt6=3;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==REGEX_LITERAL) ) {
                alt6=1;
            }
            else if ( (LA6_0==NUMBER_SIGN) ) {
                int LA6_2 = input.LA(2);

                if ( (LA6_2==APOSTROPHE) ) {
                    alt6=2;
                }
                else if ( (LA6_2==OPEN_PAREN) ) {
                    alt6=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("222:1: dispatchMacroForm : ( REGEX_LITERAL | varQuoteForm | {...}? lambdaForm );", 6, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("222:1: dispatchMacroForm : ( REGEX_LITERAL | varQuoteForm | {...}? lambdaForm );", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:223:9: REGEX_LITERAL
                    {
                    match(input,REGEX_LITERAL,FOLLOW_REGEX_LITERAL_in_dispatchMacroForm1552); 

                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:224:9: varQuoteForm
                    {
                    pushFollow(FOLLOW_varQuoteForm_in_dispatchMacroForm1562);
                    varQuoteForm();
                    _fsp--;


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:225:9: {...}? lambdaForm
                    {
                    if ( !(!this.inLambda) ) {
                        throw new FailedPredicateException(input, "dispatchMacroForm", "!this.inLambda");
                    }
                    pushFollow(FOLLOW_lambdaForm_in_dispatchMacroForm1574);
                    lambdaForm();
                    _fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end dispatchMacroForm


    // $ANTLR start list
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:228:1: list : o= OPEN_PAREN ( form )* c= CLOSE_PAREN ;
    public final void list() throws RecognitionException {
        Token o=null;
        Token c=null;

        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:228:9: (o= OPEN_PAREN ( form )* c= CLOSE_PAREN )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:228:9: o= OPEN_PAREN ( form )* c= CLOSE_PAREN
            {
            o=(Token)input.LT(1);
            match(input,OPEN_PAREN,FOLLOW_OPEN_PAREN_in_list1595); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:228:22: ( form )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==OPEN_PAREN||(LA7_0>=AMPERSAND && LA7_0<=LEFT_SQUARE_BRACKET)||LA7_0==LEFT_CURLY_BRACKET||(LA7_0>=CIRCUMFLEX && LA7_0<=SPECIAL_FORM)||(LA7_0>=STRING && LA7_0<=REGEX_LITERAL)||(LA7_0>=NUMBER && LA7_0<=BOOLEAN)||LA7_0==SYMBOL||(LA7_0>=KEYWORD && LA7_0<=COMMENT)||LA7_0==LAMBDA_ARG) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:228:22: form
            	    {
            	    pushFollow(FOLLOW_form_in_list1597);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            c=(Token)input.LT(1);
            match(input,CLOSE_PAREN,FOLLOW_CLOSE_PAREN_in_list1603); 
             parensMatching.put(Integer.valueOf(o.getTokenIndex()), Integer.valueOf(c.getTokenIndex())); parensMatching.put(Integer.valueOf(c.getTokenIndex()), Integer.valueOf(o.getTokenIndex())); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end list


    // $ANTLR start vector
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:231:1: vector : LEFT_SQUARE_BRACKET ( form )* RIGHT_SQUARE_BRACKET ;
    public final void vector() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:231:10: ( LEFT_SQUARE_BRACKET ( form )* RIGHT_SQUARE_BRACKET )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:231:10: LEFT_SQUARE_BRACKET ( form )* RIGHT_SQUARE_BRACKET
            {
            match(input,LEFT_SQUARE_BRACKET,FOLLOW_LEFT_SQUARE_BRACKET_in_vector1622); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:231:30: ( form )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==OPEN_PAREN||(LA8_0>=AMPERSAND && LA8_0<=LEFT_SQUARE_BRACKET)||LA8_0==LEFT_CURLY_BRACKET||(LA8_0>=CIRCUMFLEX && LA8_0<=SPECIAL_FORM)||(LA8_0>=STRING && LA8_0<=REGEX_LITERAL)||(LA8_0>=NUMBER && LA8_0<=BOOLEAN)||LA8_0==SYMBOL||(LA8_0>=KEYWORD && LA8_0<=COMMENT)||LA8_0==LAMBDA_ARG) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:231:30: form
            	    {
            	    pushFollow(FOLLOW_form_in_vector1624);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            match(input,RIGHT_SQUARE_BRACKET,FOLLOW_RIGHT_SQUARE_BRACKET_in_vector1627); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end vector


    // $ANTLR start map
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:234:1: map : LEFT_CURLY_BRACKET ( form form )* RIGHT_CURLY_BRACKET ;
    public final void map() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:234:9: ( LEFT_CURLY_BRACKET ( form form )* RIGHT_CURLY_BRACKET )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:234:9: LEFT_CURLY_BRACKET ( form form )* RIGHT_CURLY_BRACKET
            {
            match(input,LEFT_CURLY_BRACKET,FOLLOW_LEFT_CURLY_BRACKET_in_map1646); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:234:28: ( form form )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==OPEN_PAREN||(LA9_0>=AMPERSAND && LA9_0<=LEFT_SQUARE_BRACKET)||LA9_0==LEFT_CURLY_BRACKET||(LA9_0>=CIRCUMFLEX && LA9_0<=SPECIAL_FORM)||(LA9_0>=STRING && LA9_0<=REGEX_LITERAL)||(LA9_0>=NUMBER && LA9_0<=BOOLEAN)||LA9_0==SYMBOL||(LA9_0>=KEYWORD && LA9_0<=COMMENT)||LA9_0==LAMBDA_ARG) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:234:29: form form
            	    {
            	    pushFollow(FOLLOW_form_in_map1649);
            	    form();
            	    _fsp--;

            	    pushFollow(FOLLOW_form_in_map1651);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            match(input,RIGHT_CURLY_BRACKET,FOLLOW_RIGHT_CURLY_BRACKET_in_map1655); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end map


    // $ANTLR start quoteForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:237:1: quoteForm : APOSTROPHE form ;
    public final void quoteForm() throws RecognitionException {
         this.syntaxQuoteDepth++; 
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:240:8: ( APOSTROPHE form )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:240:8: APOSTROPHE form
            {
            match(input,APOSTROPHE,FOLLOW_APOSTROPHE_in_quoteForm1688); 
            pushFollow(FOLLOW_form_in_quoteForm1690);
            form();
            _fsp--;


            }

             this.syntaxQuoteDepth--; 
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end quoteForm


    // $ANTLR start metaForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:243:1: metaForm : CIRCUMFLEX form ;
    public final void metaForm() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:243:13: ( CIRCUMFLEX form )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:243:13: CIRCUMFLEX form
            {
            match(input,CIRCUMFLEX,FOLLOW_CIRCUMFLEX_in_metaForm1704); 
            pushFollow(FOLLOW_form_in_metaForm1706);
            form();
            _fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end metaForm


    // $ANTLR start derefForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:246:1: derefForm : COMMERCIAL_AT form ;
    public final void derefForm() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:246:13: ( COMMERCIAL_AT form )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:246:13: COMMERCIAL_AT form
            {
            match(input,COMMERCIAL_AT,FOLLOW_COMMERCIAL_AT_in_derefForm1723); 
            pushFollow(FOLLOW_form_in_derefForm1725);
            form();
            _fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end derefForm


    // $ANTLR start syntaxQuoteForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:249:1: syntaxQuoteForm : SYNTAX_QUOTE form ;
    public final void syntaxQuoteForm() throws RecognitionException {
         this.syntaxQuoteDepth++; 
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:253:9: ( SYNTAX_QUOTE form )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:253:9: SYNTAX_QUOTE form
            {
            match(input,SYNTAX_QUOTE,FOLLOW_SYNTAX_QUOTE_in_syntaxQuoteForm1765); 
            pushFollow(FOLLOW_form_in_syntaxQuoteForm1767);
            form();
            _fsp--;


            }

             this.syntaxQuoteDepth--; 
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end syntaxQuoteForm


    // $ANTLR start unquoteForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:256:1: unquoteForm : UNQUOTE form ;
    public final void unquoteForm() throws RecognitionException {
         this.syntaxQuoteDepth--; 
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:260:9: ( UNQUOTE form )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:260:9: UNQUOTE form
            {
            match(input,UNQUOTE,FOLLOW_UNQUOTE_in_unquoteForm1807); 
            pushFollow(FOLLOW_form_in_unquoteForm1809);
            form();
            _fsp--;


            }

             this.syntaxQuoteDepth++; 
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end unquoteForm


    // $ANTLR start unquoteSplicingForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:263:1: unquoteSplicingForm : UNQUOTE_SPLICING form ;
    public final void unquoteSplicingForm() throws RecognitionException {
         this.syntaxQuoteDepth--; 
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:267:9: ( UNQUOTE_SPLICING form )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:267:9: UNQUOTE_SPLICING form
            {
            match(input,UNQUOTE_SPLICING,FOLLOW_UNQUOTE_SPLICING_in_unquoteSplicingForm1849); 
            pushFollow(FOLLOW_form_in_unquoteSplicingForm1851);
            form();
            _fsp--;


            }

             this.syntaxQuoteDepth++; 
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end unquoteSplicingForm


    // $ANTLR start set
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:270:1: set : NUMBER_SIGN LEFT_CURLY_BRACKET ( form )* RIGHT_CURLY_BRACKET ;
    public final void set() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:270:9: ( NUMBER_SIGN LEFT_CURLY_BRACKET ( form )* RIGHT_CURLY_BRACKET )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:270:9: NUMBER_SIGN LEFT_CURLY_BRACKET ( form )* RIGHT_CURLY_BRACKET
            {
            match(input,NUMBER_SIGN,FOLLOW_NUMBER_SIGN_in_set1870); 
            match(input,LEFT_CURLY_BRACKET,FOLLOW_LEFT_CURLY_BRACKET_in_set1872); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:270:40: ( form )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==OPEN_PAREN||(LA10_0>=AMPERSAND && LA10_0<=LEFT_SQUARE_BRACKET)||LA10_0==LEFT_CURLY_BRACKET||(LA10_0>=CIRCUMFLEX && LA10_0<=SPECIAL_FORM)||(LA10_0>=STRING && LA10_0<=REGEX_LITERAL)||(LA10_0>=NUMBER && LA10_0<=BOOLEAN)||LA10_0==SYMBOL||(LA10_0>=KEYWORD && LA10_0<=COMMENT)||LA10_0==LAMBDA_ARG) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:270:40: form
            	    {
            	    pushFollow(FOLLOW_form_in_set1874);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match(input,RIGHT_CURLY_BRACKET,FOLLOW_RIGHT_CURLY_BRACKET_in_set1877); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end set


    // $ANTLR start metadataForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:273:1: metadataForm : NUMBER_SIGN CIRCUMFLEX ( map | SYMBOL | KEYWORD | STRING ) ;
    public final void metadataForm() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:274:9: ( NUMBER_SIGN CIRCUMFLEX ( map | SYMBOL | KEYWORD | STRING ) )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:274:9: NUMBER_SIGN CIRCUMFLEX ( map | SYMBOL | KEYWORD | STRING )
            {
            match(input,NUMBER_SIGN,FOLLOW_NUMBER_SIGN_in_metadataForm1897); 
            match(input,CIRCUMFLEX,FOLLOW_CIRCUMFLEX_in_metadataForm1899); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:274:32: ( map | SYMBOL | KEYWORD | STRING )
            int alt11=4;
            switch ( input.LA(1) ) {
            case LEFT_CURLY_BRACKET:
                {
                alt11=1;
                }
                break;
            case SYMBOL:
                {
                alt11=2;
                }
                break;
            case KEYWORD:
                {
                alt11=3;
                }
                break;
            case STRING:
                {
                alt11=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("274:32: ( map | SYMBOL | KEYWORD | STRING )", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:274:33: map
                    {
                    pushFollow(FOLLOW_map_in_metadataForm1902);
                    map();
                    _fsp--;


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:274:39: SYMBOL
                    {
                    match(input,SYMBOL,FOLLOW_SYMBOL_in_metadataForm1906); 

                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:274:46: KEYWORD
                    {
                    match(input,KEYWORD,FOLLOW_KEYWORD_in_metadataForm1908); 

                    }
                    break;
                case 4 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:274:54: STRING
                    {
                    match(input,STRING,FOLLOW_STRING_in_metadataForm1910); 

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end metadataForm


    // $ANTLR start varQuoteForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:277:1: varQuoteForm : NUMBER_SIGN APOSTROPHE form ;
    public final void varQuoteForm() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:278:9: ( NUMBER_SIGN APOSTROPHE form )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:278:9: NUMBER_SIGN APOSTROPHE form
            {
            match(input,NUMBER_SIGN,FOLLOW_NUMBER_SIGN_in_varQuoteForm1931); 
            match(input,APOSTROPHE,FOLLOW_APOSTROPHE_in_varQuoteForm1933); 
            pushFollow(FOLLOW_form_in_varQuoteForm1935);
            form();
            _fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end varQuoteForm


    // $ANTLR start lambdaForm
    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:281:1: lambdaForm : NUMBER_SIGN list ;
    public final void lambdaForm() throws RecognitionException {
        
        this.inLambda = true;

        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:288:7: ( NUMBER_SIGN list )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:288:7: NUMBER_SIGN list
            {
            match(input,NUMBER_SIGN,FOLLOW_NUMBER_SIGN_in_lambdaForm1962); 
            pushFollow(FOLLOW_list_in_lambdaForm1964);
            list();
            _fsp--;


            }

            
            this.inLambda = false;

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end lambdaForm


 

    public static final BitSet FOLLOW_set_in_literal0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_form_in_file1297 = new BitSet(new long[]{0x0000005F178DF2D2L});
    public static final BitSet FOLLOW_LAMBDA_ARG_in_form1330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_form1341 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMENT_in_form1367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AMPERSAND_in_form1377 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metadataForm_in_form1387 = new BitSet(new long[]{0x0000000010010290L});
    public static final BitSet FOLLOW_SPECIAL_FORM_in_form1392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYMBOL_in_form1398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_list_in_form1404 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vector_in_form1408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_map_in_form1412 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macroForm_in_form1424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dispatchMacroForm_in_form1434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_form1444 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quoteForm_in_macroForm1475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metaForm_in_macroForm1485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derefForm_in_macroForm1495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_syntaxQuoteForm_in_macroForm1505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unquoteSplicingForm_in_macroForm1515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unquoteForm_in_macroForm1525 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REGEX_LITERAL_in_dispatchMacroForm1552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varQuoteForm_in_dispatchMacroForm1562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lambdaForm_in_dispatchMacroForm1574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OPEN_PAREN_in_list1595 = new BitSet(new long[]{0x0000005F178DF2F0L});
    public static final BitSet FOLLOW_form_in_list1597 = new BitSet(new long[]{0x0000005F178DF2F0L});
    public static final BitSet FOLLOW_CLOSE_PAREN_in_list1603 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_SQUARE_BRACKET_in_vector1622 = new BitSet(new long[]{0x0000005F178DF3D0L});
    public static final BitSet FOLLOW_form_in_vector1624 = new BitSet(new long[]{0x0000005F178DF3D0L});
    public static final BitSet FOLLOW_RIGHT_SQUARE_BRACKET_in_vector1627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_CURLY_BRACKET_in_map1646 = new BitSet(new long[]{0x0000005F178DF6D0L});
    public static final BitSet FOLLOW_form_in_map1649 = new BitSet(new long[]{0x0000005F178DF2D0L});
    public static final BitSet FOLLOW_form_in_map1651 = new BitSet(new long[]{0x0000005F178DF6D0L});
    public static final BitSet FOLLOW_RIGHT_CURLY_BRACKET_in_map1655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_APOSTROPHE_in_quoteForm1688 = new BitSet(new long[]{0x0000005F178DF2D0L});
    public static final BitSet FOLLOW_form_in_quoteForm1690 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CIRCUMFLEX_in_metaForm1704 = new BitSet(new long[]{0x0000005F178DF2D0L});
    public static final BitSet FOLLOW_form_in_metaForm1706 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMERCIAL_AT_in_derefForm1723 = new BitSet(new long[]{0x0000005F178DF2D0L});
    public static final BitSet FOLLOW_form_in_derefForm1725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYNTAX_QUOTE_in_syntaxQuoteForm1765 = new BitSet(new long[]{0x0000005F178DF2D0L});
    public static final BitSet FOLLOW_form_in_syntaxQuoteForm1767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNQUOTE_in_unquoteForm1807 = new BitSet(new long[]{0x0000005F178DF2D0L});
    public static final BitSet FOLLOW_form_in_unquoteForm1809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNQUOTE_SPLICING_in_unquoteSplicingForm1849 = new BitSet(new long[]{0x0000005F178DF2D0L});
    public static final BitSet FOLLOW_form_in_unquoteSplicingForm1851 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_SIGN_in_set1870 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_LEFT_CURLY_BRACKET_in_set1872 = new BitSet(new long[]{0x0000005F178DF6D0L});
    public static final BitSet FOLLOW_form_in_set1874 = new BitSet(new long[]{0x0000005F178DF6D0L});
    public static final BitSet FOLLOW_RIGHT_CURLY_BRACKET_in_set1877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_SIGN_in_metadataForm1897 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_CIRCUMFLEX_in_metadataForm1899 = new BitSet(new long[]{0x0000000110040200L});
    public static final BitSet FOLLOW_map_in_metadataForm1902 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYMBOL_in_metadataForm1906 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KEYWORD_in_metadataForm1908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_metadataForm1910 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_SIGN_in_varQuoteForm1931 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_APOSTROPHE_in_varQuoteForm1933 = new BitSet(new long[]{0x0000005F178DF2D0L});
    public static final BitSet FOLLOW_form_in_varQuoteForm1935 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_SIGN_in_lambdaForm1962 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_list_in_lambdaForm1964 = new BitSet(new long[]{0x0000000000000002L});

}