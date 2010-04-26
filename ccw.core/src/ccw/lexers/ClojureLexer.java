package ccw.lexers;
// $ANTLR 3.0 /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g 2010-04-26 21:59:07

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ClojureLexer extends Lexer {
    public static final int SYNTAX_QUOTE=33;
    public static final int KEYWORD=32;
    public static final int SYMBOL=28;
    public static final int METADATA_TYPEHINT=29;
    public static final int SYMBOL_HEAD=30;
    public static final int NUMBER=23;
    public static final int AMPERSAND=6;
    public static final int OPEN_PAREN=4;
    public static final int COMMERCIAL_AT=13;
    public static final int Tokens=39;
    public static final int EOF=-1;
    public static final int SPACE=37;
    public static final int CHARACTER=24;
    public static final int RIGHT_CURLY_BRACKET=10;
    public static final int LEFT_SQUARE_BRACKET=7;
    public static final int RIGHT_SQUARE_BRACKET=8;
    public static final int LEFT_CURLY_BRACKET=9;
    public static final int NAME=27;
    public static final int BOOLEAN=26;
    public static final int NIL=25;
    public static final int UNQUOTE=35;
    public static final int UnicodeEscape=20;
    public static final int LAMBDA_ARG=38;
    public static final int NUMBER_SIGN=14;
    public static final int SPECIAL_FORM=16;
    public static final int CLOSE_PAREN=5;
    public static final int APOSTROPHE=15;
    public static final int SYMBOL_REST=31;
    public static final int REGEX_LITERAL=19;
    public static final int COMMENT=36;
    public static final int EscapeSequence=17;
    public static final int OctalEscape=21;
    public static final int CIRCUMFLEX=12;
    public static final int UNQUOTE_SPLICING=34;
    public static final int STRING=18;
    public static final int BACKSLASH=11;
    public static final int HEXDIGIT=22;
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

                                                if ( (LA1_26=='x') ) {
                                                    alt1=13;
                                                }
                                                else if ( (LA1_26=='n') ) {
                                                    alt1=12;
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

    // $ANTLR start REGEX_LITERAL
    public final void mREGEX_LITERAL() throws RecognitionException {
        try {
            int _type = REGEX_LITERAL;
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:76:7: ( NUMBER_SIGN '\"' (~ ( '\\\\' | '\"' ) | '\\\\' . )* '\"' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:76:7: NUMBER_SIGN '\"' (~ ( '\\\\' | '\"' ) | '\\\\' . )* '\"'
            {
            mNUMBER_SIGN(); 
            match('\"'); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:76:23: (~ ( '\\\\' | '\"' ) | '\\\\' . )*
            loop3:
            do {
                int alt3=3;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='\u0000' && LA3_0<='!')||(LA3_0>='#' && LA3_0<='[')||(LA3_0>=']' && LA3_0<='\uFFFE')) ) {
                    alt3=1;
                }
                else if ( (LA3_0=='\\') ) {
                    alt3=2;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:76:25: ~ ( '\\\\' | '\"' )
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
            	case 2 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:76:41: '\\\\' .
            	    {
            	    match('\\'); 
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match('\"'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end REGEX_LITERAL

    // $ANTLR start EscapeSequence
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:82:9: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UnicodeEscape | OctalEscape )
            int alt4=3;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='\\') ) {
                switch ( input.LA(2) ) {
                case 'u':
                    {
                    alt4=2;
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
                    alt4=1;
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
                    alt4=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("80:1: fragment EscapeSequence : ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UnicodeEscape | OctalEscape );", 4, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("80:1: fragment EscapeSequence : ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UnicodeEscape | OctalEscape );", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:82:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
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
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:83:9: UnicodeEscape
                    {
                    mUnicodeEscape(); 

                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:84:9: OctalEscape
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:90:9: ( '\\\\' 'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:90:9: '\\\\' 'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:96:9: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt5=3;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='\\') ) {
                int LA5_1 = input.LA(2);

                if ( ((LA5_1>='0' && LA5_1<='3')) ) {
                    int LA5_2 = input.LA(3);

                    if ( ((LA5_2>='0' && LA5_2<='7')) ) {
                        int LA5_4 = input.LA(4);

                        if ( ((LA5_4>='0' && LA5_4<='7')) ) {
                            alt5=1;
                        }
                        else {
                            alt5=2;}
                    }
                    else {
                        alt5=3;}
                }
                else if ( ((LA5_1>='4' && LA5_1<='7')) ) {
                    int LA5_3 = input.LA(3);

                    if ( ((LA5_3>='0' && LA5_3<='7')) ) {
                        alt5=2;
                    }
                    else {
                        alt5=3;}
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("94:1: fragment OctalEscape : ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) );", 5, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("94:1: fragment OctalEscape : ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) );", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:96:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:96:14: ( '0' .. '3' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:96:15: '0' .. '3'
                    {
                    matchRange('0','3'); 

                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:96:25: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:96:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:96:36: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:96:37: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:97:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:97:14: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:97:15: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:97:25: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:97:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:98:9: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:98:14: ( '0' .. '7' )
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:98:15: '0' .. '7'
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:9: ( ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )? )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:9: ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )?
            {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:9: ( '-' )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='-') ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:9: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:14: ( '0' .. '9' )+
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
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:14: '0' .. '9'
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

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:24: ( '.' ( '0' .. '9' )+ )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='.') ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:25: '.' ( '0' .. '9' )+
                    {
                    match('.'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:29: ( '0' .. '9' )+
                    int cnt8=0;
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( ((LA8_0>='0' && LA8_0<='9')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:29: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt8 >= 1 ) break loop8;
                                EarlyExitException eee =
                                    new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);


                    }
                    break;

            }

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:41: ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0=='E'||LA12_0=='e') ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:42: ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+
                    {
                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:52: ( '-' )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0=='-') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:52: '-'
                            {
                            match('-'); 

                            }
                            break;

                    }

                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:57: ( '0' .. '9' )+
                    int cnt11=0;
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( ((LA11_0>='0' && LA11_0<='9')) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:103:57: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt11 >= 1 ) break loop11;
                                EarlyExitException eee =
                                    new EarlyExitException(11, input);
                                throw eee;
                        }
                        cnt11++;
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:107:9: ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT | BACKSLASH . )
            int alt13=5;
            int LA13_0 = input.LA(1);

            if ( (LA13_0=='\\') ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1=='t') ) {
                    int LA13_2 = input.LA(3);

                    if ( (LA13_2=='a') ) {
                        alt13=3;
                    }
                    else {
                        alt13=5;}
                }
                else if ( (LA13_1=='n') ) {
                    int LA13_3 = input.LA(3);

                    if ( (LA13_3=='e') ) {
                        alt13=1;
                    }
                    else {
                        alt13=5;}
                }
                else if ( (LA13_1=='s') ) {
                    int LA13_4 = input.LA(3);

                    if ( (LA13_4=='p') ) {
                        alt13=2;
                    }
                    else {
                        alt13=5;}
                }
                else if ( (LA13_1=='u') ) {
                    int LA13_5 = input.LA(3);

                    if ( ((LA13_5>='0' && LA13_5<='9')||(LA13_5>='A' && LA13_5<='F')||(LA13_5>='a' && LA13_5<='f')) ) {
                        alt13=4;
                    }
                    else {
                        alt13=5;}
                }
                else if ( ((LA13_1>='\u0000' && LA13_1<='m')||(LA13_1>='o' && LA13_1<='r')||(LA13_1>='v' && LA13_1<='\uFFFE')) ) {
                    alt13=5;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("106:1: CHARACTER : ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT | BACKSLASH . );", 13, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("106:1: CHARACTER : ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT | BACKSLASH . );", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:107:9: '\\\\newline'
                    {
                    match("\\newline"); 


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:108:9: '\\\\space'
                    {
                    match("\\space"); 


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:109:9: '\\\\tab'
                    {
                    match("\\tab"); 


                    }
                    break;
                case 4 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:110:9: '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
                    {
                    match("\\u"); 

                    mHEXDIGIT(); 
                    mHEXDIGIT(); 
                    mHEXDIGIT(); 
                    mHEXDIGIT(); 

                    }
                    break;
                case 5 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:111:9: BACKSLASH .
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:115:9: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:117:9: ( 'nil' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:117:9: 'nil'
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:121:9: ( 'true' | 'false' )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0=='t') ) {
                alt14=1;
            }
            else if ( (LA14_0=='f') ) {
                alt14=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("120:1: BOOLEAN : ( 'true' | 'false' );", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:121:9: 'true'
                    {
                    match("true"); 


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:122:9: 'false'
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:126:9: ( '/' | NAME ( '/' NAME )? )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='/') ) {
                alt16=1;
            }
            else if ( (LA16_0=='!'||LA16_0=='$'||(LA16_0>='*' && LA16_0<='+')||LA16_0=='-'||(LA16_0>='<' && LA16_0<='?')||(LA16_0>='A' && LA16_0<='Z')||LA16_0=='_'||(LA16_0>='a' && LA16_0<='z')) ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("125:1: SYMBOL : ( '/' | NAME ( '/' NAME )? );", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:126:9: '/'
                    {
                    match('/'); 

                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:127:9: NAME ( '/' NAME )?
                    {
                    mNAME(); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:127:14: ( '/' NAME )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0=='/') ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:127:15: '/' NAME
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:3: ( ( NUMBER_SIGN )* CIRCUMFLEX ( 'ints' | 'floats' | 'longs' | 'doubles' | 'objects' | NAME | STRING )* )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:3: ( NUMBER_SIGN )* CIRCUMFLEX ( 'ints' | 'floats' | 'longs' | 'doubles' | 'objects' | NAME | STRING )*
            {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:3: ( NUMBER_SIGN )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0=='#') ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:3: NUMBER_SIGN
            	    {
            	    mNUMBER_SIGN(); 

            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);

            mCIRCUMFLEX(); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:27: ( 'ints' | 'floats' | 'longs' | 'doubles' | 'objects' | NAME | STRING )*
            loop18:
            do {
                int alt18=8;
                switch ( input.LA(1) ) {
                case 'i':
                    {
                    int LA18_2 = input.LA(2);

                    if ( (LA18_2=='n') ) {
                        int LA18_9 = input.LA(3);

                        if ( (LA18_9=='t') ) {
                            int LA18_14 = input.LA(4);

                            if ( (LA18_14=='s') ) {
                                alt18=1;
                            }

                            else {
                                alt18=6;
                            }

                        }

                        else {
                            alt18=6;
                        }

                    }

                    else {
                        alt18=6;
                    }

                    }
                    break;
                case 'f':
                    {
                    int LA18_3 = input.LA(2);

                    if ( (LA18_3=='l') ) {
                        int LA18_10 = input.LA(3);

                        if ( (LA18_10=='o') ) {
                            int LA18_15 = input.LA(4);

                            if ( (LA18_15=='a') ) {
                                int LA18_20 = input.LA(5);

                                if ( (LA18_20=='t') ) {
                                    int LA18_24 = input.LA(6);

                                    if ( (LA18_24=='s') ) {
                                        alt18=2;
                                    }

                                    else {
                                        alt18=6;
                                    }

                                }

                                else {
                                    alt18=6;
                                }

                            }

                            else {
                                alt18=6;
                            }

                        }

                        else {
                            alt18=6;
                        }

                    }

                    else {
                        alt18=6;
                    }

                    }
                    break;
                case 'l':
                    {
                    int LA18_4 = input.LA(2);

                    if ( (LA18_4=='o') ) {
                        int LA18_11 = input.LA(3);

                        if ( (LA18_11=='n') ) {
                            int LA18_16 = input.LA(4);

                            if ( (LA18_16=='g') ) {
                                int LA18_21 = input.LA(5);

                                if ( (LA18_21=='s') ) {
                                    alt18=3;
                                }

                                else {
                                    alt18=6;
                                }

                            }

                            else {
                                alt18=6;
                            }

                        }

                        else {
                            alt18=6;
                        }

                    }

                    else {
                        alt18=6;
                    }

                    }
                    break;
                case 'd':
                    {
                    int LA18_5 = input.LA(2);

                    if ( (LA18_5=='o') ) {
                        int LA18_12 = input.LA(3);

                        if ( (LA18_12=='u') ) {
                            int LA18_17 = input.LA(4);

                            if ( (LA18_17=='b') ) {
                                int LA18_22 = input.LA(5);

                                if ( (LA18_22=='l') ) {
                                    int LA18_26 = input.LA(6);

                                    if ( (LA18_26=='e') ) {
                                        int LA18_29 = input.LA(7);

                                        if ( (LA18_29=='s') ) {
                                            alt18=4;
                                        }

                                        else {
                                            alt18=6;
                                        }

                                    }

                                    else {
                                        alt18=6;
                                    }

                                }

                                else {
                                    alt18=6;
                                }

                            }

                            else {
                                alt18=6;
                            }

                        }

                        else {
                            alt18=6;
                        }

                    }

                    else {
                        alt18=6;
                    }

                    }
                    break;
                case 'o':
                    {
                    int LA18_6 = input.LA(2);

                    if ( (LA18_6=='b') ) {
                        int LA18_13 = input.LA(3);

                        if ( (LA18_13=='j') ) {
                            int LA18_18 = input.LA(4);

                            if ( (LA18_18=='e') ) {
                                int LA18_23 = input.LA(5);

                                if ( (LA18_23=='c') ) {
                                    int LA18_27 = input.LA(6);

                                    if ( (LA18_27=='t') ) {
                                        int LA18_30 = input.LA(7);

                                        if ( (LA18_30=='s') ) {
                                            alt18=5;
                                        }

                                        else {
                                            alt18=6;
                                        }

                                    }

                                    else {
                                        alt18=6;
                                    }

                                }

                                else {
                                    alt18=6;
                                }

                            }

                            else {
                                alt18=6;
                            }

                        }

                        else {
                            alt18=6;
                        }

                    }

                    else {
                        alt18=6;
                    }

                    }
                    break;
                case '!':
                case '$':
                case '*':
                case '+':
                case '-':
                case '<':
                case '=':
                case '>':
                case '?':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '_':
                case 'a':
                case 'b':
                case 'c':
                case 'e':
                case 'g':
                case 'h':
                case 'j':
                case 'k':
                case 'm':
                case 'n':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt18=6;
                    }
                    break;
                case '\"':
                    {
                    alt18=7;
                    }
                    break;

                }

                switch (alt18) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:29: 'ints'
            	    {
            	    match("ints"); 


            	    }
            	    break;
            	case 2 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:38: 'floats'
            	    {
            	    match("floats"); 


            	    }
            	    break;
            	case 3 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:49: 'longs'
            	    {
            	    match("longs"); 


            	    }
            	    break;
            	case 4 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:59: 'doubles'
            	    {
            	    match("doubles"); 


            	    }
            	    break;
            	case 5 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:71: 'objects'
            	    {
            	    match("objects"); 


            	    }
            	    break;
            	case 6 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:83: NAME
            	    {
            	    mNAME(); 

            	    }
            	    break;
            	case 7 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:131:90: STRING
            	    {
            	    mSTRING(); 

            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);


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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:135:9: ( SYMBOL_HEAD ( SYMBOL_REST )* ( ':' ( SYMBOL_REST )+ )* )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:135:9: SYMBOL_HEAD ( SYMBOL_REST )* ( ':' ( SYMBOL_REST )+ )*
            {
            mSYMBOL_HEAD(); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:135:21: ( SYMBOL_REST )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0=='!'||(LA19_0>='#' && LA19_0<='$')||(LA19_0>='*' && LA19_0<='+')||(LA19_0>='-' && LA19_0<='.')||(LA19_0>='0' && LA19_0<='9')||(LA19_0>='<' && LA19_0<='?')||(LA19_0>='A' && LA19_0<='Z')||LA19_0=='_'||(LA19_0>='a' && LA19_0<='z')) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:135:21: SYMBOL_REST
            	    {
            	    mSYMBOL_REST(); 

            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:135:34: ( ':' ( SYMBOL_REST )+ )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==':') ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:135:35: ':' ( SYMBOL_REST )+
            	    {
            	    match(':'); 
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:135:39: ( SYMBOL_REST )+
            	    int cnt20=0;
            	    loop20:
            	    do {
            	        int alt20=2;
            	        int LA20_0 = input.LA(1);

            	        if ( (LA20_0=='!'||(LA20_0>='#' && LA20_0<='$')||(LA20_0>='*' && LA20_0<='+')||(LA20_0>='-' && LA20_0<='.')||(LA20_0>='0' && LA20_0<='9')||(LA20_0>='<' && LA20_0<='?')||(LA20_0>='A' && LA20_0<='Z')||LA20_0=='_'||(LA20_0>='a' && LA20_0<='z')) ) {
            	            alt20=1;
            	        }


            	        switch (alt20) {
            	    	case 1 :
            	    	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:135:39: SYMBOL_REST
            	    	    {
            	    	    mSYMBOL_REST(); 

            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt20 >= 1 ) break loop20;
            	                EarlyExitException eee =
            	                    new EarlyExitException(20, input);
            	                throw eee;
            	        }
            	        cnt20++;
            	    } while (true);


            	    }
            	    break;

            	default :
            	    break loop21;
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:140:9: ( 'a' .. 'z' | 'A' .. 'Z' | '*' | '+' | '!' | '-' | '_' | '?' | '>' | '<' | '=' | '$' )
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:146:9: ( SYMBOL_HEAD | '0' .. '9' | '.' | NUMBER_SIGN )
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:162:9: ( ':' SYMBOL )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:162:9: ':' SYMBOL
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:166:9: ( '`' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:166:9: '`'
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:170:9: ( '~@' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:170:9: '~@'
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:9: ( '~' )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:174:9: '~'
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:178:9: ( ';' (~ ( '\\r' | '\\n' ) )* ( ( '\\r' )? '\\n' )? )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:178:9: ';' (~ ( '\\r' | '\\n' ) )* ( ( '\\r' )? '\\n' )?
            {
            match(';'); 
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:178:13: (~ ( '\\r' | '\\n' ) )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( ((LA22_0>='\u0000' && LA22_0<='\t')||(LA22_0>='\u000B' && LA22_0<='\f')||(LA22_0>='\u000E' && LA22_0<='\uFFFE')) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:178:13: ~ ( '\\r' | '\\n' )
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
            	    break loop22;
                }
            } while (true);

            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:178:29: ( ( '\\r' )? '\\n' )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0=='\n'||LA24_0=='\r') ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:178:30: ( '\\r' )? '\\n'
                    {
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:178:30: ( '\\r' )?
                    int alt23=2;
                    int LA23_0 = input.LA(1);

                    if ( (LA23_0=='\r') ) {
                        alt23=1;
                    }
                    switch (alt23) {
                        case 1 :
                            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:178:30: '\\r'
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:181:9: ( ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+ )
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:181:9: ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+
            {
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:181:9: ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+
            int cnt25=0;
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( ((LA25_0>='\t' && LA25_0<='\n')||LA25_0=='\r'||LA25_0==' '||LA25_0==',') ) {
                    alt25=1;
                }


                switch (alt25) {
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
            	    if ( cnt25 >= 1 ) break loop25;
                        EarlyExitException eee =
                            new EarlyExitException(25, input);
                        throw eee;
                }
                cnt25++;
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
            // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:186:9: ( '%' '1' .. '9' ( '0' .. '9' )* | '%&' | '%' )
            int alt27=3;
            int LA27_0 = input.LA(1);

            if ( (LA27_0=='%') ) {
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
                    alt27=1;
                    }
                    break;
                case '&':
                    {
                    alt27=2;
                    }
                    break;
                default:
                    alt27=3;}

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("185:1: LAMBDA_ARG : ( '%' '1' .. '9' ( '0' .. '9' )* | '%&' | '%' );", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:186:9: '%' '1' .. '9' ( '0' .. '9' )*
                    {
                    match('%'); 
                    matchRange('1','9'); 
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:186:22: ( '0' .. '9' )*
                    loop26:
                    do {
                        int alt26=2;
                        int LA26_0 = input.LA(1);

                        if ( ((LA26_0>='0' && LA26_0<='9')) ) {
                            alt26=1;
                        }


                        switch (alt26) {
                    	case 1 :
                    	    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:186:22: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:187:9: '%&'
                    {
                    match("%&"); 


                    }
                    break;
                case 3 :
                    // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:188:9: '%'
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
        // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:10: ( OPEN_PAREN | CLOSE_PAREN | AMPERSAND | LEFT_SQUARE_BRACKET | RIGHT_SQUARE_BRACKET | LEFT_CURLY_BRACKET | RIGHT_CURLY_BRACKET | BACKSLASH | CIRCUMFLEX | COMMERCIAL_AT | NUMBER_SIGN | APOSTROPHE | SPECIAL_FORM | STRING | REGEX_LITERAL | NUMBER | CHARACTER | HEXDIGIT | NIL | BOOLEAN | SYMBOL | METADATA_TYPEHINT | KEYWORD | SYNTAX_QUOTE | UNQUOTE_SPLICING | UNQUOTE | COMMENT | SPACE | LAMBDA_ARG )
        int alt28=29;
        alt28 = dfa28.predict(input);
        switch (alt28) {
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
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:201: REGEX_LITERAL
                {
                mREGEX_LITERAL(); 

                }
                break;
            case 16 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:215: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 17 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:222: CHARACTER
                {
                mCHARACTER(); 

                }
                break;
            case 18 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:232: HEXDIGIT
                {
                mHEXDIGIT(); 

                }
                break;
            case 19 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:241: NIL
                {
                mNIL(); 

                }
                break;
            case 20 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:245: BOOLEAN
                {
                mBOOLEAN(); 

                }
                break;
            case 21 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:253: SYMBOL
                {
                mSYMBOL(); 

                }
                break;
            case 22 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:260: METADATA_TYPEHINT
                {
                mMETADATA_TYPEHINT(); 

                }
                break;
            case 23 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:278: KEYWORD
                {
                mKEYWORD(); 

                }
                break;
            case 24 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:286: SYNTAX_QUOTE
                {
                mSYNTAX_QUOTE(); 

                }
                break;
            case 25 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:299: UNQUOTE_SPLICING
                {
                mUNQUOTE_SPLICING(); 

                }
                break;
            case 26 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:316: UNQUOTE
                {
                mUNQUOTE(); 

                }
                break;
            case 27 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:324: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 28 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:332: SPACE
                {
                mSPACE(); 

                }
                break;
            case 29 :
                // /home/lpetit/projects/ccw/clojure-antlr-grammar/src/Clojure.g:1:338: LAMBDA_ARG
                {
                mLAMBDA_ARG(); 

                }
                break;

        }

    }


    protected DFA28 dfa28 = new DFA28(this);
    static final String DFA28_eotS =
        "\10\uffff\1\45\1\46\1\uffff\1\50\1\uffff\1\54\4\35\1\54\5\35\2\uffff"+
        "\1\35\1\uffff\1\54\3\uffff\1\76\11\uffff\1\30\1\35\1\uffff\1\30"+
        "\5\35\1\30\7\35\1\74\3\uffff\2\30\2\35\1\30\4\35\1\30\1\35\1\30"+
        "\1\126\3\35\1\30\4\35\1\137\1\35\1\uffff\1\30\1\74\1\35\1\74\1\30"+
        "\1\137\2\30\1\uffff\12\35\2\30";
    static final String DFA28_eofS =
        "\154\uffff";
    static final String DFA28_minS =
        "\1\11\7\uffff\1\0\1\41\1\uffff\1\42\1\uffff\1\41\1\146\1\145\1\165"+
        "\1\141\1\41\1\145\1\150\1\157\2\145\2\uffff\1\60\1\uffff\1\41\3"+
        "\uffff\1\100\11\uffff\1\41\1\146\1\uffff\1\41\1\164\2\157\1\162"+
        "\1\154\1\41\1\143\1\162\1\165\1\156\1\167\1\154\1\164\1\41\3\uffff"+
        "\2\41\1\160\1\164\1\41\1\163\1\165\1\157\1\145\1\41\1\151\3\41\1"+
        "\60\1\55\1\41\2\145\1\162\1\167\1\41\1\164\1\uffff\2\41\1\60\5\41"+
        "\1\uffff\1\157\1\162\1\55\1\145\1\156\1\164\1\151\1\145\1\164\1"+
        "\162\2\41";
    static final String DFA28_maxS =
        "\1\176\7\uffff\1\ufffe\1\172\1\uffff\1\136\1\uffff\1\172\1\146\1"+
        "\157\1\165\1\141\1\172\1\145\1\162\1\157\1\151\1\145\2\uffff\1\71"+
        "\1\uffff\1\172\3\uffff\1\100\11\uffff\1\172\1\146\1\uffff\1\172"+
        "\1\164\2\157\1\162\1\154\1\172\1\143\1\162\1\171\1\156\1\167\1\154"+
        "\1\164\1\172\3\uffff\2\172\1\160\1\164\1\172\1\163\1\165\1\157\1"+
        "\145\1\172\1\151\2\172\1\41\2\71\1\172\2\145\1\162\1\167\1\172\1"+
        "\164\1\uffff\2\172\1\71\5\172\1\uffff\1\157\1\162\1\55\1\145\1\170"+
        "\1\164\1\151\1\145\1\164\1\162\2\172";
    static final String DFA28_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\2\uffff\1\12\1\uffff\1\14\13"+
        "\uffff\1\15\1\16\1\uffff\1\20\1\uffff\1\25\1\27\1\30\1\uffff\1\33"+
        "\1\34\1\35\1\21\1\10\1\11\1\26\1\13\1\17\2\uffff\1\22\17\uffff\1"+
        "\20\1\31\1\32\27\uffff\1\23\10\uffff\1\24\14\uffff";
    static final String DFA28_specialS =
        "\154\uffff}>";
    static final String[] DFA28_transitionS = {
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
            "\2\47\1\uffff\1\47\5\uffff\2\47\1\uffff\1\47\16\uffff\4\47\1"+
            "\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\51\1\47\72\uffff\1\47",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\4\35\1\53\11\35\1\52\13\35",
            "\1\55",
            "\1\56\11\uffff\1\57",
            "\1\60",
            "\1\61",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\1\62\14\35\1\63\14\35",
            "\1\64",
            "\1\65\11\uffff\1\66",
            "\1\67",
            "\1\70\3\uffff\1\71",
            "\1\72",
            "",
            "",
            "\12\73",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "",
            "",
            "\1\75",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\77",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\100",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\105",
            "\1\106",
            "\1\107\3\uffff\1\110",
            "\1\111",
            "\1\112",
            "\1\113",
            "\1\114",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\1\35\1\115\1\35\12\73"+
            "\1\35\1\uffff\4\35\1\uffff\4\35\1\116\25\35\4\uffff\1\35\1\uffff"+
            "\4\35\1\116\25\35",
            "",
            "",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\117",
            "\1\120",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\121",
            "\1\122",
            "\1\123",
            "\1\124",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\125",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\127",
            "\12\130",
            "\1\131\2\uffff\12\132",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\133",
            "\1\134",
            "\1\135",
            "\1\136",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\140",
            "",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\3\35\12\130\1\35\1\uffff"+
            "\4\35\1\uffff\4\35\1\116\25\35\4\uffff\1\35\1\uffff\4\35\1\116"+
            "\25\35",
            "\12\132",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\3\35\12\132\1\35\1\uffff"+
            "\4\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "\1\141",
            "\1\142",
            "\1\143",
            "\1\144",
            "\1\145\11\uffff\1\146",
            "\1\147",
            "\1\150",
            "\1\151",
            "\1\152",
            "\1\153",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35"
    };

    static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
    static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
    static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
    static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
    static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
    static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
    static final short[][] DFA28_transition;

    static {
        int numStates = DFA28_transitionS.length;
        DFA28_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
        }
    }

    class DFA28 extends DFA {

        public DFA28(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 28;
            this.eot = DFA28_eot;
            this.eof = DFA28_eof;
            this.min = DFA28_min;
            this.max = DFA28_max;
            this.accept = DFA28_accept;
            this.special = DFA28_special;
            this.transition = DFA28_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( OPEN_PAREN | CLOSE_PAREN | AMPERSAND | LEFT_SQUARE_BRACKET | RIGHT_SQUARE_BRACKET | LEFT_CURLY_BRACKET | RIGHT_CURLY_BRACKET | BACKSLASH | CIRCUMFLEX | COMMERCIAL_AT | NUMBER_SIGN | APOSTROPHE | SPECIAL_FORM | STRING | REGEX_LITERAL | NUMBER | CHARACTER | HEXDIGIT | NIL | BOOLEAN | SYMBOL | METADATA_TYPEHINT | KEYWORD | SYNTAX_QUOTE | UNQUOTE_SPLICING | UNQUOTE | COMMENT | SPACE | LAMBDA_ARG );";
        }
    }
 

}