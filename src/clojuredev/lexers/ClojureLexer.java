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
package clojuredev.lexers;
//$ANTLR 3.0.1 o:/clojure/basic-clojure-grammar/src/Clojure.g 2009-03-21 01:14:28

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;

public class ClojureLexer extends Lexer {
    public static final int SYNTAX_QUOTE=18;
    public static final int KEYWORD=17;
    public static final int SYMBOL=13;
    public static final int METADATA_TYPEHINT=14;
    public static final int SYMBOL_HEAD=15;
    public static final int NUMBER=8;
    public static final int OPEN_PAREN=4;
    public static final int T29=29;
    public static final int T28=28;
    public static final int T27=27;
    public static final int T26=26;
    public static final int T25=25;
    public static final int Tokens=33;
    public static final int T24=24;
    public static final int EOF=-1;
    public static final int SPACE=22;
    public static final int CHARACTER=9;
    public static final int NAME=12;
    public static final int BOOLEAN=11;
    public static final int NIL=10;
    public static final int UNQUOTE=20;
    public static final int LAMBDA_ARG=23;
    public static final int SPECIAL_FORM=6;
    public static final int CLOSE_PAREN=5;
    public static final int SYMBOL_REST=16;
    public static final int COMMENT=21;
    public static final int T30=30;
    public static final int UNQUOTE_SPLICING=19;
    public static final int T32=32;
    public static final int STRING=7;
    public static final int T31=31;
    public ClojureLexer() {;} 
    public ClojureLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "o:/clojure/basic-clojure-grammar/src/Clojure.g"; }

    // $ANTLR start T24
    public final void mT24() throws RecognitionException {
        try {
            int _type = T24;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:3:5: ( '&' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:3:7: '&'
            {
            match('&'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T24

    // $ANTLR start T25
    public final void mT25() throws RecognitionException {
        try {
            int _type = T25;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:4:5: ( '[' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:4:7: '['
            {
            match('['); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T25

    // $ANTLR start T26
    public final void mT26() throws RecognitionException {
        try {
            int _type = T26;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:5:5: ( ']' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:5:7: ']'
            {
            match(']'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T26

    // $ANTLR start T27
    public final void mT27() throws RecognitionException {
        try {
            int _type = T27;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:6:5: ( '{' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:6:7: '{'
            {
            match('{'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T27

    // $ANTLR start T28
    public final void mT28() throws RecognitionException {
        try {
            int _type = T28;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:7:5: ( '}' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:7:7: '}'
            {
            match('}'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T28

    // $ANTLR start T29
    public final void mT29() throws RecognitionException {
        try {
            int _type = T29;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:8:5: ( '\\'' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:8:7: '\\''
            {
            match('\''); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T29

    // $ANTLR start T30
    public final void mT30() throws RecognitionException {
        try {
            int _type = T30;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:9:5: ( '^' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:9:7: '^'
            {
            match('^'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T30

    // $ANTLR start T31
    public final void mT31() throws RecognitionException {
        try {
            int _type = T31;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:10:5: ( '@' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:10:7: '@'
            {
            match('@'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T31

    // $ANTLR start T32
    public final void mT32() throws RecognitionException {
        try {
            int _type = T32;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:11:5: ( '#' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:11:7: '#'
            {
            match('#'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T32

    // $ANTLR start OPEN_PAREN
    public final void mOPEN_PAREN() throws RecognitionException {
        try {
            int _type = OPEN_PAREN;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:39:11: ( '(' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:39:13: '('
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:41:12: ( ')' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:41:14: ')'
            {
            match(')'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CLOSE_PAREN

    // $ANTLR start SPECIAL_FORM
    public final void mSPECIAL_FORM() throws RecognitionException {
        try {
            int _type = SPECIAL_FORM;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:45:13: ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' )
            int alt1=16;
            switch ( input.LA(1) ) {
            case 'd':
                {
                int LA1_1 = input.LA(2);

                if ( (LA1_1=='o') ) {
                    alt1=3;
                }
                else if ( (LA1_1=='e') ) {
                    alt1=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 1, input);

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
                        new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 3, input);

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

                if ( (LA1_8=='h') ) {
                    alt1=10;
                }
                else if ( (LA1_8=='r') ) {
                    alt1=11;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 8, input);

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
                                                        new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 26, input);

                                                    throw nvae;
                                                }
                                            }
                                            else {
                                                NoViableAltException nvae =
                                                    new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 25, input);

                                                throw nvae;
                                            }
                                        }
                                        else {
                                            NoViableAltException nvae =
                                                new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 24, input);

                                            throw nvae;
                                        }
                                    }
                                    else {
                                        NoViableAltException nvae =
                                            new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 23, input);

                                        throw nvae;
                                    }
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 22, input);

                                    throw nvae;
                                }
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 21, input);

                                throw nvae;
                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 20, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 19, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 9, input);

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
                    new NoViableAltException("45:1: SPECIAL_FORM : ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' );", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:45:15: 'def'
                    {
                    match("def"); 


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:45:23: 'if'
                    {
                    match("if"); 


                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:45:30: 'do'
                    {
                    match("do"); 


                    }
                    break;
                case 4 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:45:37: 'let'
                    {
                    match("let"); 


                    }
                    break;
                case 5 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:45:45: 'quote'
                    {
                    match("quote"); 


                    }
                    break;
                case 6 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:45:55: 'var'
                    {
                    match("var"); 


                    }
                    break;
                case 7 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:45:63: 'fn'
                    {
                    match("fn"); 


                    }
                    break;
                case 8 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:45:70: 'loop'
                    {
                    match("loop"); 


                    }
                    break;
                case 9 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:46:13: 'recur'
                    {
                    match("recur"); 


                    }
                    break;
                case 10 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:46:23: 'throw'
                    {
                    match("throw"); 


                    }
                    break;
                case 11 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:46:33: 'try'
                    {
                    match("try"); 


                    }
                    break;
                case 12 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:46:41: 'monitor-enter'
                    {
                    match("monitor-enter"); 


                    }
                    break;
                case 13 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:46:59: 'monitor-exit'
                    {
                    match("monitor-exit"); 


                    }
                    break;
                case 14 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:47:13: 'new'
                    {
                    match("new"); 


                    }
                    break;
                case 15 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:47:21: 'set!'
                    {
                    match("set!"); 


                    }
                    break;
                case 16 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:47:30: '.'
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:52:7: ( '\"' (~ '\"' | ( '\\\\' '\"' ) )* '\"' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:52:9: '\"' (~ '\"' | ( '\\\\' '\"' ) )* '\"'
            {
            match('\"'); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:52:13: (~ '\"' | ( '\\\\' '\"' ) )*
            loop2:
            do {
                int alt2=3;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='\\') ) {
                    int LA2_2 = input.LA(2);

                    if ( (LA2_2=='\"') ) {
                        int LA2_4 = input.LA(3);

                        if ( ((LA2_4>='\u0000' && LA2_4<='\uFFFE')) ) {
                            alt2=2;
                        }

                        else {
                            alt2=1;
                        }

                    }
                    else if ( ((LA2_2>='\u0000' && LA2_2<='!')||(LA2_2>='#' && LA2_2<='\uFFFE')) ) {
                        alt2=1;
                    }


                }
                else if ( ((LA2_0>='\u0000' && LA2_0<='!')||(LA2_0>='#' && LA2_0<='[')||(LA2_0>=']' && LA2_0<='\uFFFE')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:52:15: ~ '\"'
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFE') ) {
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
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:52:22: ( '\\\\' '\"' )
            	    {
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:52:22: ( '\\\\' '\"' )
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:52:23: '\\\\' '\"'
            	    {
            	    match('\\'); 
            	    match('\"'); 

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

    // $ANTLR start NUMBER
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:7: ( ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )? )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:9: ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )?
            {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:9: ( '-' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='-') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:9: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:14: ( '0' .. '9' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:14: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);

            // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:24: ( '.' ( '0' .. '9' )+ )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='.') ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:25: '.' ( '0' .. '9' )+
                    {
                    match('.'); 
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:29: ( '0' .. '9' )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:29: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt5 >= 1 ) break loop5;
                                EarlyExitException eee =
                                    new EarlyExitException(5, input);
                                throw eee;
                        }
                        cnt5++;
                    } while (true);


                    }
                    break;

            }

            // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:41: ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='E'||LA9_0=='e') ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:42: ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+
                    {
                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }

                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:52: ( '-' )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0=='-') ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:52: '-'
                            {
                            match('-'); 

                            }
                            break;

                    }

                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:57: ( '0' .. '9' )+
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
                    	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:57:57: '0' .. '9'
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:60:10: ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\' . )
            int alt10=4;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='\\') ) {
                int LA10_1 = input.LA(2);

                if ( (LA10_1=='n') ) {
                    int LA10_2 = input.LA(3);

                    if ( (LA10_2=='e') ) {
                        alt10=1;
                    }
                    else {
                        alt10=4;}
                }
                else if ( (LA10_1=='s') ) {
                    int LA10_3 = input.LA(3);

                    if ( (LA10_3=='p') ) {
                        alt10=2;
                    }
                    else {
                        alt10=4;}
                }
                else if ( (LA10_1=='t') ) {
                    int LA10_4 = input.LA(3);

                    if ( (LA10_4=='a') ) {
                        alt10=3;
                    }
                    else {
                        alt10=4;}
                }
                else if ( ((LA10_1>='\u0000' && LA10_1<='m')||(LA10_1>='o' && LA10_1<='r')||(LA10_1>='u' && LA10_1<='\uFFFE')) ) {
                    alt10=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("60:1: CHARACTER : ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\' . );", 10, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("60:1: CHARACTER : ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\' . );", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:61:9: '\\\\newline'
                    {
                    match("\\newline"); 


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:62:9: '\\\\space'
                    {
                    match("\\space"); 


                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:63:9: '\\\\tab'
                    {
                    match("\\tab"); 


                    }
                    break;
                case 4 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:64:9: '\\\\' .
                    {
                    match('\\'); 
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

    // $ANTLR start NIL
    public final void mNIL() throws RecognitionException {
        try {
            int _type = NIL;
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:67:4: ( 'nil' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:67:9: 'nil'
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:70:8: ( 'true' | 'false' )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='t') ) {
                alt11=1;
            }
            else if ( (LA11_0=='f') ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("70:1: BOOLEAN : ( 'true' | 'false' );", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:71:9: 'true'
                    {
                    match("true"); 


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:72:9: 'false'
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:75:7: ( '/' | NAME ( '/' NAME )? )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0=='/') ) {
                alt13=1;
            }
            else if ( (LA13_0=='!'||LA13_0=='$'||(LA13_0>='*' && LA13_0<='+')||LA13_0=='-'||(LA13_0>='<' && LA13_0<='?')||(LA13_0>='A' && LA13_0<='Z')||LA13_0=='_'||(LA13_0>='a' && LA13_0<='z')) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("75:1: SYMBOL : ( '/' | NAME ( '/' NAME )? );", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:76:9: '/'
                    {
                    match('/'); 

                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:77:9: NAME ( '/' NAME )?
                    {
                    mNAME(); 
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:77:14: ( '/' NAME )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0=='/') ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:77:15: '/' NAME
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:80:18: ( '#' '^' NAME )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:81:3: '#' '^' NAME
            {
            match('#'); 
            match('^'); 
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:85:5: ( SYMBOL_HEAD ( SYMBOL_REST )* ( ':' ( SYMBOL_REST )+ )* )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:85:9: SYMBOL_HEAD ( SYMBOL_REST )* ( ':' ( SYMBOL_REST )+ )*
            {
            mSYMBOL_HEAD(); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:85:21: ( SYMBOL_REST )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0=='!'||(LA14_0>='#' && LA14_0<='$')||(LA14_0>='*' && LA14_0<='+')||(LA14_0>='-' && LA14_0<='.')||(LA14_0>='0' && LA14_0<='9')||(LA14_0>='<' && LA14_0<='?')||(LA14_0>='A' && LA14_0<='Z')||LA14_0=='_'||(LA14_0>='a' && LA14_0<='z')) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:85:21: SYMBOL_REST
            	    {
            	    mSYMBOL_REST(); 

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            // o:/clojure/basic-clojure-grammar/src/Clojure.g:85:34: ( ':' ( SYMBOL_REST )+ )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==':') ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:85:35: ':' ( SYMBOL_REST )+
            	    {
            	    match(':'); 
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:85:39: ( SYMBOL_REST )+
            	    int cnt15=0;
            	    loop15:
            	    do {
            	        int alt15=2;
            	        int LA15_0 = input.LA(1);

            	        if ( (LA15_0=='!'||(LA15_0>='#' && LA15_0<='$')||(LA15_0>='*' && LA15_0<='+')||(LA15_0>='-' && LA15_0<='.')||(LA15_0>='0' && LA15_0<='9')||(LA15_0>='<' && LA15_0<='?')||(LA15_0>='A' && LA15_0<='Z')||LA15_0=='_'||(LA15_0>='a' && LA15_0<='z')) ) {
            	            alt15=1;
            	        }


            	        switch (alt15) {
            	    	case 1 :
            	    	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:85:39: SYMBOL_REST
            	    	    {
            	    	    mSYMBOL_REST(); 

            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt15 >= 1 ) break loop15;
            	                EarlyExitException eee =
            	                    new EarlyExitException(15, input);
            	                throw eee;
            	        }
            	        cnt15++;
            	    } while (true);


            	    }
            	    break;

            	default :
            	    break loop16;
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:89:12: ( 'a' .. 'z' | 'A' .. 'Z' | '*' | '+' | '!' | '-' | '_' | '?' | '>' | '<' | '=' | '$' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:95:12: ( SYMBOL_HEAD | '0' .. '9' | '.' | '#' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:111:8: ( ':' SYMBOL )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:112:9: ':' SYMBOL
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:115:13: ( '`' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:116:9: '`'
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:119:17: ( '~@' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:120:9: '~@'
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:123:8: ( '~' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:124:9: '~'
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:127:8: ( ';' (~ ( '\\r' | '\\n' ) )* ( ( '\\r' )? '\\n' )? )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:128:9: ';' (~ ( '\\r' | '\\n' ) )* ( ( '\\r' )? '\\n' )?
            {
            match(';'); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:128:13: (~ ( '\\r' | '\\n' ) )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( ((LA17_0>='\u0000' && LA17_0<='\t')||(LA17_0>='\u000B' && LA17_0<='\f')||(LA17_0>='\u000E' && LA17_0<='\uFFFE')) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:128:13: ~ ( '\\r' | '\\n' )
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
            	    break loop17;
                }
            } while (true);

            // o:/clojure/basic-clojure-grammar/src/Clojure.g:128:29: ( ( '\\r' )? '\\n' )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0=='\n'||LA19_0=='\r') ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:128:30: ( '\\r' )? '\\n'
                    {
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:128:30: ( '\\r' )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0=='\r') ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:128:30: '\\r'
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:131:6: ( ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+ )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:131:9: ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+
            {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:131:9: ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+
            int cnt20=0;
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( ((LA20_0>='\t' && LA20_0<='\n')||LA20_0=='\r'||LA20_0==' '||LA20_0==',') ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:
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
            	    if ( cnt20 >= 1 ) break loop20;
                        EarlyExitException eee =
                            new EarlyExitException(20, input);
                        throw eee;
                }
                cnt20++;
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
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:135:11: ( '%' '1' .. '9' ( '0' .. '9' )* | '%&' | '%' )
            int alt22=3;
            int LA22_0 = input.LA(1);

            if ( (LA22_0=='%') ) {
                switch ( input.LA(2) ) {
                case '&':
                    {
                    alt22=2;
                    }
                    break;
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
                    alt22=1;
                    }
                    break;
                default:
                    alt22=3;}

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("135:1: LAMBDA_ARG : ( '%' '1' .. '9' ( '0' .. '9' )* | '%&' | '%' );", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:136:9: '%' '1' .. '9' ( '0' .. '9' )*
                    {
                    match('%'); 
                    matchRange('1','9'); 
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:136:22: ( '0' .. '9' )*
                    loop21:
                    do {
                        int alt21=2;
                        int LA21_0 = input.LA(1);

                        if ( ((LA21_0>='0' && LA21_0<='9')) ) {
                            alt21=1;
                        }


                        switch (alt21) {
                    	case 1 :
                    	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:136:22: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop21;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:137:9: '%&'
                    {
                    match("%&"); 


                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:138:9: '%'
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
        // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:8: ( T24 | T25 | T26 | T27 | T28 | T29 | T30 | T31 | T32 | OPEN_PAREN | CLOSE_PAREN | SPECIAL_FORM | STRING | NUMBER | CHARACTER | NIL | BOOLEAN | SYMBOL | METADATA_TYPEHINT | KEYWORD | SYNTAX_QUOTE | UNQUOTE_SPLICING | UNQUOTE | COMMENT | SPACE | LAMBDA_ARG )
        int alt23=26;
        alt23 = dfa23.predict(input);
        switch (alt23) {
            case 1 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:10: T24
                {
                mT24(); 

                }
                break;
            case 2 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:14: T25
                {
                mT25(); 

                }
                break;
            case 3 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:18: T26
                {
                mT26(); 

                }
                break;
            case 4 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:22: T27
                {
                mT27(); 

                }
                break;
            case 5 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:26: T28
                {
                mT28(); 

                }
                break;
            case 6 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:30: T29
                {
                mT29(); 

                }
                break;
            case 7 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:34: T30
                {
                mT30(); 

                }
                break;
            case 8 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:38: T31
                {
                mT31(); 

                }
                break;
            case 9 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:42: T32
                {
                mT32(); 

                }
                break;
            case 10 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:46: OPEN_PAREN
                {
                mOPEN_PAREN(); 

                }
                break;
            case 11 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:57: CLOSE_PAREN
                {
                mCLOSE_PAREN(); 

                }
                break;
            case 12 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:69: SPECIAL_FORM
                {
                mSPECIAL_FORM(); 

                }
                break;
            case 13 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:82: STRING
                {
                mSTRING(); 

                }
                break;
            case 14 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:89: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 15 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:96: CHARACTER
                {
                mCHARACTER(); 

                }
                break;
            case 16 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:106: NIL
                {
                mNIL(); 

                }
                break;
            case 17 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:110: BOOLEAN
                {
                mBOOLEAN(); 

                }
                break;
            case 18 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:118: SYMBOL
                {
                mSYMBOL(); 

                }
                break;
            case 19 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:125: METADATA_TYPEHINT
                {
                mMETADATA_TYPEHINT(); 

                }
                break;
            case 20 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:143: KEYWORD
                {
                mKEYWORD(); 

                }
                break;
            case 21 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:151: SYNTAX_QUOTE
                {
                mSYNTAX_QUOTE(); 

                }
                break;
            case 22 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:164: UNQUOTE_SPLICING
                {
                mUNQUOTE_SPLICING(); 

                }
                break;
            case 23 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:181: UNQUOTE
                {
                mUNQUOTE(); 

                }
                break;
            case 24 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:189: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 25 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:197: SPACE
                {
                mSPACE(); 

                }
                break;
            case 26 :
                // o:/clojure/basic-clojure-grammar/src/Clojure.g:1:203: LAMBDA_ARG
                {
                mLAMBDA_ARG(); 

                }
                break;

        }

    }


    protected DFA23 dfa23 = new DFA23(this);
    static final String DFA23_eotS =
        "\11\uffff\1\44\2\uffff\13\34\2\uffff\1\34\5\uffff\1\67\5\uffff\1"+
        "\34\2\27\4\34\1\27\10\34\1\32\2\uffff\2\27\2\34\1\27\4\34\1\27\1"+
        "\34\1\27\1\117\3\34\1\27\4\34\1\130\1\34\1\uffff\1\27\1\32\1\34"+
        "\1\32\1\27\1\130\2\27\1\uffff\12\34\2\27";
    static final String DFA23_eofS =
        "\145\uffff";
    static final String DFA23_minS =
        "\1\11\10\uffff\1\136\2\uffff\1\145\1\146\1\145\1\165\2\141\1\145"+
        "\1\150\1\157\2\145\2\uffff\1\60\5\uffff\1\100\5\uffff\1\146\2\41"+
        "\1\164\2\157\1\162\1\41\1\154\1\143\1\162\1\165\1\156\1\167\1\154"+
        "\1\164\1\41\2\uffff\2\41\1\160\1\164\1\41\1\163\1\165\1\157\1\145"+
        "\1\41\1\151\3\41\1\60\1\55\1\41\2\145\1\162\1\167\1\41\1\164\1\uffff"+
        "\2\41\1\60\5\41\1\uffff\1\157\1\162\1\55\1\145\1\156\1\164\1\151"+
        "\1\145\1\164\1\162\2\41";
    static final String DFA23_maxS =
        "\1\176\10\uffff\1\136\2\uffff\1\157\1\146\1\157\1\165\1\141\1\156"+
        "\1\145\1\162\1\157\1\151\1\145\2\uffff\1\71\5\uffff\1\100\5\uffff"+
        "\1\146\2\172\1\164\2\157\1\162\1\172\1\154\1\143\1\162\1\171\1\156"+
        "\1\167\1\154\1\164\1\172\2\uffff\2\172\1\160\1\164\1\172\1\163\1"+
        "\165\1\157\1\145\1\172\1\151\2\172\1\41\2\71\1\172\2\145\1\162\1"+
        "\167\1\172\1\164\1\uffff\2\172\1\71\5\172\1\uffff\1\157\1\162\1"+
        "\55\1\145\1\170\1\164\1\151\1\145\1\164\1\162\2\172";
    static final String DFA23_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\uffff\1\12\1\13\13\uffff"+
        "\1\14\1\15\1\uffff\1\16\1\17\1\22\1\24\1\25\1\uffff\1\30\1\31\1"+
        "\32\1\23\1\11\21\uffff\1\26\1\27\27\uffff\1\20\10\uffff\1\21\14"+
        "\uffff";
    static final String DFA23_specialS =
        "\145\uffff}>";
    static final String[] DFA23_transitionS = {
            "\2\41\2\uffff\1\41\22\uffff\1\41\1\34\1\30\1\11\1\34\1\42\1"+
            "\1\1\6\1\12\1\13\2\34\1\41\1\31\1\27\1\34\12\32\1\35\1\40\4"+
            "\34\1\10\32\34\1\2\1\33\1\3\1\7\1\34\1\36\3\34\1\14\1\34\1\21"+
            "\2\34\1\15\2\34\1\16\1\24\1\25\2\34\1\17\1\22\1\26\1\23\1\34"+
            "\1\20\4\34\1\4\1\uffff\1\5\1\37",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\43",
            "",
            "",
            "\1\45\11\uffff\1\46",
            "\1\47",
            "\1\50\11\uffff\1\51",
            "\1\52",
            "\1\53",
            "\1\55\14\uffff\1\54",
            "\1\56",
            "\1\57\11\uffff\1\60",
            "\1\61",
            "\1\62\3\uffff\1\63",
            "\1\64",
            "",
            "",
            "\12\65",
            "",
            "",
            "",
            "",
            "",
            "\1\66",
            "",
            "",
            "",
            "",
            "",
            "\1\70",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\71",
            "\1\72",
            "\1\73",
            "\1\74",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\75",
            "\1\76",
            "\1\77",
            "\1\100\3\uffff\1\101",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\105",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\1\34\1\106\1\34\12\65"+
            "\1\34\1\uffff\4\34\1\uffff\4\34\1\107\25\34\4\uffff\1\34\1\uffff"+
            "\4\34\1\107\25\34",
            "",
            "",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\110",
            "\1\111",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\112",
            "\1\113",
            "\1\114",
            "\1\115",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\116",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\120",
            "\12\121",
            "\1\122\2\uffff\12\123",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\124",
            "\1\125",
            "\1\126",
            "\1\127",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\131",
            "",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\3\34\12\121\1\34\1\uffff"+
            "\4\34\1\uffff\4\34\1\107\25\34\4\uffff\1\34\1\uffff\4\34\1\107"+
            "\25\34",
            "\12\123",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\3\34\12\123\1\34\1\uffff"+
            "\4\34\1\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "",
            "\1\132",
            "\1\133",
            "\1\134",
            "\1\135",
            "\1\136\11\uffff\1\137",
            "\1\140",
            "\1\141",
            "\1\142",
            "\1\143",
            "\1\144",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34",
            "\1\34\1\uffff\2\34\5\uffff\2\34\1\uffff\16\34\1\uffff\4\34\1"+
            "\uffff\32\34\4\uffff\1\34\1\uffff\32\34"
    };

    static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
    static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
    static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
    static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
    static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
    static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
    static final short[][] DFA23_transition;

    static {
        int numStates = DFA23_transitionS.length;
        DFA23_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
        }
    }

    class DFA23 extends DFA {

        public DFA23(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 23;
            this.eot = DFA23_eot;
            this.eof = DFA23_eof;
            this.min = DFA23_min;
            this.max = DFA23_max;
            this.accept = DFA23_accept;
            this.special = DFA23_special;
            this.transition = DFA23_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T24 | T25 | T26 | T27 | T28 | T29 | T30 | T31 | T32 | OPEN_PAREN | CLOSE_PAREN | SPECIAL_FORM | STRING | NUMBER | CHARACTER | NIL | BOOLEAN | SYMBOL | METADATA_TYPEHINT | KEYWORD | SYNTAX_QUOTE | UNQUOTE_SPLICING | UNQUOTE | COMMENT | SPACE | LAMBDA_ARG );";
        }
    }
 

}