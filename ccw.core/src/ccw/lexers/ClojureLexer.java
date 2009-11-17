package ccw.lexers;
// $ANTLR 3.0 /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g 2009-11-17 21:25:22

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;

public class ClojureLexer extends Lexer {
    public static final int SYNTAX_QUOTE=32;
    public static final int KEYWORD=31;
    public static final int SYMBOL=27;
    public static final int METADATA_TYPEHINT=28;
    public static final int SYMBOL_HEAD=29;
    public static final int NUMBER=22;
    public static final int AMPERSAND=6;
    public static final int OPEN_PAREN=4;
    public static final int COMMERCIAL_AT=13;
    public static final int Tokens=38;
    public static final int EOF=-1;
    public static final int SPACE=36;
    public static final int CHARACTER=23;
    public static final int RIGHT_CURLY_BRACKET=10;
    public static final int LEFT_SQUARE_BRACKET=7;
    public static final int RIGHT_SQUARE_BRACKET=8;
    public static final int LEFT_CURLY_BRACKET=9;
    public static final int NAME=26;
    public static final int BOOLEAN=25;
    public static final int NIL=24;
    public static final int UNQUOTE=34;
    public static final int UnicodeEscape=19;
    public static final int LAMBDA_ARG=37;
    public static final int NUMBER_SIGN=14;
    public static final int SPECIAL_FORM=16;
    public static final int CLOSE_PAREN=5;
    public static final int APOSTROPHE=15;
    public static final int SYMBOL_REST=30;
    public static final int COMMENT=35;
    public static final int EscapeSequence=17;
    public static final int OctalEscape=20;
    public static final int CIRCUMFLEX=12;
    public static final int UNQUOTE_SPLICING=33;
    public static final int STRING=18;
    public static final int BACKSLASH=11;
    public static final int HEXDIGIT=21;
    public ClojureLexer() {;} 
    public ClojureLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "/home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g"; }

    // $ANTLR start OPEN_PAREN
    public final void mOPEN_PAREN() throws RecognitionException {
        try {
            int _type = OPEN_PAREN;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:39:13: ( '(' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:39:13: '('
            {
            match('('); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OPEN_PAREN

    // $ANTLR start CLOSE_PAREN
    public final void mCLOSE_PAREN() throws RecognitionException {
        try {
            int _type = CLOSE_PAREN;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:41:14: ( ')' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:41:14: ')'
            {
            match(')'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CLOSE_PAREN

    // $ANTLR start AMPERSAND
    public final void mAMPERSAND() throws RecognitionException {
        try {
            int _type = AMPERSAND;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:43:12: ( '&' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:43:12: '&'
            {
            match('&'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end AMPERSAND

    // $ANTLR start LEFT_SQUARE_BRACKET
    public final void mLEFT_SQUARE_BRACKET() throws RecognitionException {
        try {
            int _type = LEFT_SQUARE_BRACKET;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:45:22: ( '[' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:45:22: '['
            {
            match('['); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LEFT_SQUARE_BRACKET

    // $ANTLR start RIGHT_SQUARE_BRACKET
    public final void mRIGHT_SQUARE_BRACKET() throws RecognitionException {
        try {
            int _type = RIGHT_SQUARE_BRACKET;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:47:23: ( ']' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:47:23: ']'
            {
            match(']'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RIGHT_SQUARE_BRACKET

    // $ANTLR start LEFT_CURLY_BRACKET
    public final void mLEFT_CURLY_BRACKET() throws RecognitionException {
        try {
            int _type = LEFT_CURLY_BRACKET;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:49:21: ( '{' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:49:21: '{'
            {
            match('{'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LEFT_CURLY_BRACKET

    // $ANTLR start RIGHT_CURLY_BRACKET
    public final void mRIGHT_CURLY_BRACKET() throws RecognitionException {
        try {
            int _type = RIGHT_CURLY_BRACKET;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:51:22: ( '}' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:51:22: '}'
            {
            match('}'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RIGHT_CURLY_BRACKET

    // $ANTLR start BACKSLASH
    public final void mBACKSLASH() throws RecognitionException {
        try {
            int _type = BACKSLASH;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:53:12: ( '\\\\' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:53:12: '\\\\'
            {
            match('\\'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BACKSLASH

    // $ANTLR start CIRCUMFLEX
    public final void mCIRCUMFLEX() throws RecognitionException {
        try {
            int _type = CIRCUMFLEX;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:55:13: ( '^' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:55:13: '^'
            {
            match('^'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CIRCUMFLEX

    // $ANTLR start COMMERCIAL_AT
    public final void mCOMMERCIAL_AT() throws RecognitionException {
        try {
            int _type = COMMERCIAL_AT;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:57:16: ( '@' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:57:16: '@'
            {
            match('@'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end COMMERCIAL_AT

    // $ANTLR start NUMBER_SIGN
    public final void mNUMBER_SIGN() throws RecognitionException {
        try {
            int _type = NUMBER_SIGN;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:59:14: ( '#' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:59:14: '#'
            {
            match('#'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NUMBER_SIGN

    // $ANTLR start APOSTROPHE
    public final void mAPOSTROPHE() throws RecognitionException {
        try {
            int _type = APOSTROPHE;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:61:13: ( '\\'' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:61:13: '\\''
            {
            match('\''); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end APOSTROPHE

    // $ANTLR start SPECIAL_FORM
    public final void mSPECIAL_FORM() throws RecognitionException {
        try {
            int _type = SPECIAL_FORM;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:65:15: ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' )
            int alt1=16;
            switch ( input.LA(1) ) {
            case 'd':
                {
                int LA1_1 = input.LA(2);

                if ( (LA1_1=='e') ) {
                    alt1=1;
                }
                else if ( (LA1_1=='o') ) {
                    alt1=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 1, input);

                    throw nvae;
                }
                }
                break;
            case 'i':
                {
                alt1=2;
                }
                break;
            case 'l':
                {
                int LA1_3 = input.LA(2);

                if ( (LA1_3=='o') ) {
                    alt1=8;
                }
                else if ( (LA1_3=='e') ) {
                    alt1=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 3, input);

                    throw nvae;
                }
                }
                break;
            case 'q':
                {
                alt1=5;
                }
                break;
            case 'v':
                {
                alt1=6;
                }
                break;
            case 'f':
                {
                alt1=7;
                }
                break;
            case 'r':
                {
                alt1=9;
                }
                break;
            case 't':
                {
                int LA1_8 = input.LA(2);

                if ( (LA1_8=='r') ) {
                    alt1=11;
                }
                else if ( (LA1_8=='h') ) {
                    alt1=10;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 8, input);

                    throw nvae;
                }
                }
                break;
            case 'm':
                {
                int LA1_9 = input.LA(2);

                if ( (LA1_9=='o') ) {
                    int LA1_19 = input.LA(3);

                    if ( (LA1_19=='n') ) {
                        int LA1_20 = input.LA(4);

                        if ( (LA1_20=='i') ) {
                            int LA1_21 = input.LA(5);

                            if ( (LA1_21=='t') ) {
                                int LA1_22 = input.LA(6);

                                if ( (LA1_22=='o') ) {
                                    int LA1_23 = input.LA(7);

                                    if ( (LA1_23=='r') ) {
                                        int LA1_24 = input.LA(8);

                                        if ( (LA1_24=='-') ) {
                                            int LA1_25 = input.LA(9);

                                            if ( (LA1_25=='e') ) {
                                                int LA1_26 = input.LA(10);

                                                if ( (LA1_26=='n') ) {
                                                    alt1=12;
                                                }
                                                else if ( (LA1_26=='x') ) {
                                                    alt1=13;
                                                }
                                                else {
                                                    NoViableAltException nvae =
                                                        new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 26, input);

                                                    throw nvae;
                                                }
                                            }
                                            else {
                                                NoViableAltException nvae =
                                                    new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 25, input);

                                                throw nvae;
                                            }
                                        }
                                        else {
                                            NoViableAltException nvae =
                                                new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 24, input);

                                            throw nvae;
                                        }
                                    }
                                    else {
                                        NoViableAltException nvae =
                                            new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 23, input);

                                        throw nvae;
                                    }
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 22, input);

                                    throw nvae;
                                }
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 21, input);

                                throw nvae;
                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 20, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 19, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 9, input);

                    throw nvae;
                }
                }
                break;
            case 'n':
                {
                alt1=14;
                }
                break;
            case 's':
                {
                alt1=15;
                }
                break;
            case '.':
                {
                alt1=16;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("65:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:65:15: 'def'
                    {
                    match("def"); 


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:65:23: 'if'
                    {
                    match("if"); 


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:65:30: 'do'
                    {
                    match("do"); 


                    }
                    break;
                case 4 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:65:37: 'let'
                    {
                    match("let"); 


                    }
                    break;
                case 5 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:65:45: 'quote'
                    {
                    match("quote"); 


                    }
                    break;
                case 6 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:65:55: 'var'
                    {
                    match("var"); 


                    }
                    break;
                case 7 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:65:63: 'fn'
                    {
                    match("fn"); 


                    }
                    break;
                case 8 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:65:70: 'loop'
                    {
                    match("loop"); 


                    }
                    break;
                case 9 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:66:13: 'recur'
                    {
                    match("recur"); 


                    }
                    break;
                case 10 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:66:23: 'throw'
                    {
                    match("throw"); 


                    }
                    break;
                case 11 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:66:33: 'try'
                    {
                    match("try"); 


                    }
                    break;
                case 12 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:66:41: 'monitor-enter'
                    {
                    match("monitor-enter"); 


                    }
                    break;
                case 13 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:66:59: 'monitor-exit'
                    {
                    match("monitor-exit"); 


                    }
                    break;
                case 14 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:67:13: 'new'
                    {
                    match("new"); 


                    }
                    break;
                case 15 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:67:21: 'set!'
                    {
                    match("set!"); 


                    }
                    break;
                case 16 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:67:30: '.'
                    {
                    match('.'); 

                    }
                    break;

            }
            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SPECIAL_FORM

    // $ANTLR start STRING
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:72:8: ( '\"' ( EscapeSequence | ~ ( '\\\\' | '\"' ) )* '\"' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:72:8: '\"' ( EscapeSequence | ~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:72:12: ( EscapeSequence | ~ ( '\\\\' | '\"' ) )*
            loop2:
            do {
                int alt2=3;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='\\') ) {
                    alt2=1;
                }
                else if ( ((LA2_0>='\u0000' && LA2_0<='!')||(LA2_0>='#' && LA2_0<='[')||(LA2_0>=']' && LA2_0<='\uFFFE')) ) {
                    alt2=2;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:72:14: EscapeSequence
            	    {
            	    mEscapeSequence(); 

            	    }
            	    break;
            	case 2 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:72:31: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            match('\"'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end STRING

    // $ANTLR start EscapeSequence
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:78:9: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UnicodeEscape | OctalEscape )
            int alt3=3;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='\\') ) {
                switch ( input.LA(2) ) {
                case 'u':
                    {
                    alt3=2;
                    }
                    break;
                case '\"':
                case '\'':
                case '\\':
                case 'b':
                case 'f':
                case 'n':
                case 'r':
                case 't':
                    {
                    alt3=1;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    {
                    alt3=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("76:1: fragment EscapeSequence : ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UnicodeEscape | OctalEscape );", 3, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("76:1: fragment EscapeSequence : ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UnicodeEscape | OctalEscape );", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:78:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 
                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:79:9: UnicodeEscape
                    {
                    mUnicodeEscape(); 

                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:80:9: OctalEscape
                    {
                    mOctalEscape(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end EscapeSequence

    // $ANTLR start UnicodeEscape
    public final void mUnicodeEscape() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:86:9: ( '\\\\' 'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:86:9: '\\\\' 'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
            {
            match('\\'); 
            match('u'); 
            mHEXDIGIT(); 
            mHEXDIGIT(); 
            mHEXDIGIT(); 
            mHEXDIGIT(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end UnicodeEscape

    // $ANTLR start OctalEscape
    public final void mOctalEscape() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:92:9: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt4=3;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='\\') ) {
                int LA4_1 = input.LA(2);

                if ( ((LA4_1>='0' && LA4_1<='3')) ) {
                    int LA4_2 = input.LA(3);

                    if ( ((LA4_2>='0' && LA4_2<='7')) ) {
                        int LA4_4 = input.LA(4);

                        if ( ((LA4_4>='0' && LA4_4<='7')) ) {
                            alt4=1;
                        }
                        else {
                            alt4=2;}
                    }
                    else {
                        alt4=3;}
                }
                else if ( ((LA4_1>='4' && LA4_1<='7')) ) {
                    int LA4_3 = input.LA(3);

                    if ( ((LA4_3>='0' && LA4_3<='7')) ) {
                        alt4=2;
                    }
                    else {
                        alt4=3;}
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("90:1: fragment OctalEscape : ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) );", 4, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("90:1: fragment OctalEscape : ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) );", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:92:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:92:14: ( '0' .. '3' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:92:15: '0' .. '3'
                    {
                    matchRange('0','3'); 

                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:92:25: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:92:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:92:36: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:92:37: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:93:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:93:14: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:93:15: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:93:25: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:93:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:94:9: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:94:14: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:94:15: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end OctalEscape

    // $ANTLR start NUMBER
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:9: ( ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )? )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:9: ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )?
            {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:9: ( '-' )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='-') ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:9: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:14: ( '0' .. '9' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:14: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:24: ( '.' ( '0' .. '9' )+ )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='.') ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:25: '.' ( '0' .. '9' )+
                    {
                    match('.'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:29: ( '0' .. '9' )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( ((LA7_0>='0' && LA7_0<='9')) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:29: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt7 >= 1 ) break loop7;
                                EarlyExitException eee =
                                    new EarlyExitException(7, input);
                                throw eee;
                        }
                        cnt7++;
                    } while (true);


                    }
                    break;

            }

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:41: ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='E'||LA11_0=='e') ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:42: ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+
                    {
                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:52: ( '-' )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0=='-') ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:52: '-'
                            {
                            match('-'); 

                            }
                            break;

                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:57: ( '0' .. '9' )+
                    int cnt10=0;
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0>='0' && LA10_0<='9')) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:99:57: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt10 >= 1 ) break loop10;
                                EarlyExitException eee =
                                    new EarlyExitException(10, input);
                                throw eee;
                        }
                        cnt10++;
                    } while (true);


                    }
                    break;

            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NUMBER

    // $ANTLR start CHARACTER
    public final void mCHARACTER() throws RecognitionException {
        try {
            int _type = CHARACTER;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:9: ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT | BACKSLASH . )
            int alt12=5;
            int LA12_0 = input.LA(1);

            if ( (LA12_0=='\\') ) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1=='s') ) {
                    int LA12_2 = input.LA(3);

                    if ( (LA12_2=='p') ) {
                        alt12=2;
                    }
                    else {
                        alt12=5;}
                }
                else if ( (LA12_1=='u') ) {
                    int LA12_3 = input.LA(3);

                    if ( ((LA12_3>='0' && LA12_3<='9')||(LA12_3>='A' && LA12_3<='F')||(LA12_3>='a' && LA12_3<='f')) ) {
                        alt12=4;
                    }
                    else {
                        alt12=5;}
                }
                else if ( (LA12_1=='t') ) {
                    int LA12_4 = input.LA(3);

                    if ( (LA12_4=='a') ) {
                        alt12=3;
                    }
                    else {
                        alt12=5;}
                }
                else if ( (LA12_1=='n') ) {
                    int LA12_5 = input.LA(3);

                    if ( (LA12_5=='e') ) {
                        alt12=1;
                    }
                    else {
                        alt12=5;}
                }
                else if ( ((LA12_1>='\u0000' && LA12_1<='m')||(LA12_1>='o' && LA12_1<='r')||(LA12_1>='v' && LA12_1<='\uFFFE')) ) {
                    alt12=5;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("102:1: CHARACTER : ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT | BACKSLASH . );", 12, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("102:1: CHARACTER : ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT | BACKSLASH . );", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:9: '\\\\newline'
                    {
                    match("\\newline"); 


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:104:9: '\\\\space'
                    {
                    match("\\space"); 


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:105:9: '\\\\tab'
                    {
                    match("\\tab"); 


                    }
                    break;
                case 4 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:106:9: '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
                    {
                    match("\\u"); 

                    mHEXDIGIT(); 
                    mHEXDIGIT(); 
                    mHEXDIGIT(); 
                    mHEXDIGIT(); 

                    }
                    break;
                case 5 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:107:9: BACKSLASH .
                    {
                    mBACKSLASH(); 
                    matchAny(); 

                    }
                    break;

            }
            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CHARACTER

    // $ANTLR start HEXDIGIT
    public final void mHEXDIGIT() throws RecognitionException {
        try {
            int _type = HEXDIGIT;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:111:9: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end HEXDIGIT

    // $ANTLR start NIL
    public final void mNIL() throws RecognitionException {
        try {
            int _type = NIL;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:113:9: ( 'nil' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:113:9: 'nil'
            {
            match("nil"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NIL

    // $ANTLR start BOOLEAN
    public final void mBOOLEAN() throws RecognitionException {
        try {
            int _type = BOOLEAN;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:117:9: ( 'true' | 'false' )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0=='t') ) {
                alt13=1;
            }
            else if ( (LA13_0=='f') ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("116:1: BOOLEAN : ( 'true' | 'false' );", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:117:9: 'true'
                    {
                    match("true"); 


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:118:9: 'false'
                    {
                    match("false"); 


                    }
                    break;

            }
            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BOOLEAN

    // $ANTLR start SYMBOL
    public final void mSYMBOL() throws RecognitionException {
        try {
            int _type = SYMBOL;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:122:9: ( '/' | NAME ( '/' NAME )? )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0=='/') ) {
                alt15=1;
            }
            else if ( (LA15_0=='!'||LA15_0=='$'||(LA15_0>='*' && LA15_0<='+')||LA15_0=='-'||(LA15_0>='<' && LA15_0<='?')||(LA15_0>='A' && LA15_0<='Z')||LA15_0=='_'||(LA15_0>='a' && LA15_0<='z')) ) {
                alt15=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("121:1: SYMBOL : ( '/' | NAME ( '/' NAME )? );", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:122:9: '/'
                    {
                    match('/'); 

                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:123:9: NAME ( '/' NAME )?
                    {
                    mNAME(); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:123:14: ( '/' NAME )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0=='/') ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:123:15: '/' NAME
                            {
                            match('/'); 
                            mNAME(); 

                            }
                            break;

                    }


                    }
                    break;

            }
            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SYMBOL

    // $ANTLR start METADATA_TYPEHINT
    public final void mMETADATA_TYPEHINT() throws RecognitionException {
        try {
            int _type = METADATA_TYPEHINT;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:127:3: ( NUMBER_SIGN CIRCUMFLEX NAME )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:127:3: NUMBER_SIGN CIRCUMFLEX NAME
            {
            mNUMBER_SIGN(); 
            mCIRCUMFLEX(); 
            mNAME(); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end METADATA_TYPEHINT

    // $ANTLR start NAME
    public final void mNAME() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:9: ( SYMBOL_HEAD ( SYMBOL_REST )* ( ':' ( SYMBOL_REST )+ )* )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:9: SYMBOL_HEAD ( SYMBOL_REST )* ( ':' ( SYMBOL_REST )+ )*
            {
            mSYMBOL_HEAD(); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:21: ( SYMBOL_REST )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0=='!'||(LA16_0>='#' && LA16_0<='$')||(LA16_0>='*' && LA16_0<='+')||(LA16_0>='-' && LA16_0<='.')||(LA16_0>='0' && LA16_0<='9')||(LA16_0>='<' && LA16_0<='?')||(LA16_0>='A' && LA16_0<='Z')||LA16_0=='_'||(LA16_0>='a' && LA16_0<='z')) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:21: SYMBOL_REST
            	    {
            	    mSYMBOL_REST(); 

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:34: ( ':' ( SYMBOL_REST )+ )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==':') ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:35: ':' ( SYMBOL_REST )+
            	    {
            	    match(':'); 
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:39: ( SYMBOL_REST )+
            	    int cnt17=0;
            	    loop17:
            	    do {
            	        int alt17=2;
            	        int LA17_0 = input.LA(1);

            	        if ( (LA17_0=='!'||(LA17_0>='#' && LA17_0<='$')||(LA17_0>='*' && LA17_0<='+')||(LA17_0>='-' && LA17_0<='.')||(LA17_0>='0' && LA17_0<='9')||(LA17_0>='<' && LA17_0<='?')||(LA17_0>='A' && LA17_0<='Z')||LA17_0=='_'||(LA17_0>='a' && LA17_0<='z')) ) {
            	            alt17=1;
            	        }


            	        switch (alt17) {
            	    	case 1 :
            	    	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:39: SYMBOL_REST
            	    	    {
            	    	    mSYMBOL_REST(); 

            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt17 >= 1 ) break loop17;
            	                EarlyExitException eee =
            	                    new EarlyExitException(17, input);
            	                throw eee;
            	        }
            	        cnt17++;
            	    } while (true);


            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end NAME

    // $ANTLR start SYMBOL_HEAD
    public final void mSYMBOL_HEAD() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:136:9: ( 'a' .. 'z' | 'A' .. 'Z' | '*' | '+' | '!' | '-' | '_' | '?' | '>' | '<' | '=' | '$' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:
            {
            if ( input.LA(1)=='!'||input.LA(1)=='$'||(input.LA(1)>='*' && input.LA(1)<='+')||input.LA(1)=='-'||(input.LA(1)>='<' && input.LA(1)<='?')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end SYMBOL_HEAD

    // $ANTLR start SYMBOL_REST
    public final void mSYMBOL_REST() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:142:9: ( SYMBOL_HEAD | '0' .. '9' | '.' | NUMBER_SIGN )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:
            {
            if ( input.LA(1)=='!'||(input.LA(1)>='#' && input.LA(1)<='$')||(input.LA(1)>='*' && input.LA(1)<='+')||(input.LA(1)>='-' && input.LA(1)<='.')||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='<' && input.LA(1)<='?')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end SYMBOL_REST

    // $ANTLR start KEYWORD
    public final void mKEYWORD() throws RecognitionException {
        try {
            int _type = KEYWORD;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:158:9: ( ':' SYMBOL )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:158:9: ':' SYMBOL
            {
            match(':'); 
            mSYMBOL(); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KEYWORD

    // $ANTLR start SYNTAX_QUOTE
    public final void mSYNTAX_QUOTE() throws RecognitionException {
        try {
            int _type = SYNTAX_QUOTE;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:162:9: ( '`' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:162:9: '`'
            {
            match('`'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SYNTAX_QUOTE

    // $ANTLR start UNQUOTE_SPLICING
    public final void mUNQUOTE_SPLICING() throws RecognitionException {
        try {
            int _type = UNQUOTE_SPLICING;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:166:9: ( '~@' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:166:9: '~@'
            {
            match("~@"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end UNQUOTE_SPLICING

    // $ANTLR start UNQUOTE
    public final void mUNQUOTE() throws RecognitionException {
        try {
            int _type = UNQUOTE;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:170:9: ( '~' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:170:9: '~'
            {
            match('~'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end UNQUOTE

    // $ANTLR start COMMENT
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:9: ( ';' (~ ( '\\r' | '\\n' ) )* ( ( '\\r' )? '\\n' )? )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:9: ';' (~ ( '\\r' | '\\n' ) )* ( ( '\\r' )? '\\n' )?
            {
            match(';'); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:13: (~ ( '\\r' | '\\n' ) )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( ((LA19_0>='\u0000' && LA19_0<='\t')||(LA19_0>='\u000B' && LA19_0<='\f')||(LA19_0>='\u000E' && LA19_0<='\uFFFE')) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:13: ~ ( '\\r' | '\\n' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:29: ( ( '\\r' )? '\\n' )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0=='\n'||LA21_0=='\r') ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:30: ( '\\r' )? '\\n'
                    {
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:30: ( '\\r' )?
                    int alt20=2;
                    int LA20_0 = input.LA(1);

                    if ( (LA20_0=='\r') ) {
                        alt20=1;
                    }
                    switch (alt20) {
                        case 1 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:30: '\\r'
                            {
                            match('\r'); 

                            }
                            break;

                    }

                    match('\n'); 

                    }
                    break;

            }

            channel=HIDDEN;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end COMMENT

    // $ANTLR start SPACE
    public final void mSPACE() throws RecognitionException {
        try {
            int _type = SPACE;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:177:9: ( ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+ )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:177:9: ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+
            {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:177:9: ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+
            int cnt22=0;
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( ((LA22_0>='\t' && LA22_0<='\n')||LA22_0=='\r'||LA22_0==' '||LA22_0==',') ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' '||input.LA(1)==',' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt22 >= 1 ) break loop22;
                        EarlyExitException eee =
                            new EarlyExitException(22, input);
                        throw eee;
                }
                cnt22++;
            } while (true);

            channel=HIDDEN;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SPACE

    // $ANTLR start LAMBDA_ARG
    public final void mLAMBDA_ARG() throws RecognitionException {
        try {
            int _type = LAMBDA_ARG;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:182:9: ( '%' '1' .. '9' ( '0' .. '9' )* | '%&' | '%' )
            int alt24=3;
            int LA24_0 = input.LA(1);

            if ( (LA24_0=='%') ) {
                switch ( input.LA(2) ) {
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt24=1;
                    }
                    break;
                case '&':
                    {
                    alt24=2;
                    }
                    break;
                default:
                    alt24=3;}

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("181:1: LAMBDA_ARG : ( '%' '1' .. '9' ( '0' .. '9' )* | '%&' | '%' );", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:182:9: '%' '1' .. '9' ( '0' .. '9' )*
                    {
                    match('%'); 
                    matchRange('1','9'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:182:22: ( '0' .. '9' )*
                    loop23:
                    do {
                        int alt23=2;
                        int LA23_0 = input.LA(1);

                        if ( ((LA23_0>='0' && LA23_0<='9')) ) {
                            alt23=1;
                        }


                        switch (alt23) {
                    	case 1 :
                    	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:182:22: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop23;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:183:9: '%&'
                    {
                    match("%&"); 


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:184:9: '%'
                    {
                    match('%'); 

                    }
                    break;

            }
            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LAMBDA_ARG

    public void mTokens() throws RecognitionException {
        // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:10: ( OPEN_PAREN | CLOSE_PAREN | AMPERSAND | LEFT_SQUARE_BRACKET | RIGHT_SQUARE_BRACKET | LEFT_CURLY_BRACKET | RIGHT_CURLY_BRACKET | BACKSLASH | CIRCUMFLEX | COMMERCIAL_AT | NUMBER_SIGN | APOSTROPHE | SPECIAL_FORM | STRING | NUMBER | CHARACTER | HEXDIGIT | NIL | BOOLEAN | SYMBOL | METADATA_TYPEHINT | KEYWORD | SYNTAX_QUOTE | UNQUOTE_SPLICING | UNQUOTE | COMMENT | SPACE | LAMBDA_ARG )
        int alt25=28;
        alt25 = dfa25.predict(input);
        switch (alt25) {
            case 1 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:10: OPEN_PAREN
                {
                mOPEN_PAREN(); 

                }
                break;
            case 2 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:21: CLOSE_PAREN
                {
                mCLOSE_PAREN(); 

                }
                break;
            case 3 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:33: AMPERSAND
                {
                mAMPERSAND(); 

                }
                break;
            case 4 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:43: LEFT_SQUARE_BRACKET
                {
                mLEFT_SQUARE_BRACKET(); 

                }
                break;
            case 5 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:63: RIGHT_SQUARE_BRACKET
                {
                mRIGHT_SQUARE_BRACKET(); 

                }
                break;
            case 6 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:84: LEFT_CURLY_BRACKET
                {
                mLEFT_CURLY_BRACKET(); 

                }
                break;
            case 7 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:103: RIGHT_CURLY_BRACKET
                {
                mRIGHT_CURLY_BRACKET(); 

                }
                break;
            case 8 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:123: BACKSLASH
                {
                mBACKSLASH(); 

                }
                break;
            case 9 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:133: CIRCUMFLEX
                {
                mCIRCUMFLEX(); 

                }
                break;
            case 10 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:144: COMMERCIAL_AT
                {
                mCOMMERCIAL_AT(); 

                }
                break;
            case 11 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:158: NUMBER_SIGN
                {
                mNUMBER_SIGN(); 

                }
                break;
            case 12 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:170: APOSTROPHE
                {
                mAPOSTROPHE(); 

                }
                break;
            case 13 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:181: SPECIAL_FORM
                {
                mSPECIAL_FORM(); 

                }
                break;
            case 14 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:194: STRING
                {
                mSTRING(); 

                }
                break;
            case 15 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:201: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 16 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:208: CHARACTER
                {
                mCHARACTER(); 

                }
                break;
            case 17 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:218: HEXDIGIT
                {
                mHEXDIGIT(); 

                }
                break;
            case 18 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:227: NIL
                {
                mNIL(); 

                }
                break;
            case 19 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:231: BOOLEAN
                {
                mBOOLEAN(); 

                }
                break;
            case 20 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:239: SYMBOL
                {
                mSYMBOL(); 

                }
                break;
            case 21 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:246: METADATA_TYPEHINT
                {
                mMETADATA_TYPEHINT(); 

                }
                break;
            case 22 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:264: KEYWORD
                {
                mKEYWORD(); 

                }
                break;
            case 23 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:272: SYNTAX_QUOTE
                {
                mSYNTAX_QUOTE(); 

                }
                break;
            case 24 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:285: UNQUOTE_SPLICING
                {
                mUNQUOTE_SPLICING(); 

                }
                break;
            case 25 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:302: UNQUOTE
                {
                mUNQUOTE(); 

                }
                break;
            case 26 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:310: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 27 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:318: SPACE
                {
                mSPACE(); 

                }
                break;
            case 28 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:324: LAMBDA_ARG
                {
                mLAMBDA_ARG(); 

                }
                break;

        }

    }


    protected DFA25 dfa25 = new DFA25(this);
    static final String DFA25_eotS =
        "\10\uffff\1\45\2\uffff\1\46\1\uffff\1\52\4\35\1\52\5\35\2\uffff"+
        "\1\35\1\uffff\1\52\3\uffff\1\74\7\uffff\1\35\1\30\1\uffff\1\30\4"+
        "\35\1\30\10\35\1\72\3\uffff\2\30\2\35\1\30\2\35\1\30\3\35\1\124"+
        "\1\30\3\35\1\30\3\35\1\134\2\35\1\uffff\1\30\1\72\1\35\1\72\1\30"+
        "\1\134\1\30\1\uffff\1\30\12\35\2\30";
    static final String DFA25_eofS =
        "\152\uffff";
    static final String DFA25_minS =
        "\1\11\7\uffff\1\0\2\uffff\1\136\1\uffff\1\41\1\146\1\145\1\165\1"+
        "\141\1\41\1\145\1\150\1\157\2\145\2\uffff\1\60\1\uffff\1\41\3\uffff"+
        "\1\100\7\uffff\1\146\1\41\1\uffff\1\41\1\164\2\157\1\162\1\41\1"+
        "\154\1\143\1\165\1\162\1\156\1\154\1\167\1\164\1\41\3\uffff\2\41"+
        "\1\160\1\164\1\41\1\163\1\165\1\41\1\145\1\157\1\151\3\41\1\60\1"+
        "\55\1\41\2\145\1\162\1\41\1\167\1\164\1\uffff\2\41\1\60\4\41\1\uffff"+
        "\1\41\1\157\1\162\1\55\1\145\1\156\1\164\1\151\1\145\1\164\1\162"+
        "\2\41";
    static final String DFA25_maxS =
        "\1\176\7\uffff\1\ufffe\2\uffff\1\136\1\uffff\1\172\1\146\1\157\1"+
        "\165\1\141\1\172\1\145\1\162\1\157\1\151\1\145\2\uffff\1\71\1\uffff"+
        "\1\172\3\uffff\1\100\7\uffff\1\146\1\172\1\uffff\1\172\1\164\2\157"+
        "\1\162\1\172\1\154\1\143\1\171\1\162\1\156\1\154\1\167\1\164\1\172"+
        "\3\uffff\2\172\1\160\1\164\1\172\1\163\1\165\1\172\1\145\1\157\1"+
        "\151\2\172\1\41\2\71\1\172\2\145\1\162\1\172\1\167\1\164\1\uffff"+
        "\2\172\1\71\4\172\1\uffff\1\172\1\157\1\162\1\55\1\145\1\170\1\164"+
        "\1\151\1\145\1\164\1\162\2\172";
    static final String DFA25_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\11\1\12\1\uffff\1"+
        "\14\13\uffff\1\15\1\16\1\uffff\1\17\1\uffff\1\24\1\26\1\27\1\uffff"+
        "\1\32\1\33\1\34\1\20\1\10\1\13\1\25\2\uffff\1\21\17\uffff\1\17\1"+
        "\30\1\31\27\uffff\1\22\7\uffff\1\23\15\uffff";
    static final String DFA25_specialS =
        "\152\uffff}>";
    static final String[] DFA25_transitionS = {
            "\2\42\2\uffff\1\42\22\uffff\1\42\1\35\1\31\1\13\1\35\1\43\1"+
            "\3\1\14\1\1\1\2\2\35\1\42\1\32\1\30\1\35\12\33\1\36\1\41\4\35"+
            "\1\12\6\34\24\35\1\4\1\10\1\5\1\11\1\35\1\37\3\34\1\15\1\34"+
            "\1\22\2\35\1\16\2\35\1\17\1\25\1\26\2\35\1\20\1\23\1\27\1\24"+
            "\1\35\1\21\4\35\1\6\1\uffff\1\7\1\40",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\uffff\44",
            "",
            "",
            "\1\47",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\4\35\1\50\11\35\1\51\13\35",
            "\1\53",
            "\1\54\11\uffff\1\55",
            "\1\56",
            "\1\57",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\1\61\14\35\1\60\14\35",
            "\1\62",
            "\1\64\11\uffff\1\63",
            "\1\65",
            "\1\67\3\uffff\1\66",
            "\1\70",
            "",
            "",
            "\12\71",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "",
            "",
            "\1\73",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\75",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\101",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\102",
            "\1\103",
            "\1\105\3\uffff\1\104",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\1\35\1\113\1\35\12\71"+
            "\1\35\1\uffff\4\35\1\uffff\4\35\1\114\25\35\4\uffff\1\35\1\uffff"+
            "\4\35\1\114\25\35",
            "",
            "",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\115",
            "\1\116",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\117",
            "\1\120",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\121",
            "\1\122",
            "\1\123",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\125",
            "\12\126",
            "\1\127\2\uffff\12\130",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\131",
            "\1\132",
            "\1\133",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\135",
            "\1\136",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\3\35\12\126\1\35\1\uffff"+
            "\4\35\1\uffff\4\35\1\114\25\35\4\uffff\1\35\1\uffff\4\35\1\114"+
            "\25\35",
            "\12\130",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\3\35\12\130\1\35\1\uffff"+
            "\4\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\137",
            "\1\140",
            "\1\141",
            "\1\142",
            "\1\143\11\uffff\1\144",
            "\1\145",
            "\1\146",
            "\1\147",
            "\1\150",
            "\1\151",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35"
    };

    static final short[] DFA25_eot = DFA.unpackEncodedString(DFA25_eotS);
    static final short[] DFA25_eof = DFA.unpackEncodedString(DFA25_eofS);
    static final char[] DFA25_min = DFA.unpackEncodedStringToUnsignedChars(DFA25_minS);
    static final char[] DFA25_max = DFA.unpackEncodedStringToUnsignedChars(DFA25_maxS);
    static final short[] DFA25_accept = DFA.unpackEncodedString(DFA25_acceptS);
    static final short[] DFA25_special = DFA.unpackEncodedString(DFA25_specialS);
    static final short[][] DFA25_transition;

    static {
        int numStates = DFA25_transitionS.length;
        DFA25_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA25_transition[i] = DFA.unpackEncodedString(DFA25_transitionS[i]);
        }
    }

    class DFA25 extends DFA {

        public DFA25(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 25;
            this.eot = DFA25_eot;
            this.eof = DFA25_eof;
            this.min = DFA25_min;
            this.max = DFA25_max;
            this.accept = DFA25_accept;
            this.special = DFA25_special;
            this.transition = DFA25_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( OPEN_PAREN | CLOSE_PAREN | AMPERSAND | LEFT_SQUARE_BRACKET | RIGHT_SQUARE_BRACKET | LEFT_CURLY_BRACKET | RIGHT_CURLY_BRACKET | BACKSLASH | CIRCUMFLEX | COMMERCIAL_AT | NUMBER_SIGN | APOSTROPHE | SPECIAL_FORM | STRING | NUMBER | CHARACTER | HEXDIGIT | NIL | BOOLEAN | SYMBOL | METADATA_TYPEHINT | KEYWORD | SYNTAX_QUOTE | UNQUOTE_SPLICING | UNQUOTE | COMMENT | SPACE | LAMBDA_ARG );";
        }
    }
 

}