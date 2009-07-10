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
// $ANTLR 3.0.1 /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g 2009-07-10 21:26:57

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ClojureLexer extends Lexer {
    public static final int UNQUOTE=31;
    public static final int HEXDIGIT=19;
    public static final int SYNTAX_QUOTE=29;
    public static final int SPACE=33;
    public static final int CLOSE_PAREN=5;
    public static final int RIGHT_CURLY_BRACKET=10;
    public static final int SPECIAL_FORM=16;
    public static final int KEYWORD=28;
    public static final int COMMERCIAL_AT=13;
    public static final int NUMBER=18;
    public static final int STRING=17;
    public static final int LAMBDA_ARG=34;
    public static final int COMMENT=32;
    public static final int BACKSLASH=11;
    public static final int CIRCUMFLEX=12;
    public static final int LEFT_CURLY_BRACKET=9;
    public static final int BOOLEAN=22;
    public static final int SYMBOL_HEAD=26;
    public static final int EOF=-1;
    public static final int NIL=21;
    public static final int SYMBOL=24;
    public static final int Tokens=35;
    public static final int OPEN_PAREN=4;
    public static final int UNQUOTE_SPLICING=30;
    public static final int APOSTROPHE=15;
    public static final int CHARACTER=20;
    public static final int NUMBER_SIGN=14;
    public static final int AMPERSAND=6;
    public static final int RIGHT_SQUARE_BRACKET=8;
    public static final int NAME=23;
    public static final int METADATA_TYPEHINT=25;
    public static final int SYMBOL_REST=27;
    public static final int LEFT_SQUARE_BRACKET=7;
    public ClojureLexer() {;} 
    public ClojureLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "/home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g"; }

    // $ANTLR start OPEN_PAREN
    public final void mOPEN_PAREN() throws RecognitionException {
        try {
            int _type = OPEN_PAREN;
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:39:11: ( '(' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:39:13: '('
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:41:12: ( ')' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:41:14: ')'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:43:10: ( '&' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:43:12: '&'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:45:20: ( '[' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:45:22: '['
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:47:21: ( ']' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:47:23: ']'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:49:19: ( '{' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:49:21: '{'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:51:20: ( '}' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:51:22: '}'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:53:10: ( '\\\\' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:53:12: '\\\\'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:55:11: ( '^' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:55:13: '^'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:57:14: ( '@' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:57:16: '@'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:59:12: ( '#' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:59:14: '#'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:61:11: ( '\\'' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:61:13: '\\''
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:65:13: ( 'def' | 'if' | 'do' | 'let' | 'quote' | 'var' | 'fn' | 'loop' | 'recur' | 'throw' | 'try' | 'monitor-enter' | 'monitor-exit' | 'new' | 'set!' | '.' )
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

                if ( (LA1_3=='e') ) {
                    alt1=4;
                }
                else if ( (LA1_3=='o') ) {
                    alt1=8;
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
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:65:15: 'def'
                    {
                    match("def"); 


                    }
                    break;
                case 2 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:65:23: 'if'
                    {
                    match("if"); 


                    }
                    break;
                case 3 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:65:30: 'do'
                    {
                    match("do"); 


                    }
                    break;
                case 4 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:65:37: 'let'
                    {
                    match("let"); 


                    }
                    break;
                case 5 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:65:45: 'quote'
                    {
                    match("quote"); 


                    }
                    break;
                case 6 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:65:55: 'var'
                    {
                    match("var"); 


                    }
                    break;
                case 7 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:65:63: 'fn'
                    {
                    match("fn"); 


                    }
                    break;
                case 8 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:65:70: 'loop'
                    {
                    match("loop"); 


                    }
                    break;
                case 9 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:66:13: 'recur'
                    {
                    match("recur"); 


                    }
                    break;
                case 10 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:66:23: 'throw'
                    {
                    match("throw"); 


                    }
                    break;
                case 11 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:66:33: 'try'
                    {
                    match("try"); 


                    }
                    break;
                case 12 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:66:41: 'monitor-enter'
                    {
                    match("monitor-enter"); 


                    }
                    break;
                case 13 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:66:59: 'monitor-exit'
                    {
                    match("monitor-exit"); 


                    }
                    break;
                case 14 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:67:13: 'new'
                    {
                    match("new"); 


                    }
                    break;
                case 15 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:67:21: 'set!'
                    {
                    match("set!"); 


                    }
                    break;
                case 16 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:67:30: '.'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:71:7: ( '\"' (~ '\"' | ( BACKSLASH '\"' ) )* '\"' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:71:9: '\"' (~ '\"' | ( BACKSLASH '\"' ) )* '\"'
            {
            match('\"'); 
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:71:13: (~ '\"' | ( BACKSLASH '\"' ) )*
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
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:71:15: ~ '\"'
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
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:71:22: ( BACKSLASH '\"' )
            	    {
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:71:22: ( BACKSLASH '\"' )
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:71:23: BACKSLASH '\"'
            	    {
            	    mBACKSLASH(); 
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:7: ( ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )? )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:9: ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )?
            {
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:9: ( '-' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='-') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:9: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:14: ( '0' .. '9' )+
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
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:14: '0' .. '9'
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

            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:24: ( '.' ( '0' .. '9' )+ )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='.') ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:25: '.' ( '0' .. '9' )+
                    {
                    match('.'); 
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:29: ( '0' .. '9' )+
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
                    	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:29: '0' .. '9'
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

            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:41: ( ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+ )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='E'||LA9_0=='e') ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:42: ( 'e' | 'E' ) ( '-' )? ( '0' .. '9' )+
                    {
                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }

                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:52: ( '-' )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0=='-') ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:52: '-'
                            {
                            match('-'); 

                            }
                            break;

                    }

                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:57: ( '0' .. '9' )+
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
                    	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:76:57: '0' .. '9'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:79:10: ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT | BACKSLASH . )
            int alt10=5;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='\\') ) {
                int LA10_1 = input.LA(2);

                if ( (LA10_1=='s') ) {
                    int LA10_2 = input.LA(3);

                    if ( (LA10_2=='p') ) {
                        alt10=2;
                    }
                    else {
                        alt10=5;}
                }
                else if ( (LA10_1=='n') ) {
                    int LA10_3 = input.LA(3);

                    if ( (LA10_3=='e') ) {
                        alt10=1;
                    }
                    else {
                        alt10=5;}
                }
                else if ( (LA10_1=='u') ) {
                    int LA10_4 = input.LA(3);

                    if ( ((LA10_4>='0' && LA10_4<='9')||(LA10_4>='A' && LA10_4<='F')||(LA10_4>='a' && LA10_4<='f')) ) {
                        alt10=4;
                    }
                    else {
                        alt10=5;}
                }
                else if ( (LA10_1=='t') ) {
                    int LA10_5 = input.LA(3);

                    if ( (LA10_5=='a') ) {
                        alt10=3;
                    }
                    else {
                        alt10=5;}
                }
                else if ( ((LA10_1>='\u0000' && LA10_1<='m')||(LA10_1>='o' && LA10_1<='r')||(LA10_1>='v' && LA10_1<='\uFFFE')) ) {
                    alt10=5;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("79:1: CHARACTER : ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT | BACKSLASH . );", 10, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("79:1: CHARACTER : ( '\\\\newline' | '\\\\space' | '\\\\tab' | '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT | BACKSLASH . );", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:80:9: '\\\\newline'
                    {
                    match("\\newline"); 


                    }
                    break;
                case 2 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:81:9: '\\\\space'
                    {
                    match("\\space"); 


                    }
                    break;
                case 3 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:82:9: '\\\\tab'
                    {
                    match("\\tab"); 


                    }
                    break;
                case 4 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:83:9: '\\\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
                    {
                    match("\\u"); 

                    mHEXDIGIT(); 
                    mHEXDIGIT(); 
                    mHEXDIGIT(); 
                    mHEXDIGIT(); 

                    }
                    break;
                case 5 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:84:9: BACKSLASH .
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:87:9: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:90:4: ( 'nil' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:90:9: 'nil'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:93:8: ( 'true' | 'false' )
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
                    new NoViableAltException("93:1: BOOLEAN : ( 'true' | 'false' );", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:94:9: 'true'
                    {
                    match("true"); 


                    }
                    break;
                case 2 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:95:9: 'false'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:98:7: ( '/' | NAME ( '/' NAME )? )
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
                    new NoViableAltException("98:1: SYMBOL : ( '/' | NAME ( '/' NAME )? );", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:99:9: '/'
                    {
                    match('/'); 

                    }
                    break;
                case 2 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:100:9: NAME ( '/' NAME )?
                    {
                    mNAME(); 
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:100:14: ( '/' NAME )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0=='/') ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:100:15: '/' NAME
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:103:18: ( NUMBER_SIGN CIRCUMFLEX NAME )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:104:3: NUMBER_SIGN CIRCUMFLEX NAME
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:108:5: ( SYMBOL_HEAD ( SYMBOL_REST )* ( ':' ( SYMBOL_REST )+ )* )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:108:9: SYMBOL_HEAD ( SYMBOL_REST )* ( ':' ( SYMBOL_REST )+ )*
            {
            mSYMBOL_HEAD(); 
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:108:21: ( SYMBOL_REST )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0=='!'||(LA14_0>='#' && LA14_0<='$')||(LA14_0>='*' && LA14_0<='+')||(LA14_0>='-' && LA14_0<='.')||(LA14_0>='0' && LA14_0<='9')||(LA14_0>='<' && LA14_0<='?')||(LA14_0>='A' && LA14_0<='Z')||LA14_0=='_'||(LA14_0>='a' && LA14_0<='z')) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:108:21: SYMBOL_REST
            	    {
            	    mSYMBOL_REST(); 

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:108:34: ( ':' ( SYMBOL_REST )+ )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==':') ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:108:35: ':' ( SYMBOL_REST )+
            	    {
            	    match(':'); 
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:108:39: ( SYMBOL_REST )+
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
            	    	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:108:39: SYMBOL_REST
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:112:12: ( 'a' .. 'z' | 'A' .. 'Z' | '*' | '+' | '!' | '-' | '_' | '?' | '>' | '<' | '=' | '$' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:118:12: ( SYMBOL_HEAD | '0' .. '9' | '.' | NUMBER_SIGN )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:134:8: ( ':' SYMBOL )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:135:9: ':' SYMBOL
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:138:13: ( '`' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:139:9: '`'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:142:17: ( '~@' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:143:9: '~@'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:146:8: ( '~' )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:147:9: '~'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:150:8: ( ';' (~ ( '\\r' | '\\n' ) )* ( ( '\\r' )? '\\n' )? )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:151:9: ';' (~ ( '\\r' | '\\n' ) )* ( ( '\\r' )? '\\n' )?
            {
            match(';'); 
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:151:13: (~ ( '\\r' | '\\n' ) )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( ((LA17_0>='\u0000' && LA17_0<='\t')||(LA17_0>='\u000B' && LA17_0<='\f')||(LA17_0>='\u000E' && LA17_0<='\uFFFE')) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:151:13: ~ ( '\\r' | '\\n' )
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

            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:151:29: ( ( '\\r' )? '\\n' )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0=='\n'||LA19_0=='\r') ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:151:30: ( '\\r' )? '\\n'
                    {
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:151:30: ( '\\r' )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0=='\r') ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:151:30: '\\r'
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:154:6: ( ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+ )
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:154:9: ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+
            {
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:154:9: ( ' ' | '\\t' | ',' | '\\r' | '\\n' )+
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
            	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:
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
            // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:158:11: ( '%' '1' .. '9' ( '0' .. '9' )* | '%&' | '%' )
            int alt22=3;
            int LA22_0 = input.LA(1);

            if ( (LA22_0=='%') ) {
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
                    alt22=1;
                    }
                    break;
                case '&':
                    {
                    alt22=2;
                    }
                    break;
                default:
                    alt22=3;}

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("158:1: LAMBDA_ARG : ( '%' '1' .. '9' ( '0' .. '9' )* | '%&' | '%' );", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:159:9: '%' '1' .. '9' ( '0' .. '9' )*
                    {
                    match('%'); 
                    matchRange('1','9'); 
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:159:22: ( '0' .. '9' )*
                    loop21:
                    do {
                        int alt21=2;
                        int LA21_0 = input.LA(1);

                        if ( ((LA21_0>='0' && LA21_0<='9')) ) {
                            alt21=1;
                        }


                        switch (alt21) {
                    	case 1 :
                    	    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:159:22: '0' .. '9'
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
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:160:9: '%&'
                    {
                    match("%&"); 


                    }
                    break;
                case 3 :
                    // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:161:9: '%'
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
        // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:8: ( OPEN_PAREN | CLOSE_PAREN | AMPERSAND | LEFT_SQUARE_BRACKET | RIGHT_SQUARE_BRACKET | LEFT_CURLY_BRACKET | RIGHT_CURLY_BRACKET | BACKSLASH | CIRCUMFLEX | COMMERCIAL_AT | NUMBER_SIGN | APOSTROPHE | SPECIAL_FORM | STRING | NUMBER | CHARACTER | HEXDIGIT | NIL | BOOLEAN | SYMBOL | METADATA_TYPEHINT | KEYWORD | SYNTAX_QUOTE | UNQUOTE_SPLICING | UNQUOTE | COMMENT | SPACE | LAMBDA_ARG )
        int alt23=28;
        alt23 = dfa23.predict(input);
        switch (alt23) {
            case 1 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:10: OPEN_PAREN
                {
                mOPEN_PAREN(); 

                }
                break;
            case 2 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:21: CLOSE_PAREN
                {
                mCLOSE_PAREN(); 

                }
                break;
            case 3 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:33: AMPERSAND
                {
                mAMPERSAND(); 

                }
                break;
            case 4 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:43: LEFT_SQUARE_BRACKET
                {
                mLEFT_SQUARE_BRACKET(); 

                }
                break;
            case 5 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:63: RIGHT_SQUARE_BRACKET
                {
                mRIGHT_SQUARE_BRACKET(); 

                }
                break;
            case 6 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:84: LEFT_CURLY_BRACKET
                {
                mLEFT_CURLY_BRACKET(); 

                }
                break;
            case 7 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:103: RIGHT_CURLY_BRACKET
                {
                mRIGHT_CURLY_BRACKET(); 

                }
                break;
            case 8 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:123: BACKSLASH
                {
                mBACKSLASH(); 

                }
                break;
            case 9 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:133: CIRCUMFLEX
                {
                mCIRCUMFLEX(); 

                }
                break;
            case 10 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:144: COMMERCIAL_AT
                {
                mCOMMERCIAL_AT(); 

                }
                break;
            case 11 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:158: NUMBER_SIGN
                {
                mNUMBER_SIGN(); 

                }
                break;
            case 12 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:170: APOSTROPHE
                {
                mAPOSTROPHE(); 

                }
                break;
            case 13 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:181: SPECIAL_FORM
                {
                mSPECIAL_FORM(); 

                }
                break;
            case 14 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:194: STRING
                {
                mSTRING(); 

                }
                break;
            case 15 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:201: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 16 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:208: CHARACTER
                {
                mCHARACTER(); 

                }
                break;
            case 17 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:218: HEXDIGIT
                {
                mHEXDIGIT(); 

                }
                break;
            case 18 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:227: NIL
                {
                mNIL(); 

                }
                break;
            case 19 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:231: BOOLEAN
                {
                mBOOLEAN(); 

                }
                break;
            case 20 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:239: SYMBOL
                {
                mSYMBOL(); 

                }
                break;
            case 21 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:246: METADATA_TYPEHINT
                {
                mMETADATA_TYPEHINT(); 

                }
                break;
            case 22 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:264: KEYWORD
                {
                mKEYWORD(); 

                }
                break;
            case 23 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:272: SYNTAX_QUOTE
                {
                mSYNTAX_QUOTE(); 

                }
                break;
            case 24 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:285: UNQUOTE_SPLICING
                {
                mUNQUOTE_SPLICING(); 

                }
                break;
            case 25 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:302: UNQUOTE
                {
                mUNQUOTE(); 

                }
                break;
            case 26 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:310: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 27 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:318: SPACE
                {
                mSPACE(); 

                }
                break;
            case 28 :
                // /home/stm/JAVA/ws-clojure-dev-git/clojure-antlr-grammar/src/Clojure.g:1:324: LAMBDA_ARG
                {
                mLAMBDA_ARG(); 

                }
                break;

        }

    }


    protected DFA23 dfa23 = new DFA23(this);
    static final String DFA23_eotS =
        "\10\uffff\1\45\2\uffff\1\46\1\uffff\1\52\4\35\1\52\5\35\2\uffff"+
        "\1\35\1\uffff\1\52\3\uffff\1\74\7\uffff\1\35\1\30\1\uffff\1\30\5"+
        "\35\1\30\7\35\1\72\3\uffff\1\30\1\35\1\30\1\35\1\30\2\35\1\30\3"+
        "\35\1\124\1\30\3\35\1\30\3\35\1\134\2\35\1\uffff\1\30\1\72\1\35"+
        "\1\72\1\30\1\134\1\30\1\uffff\1\30\11\35\1\30\1\35\1\30";
    static final String DFA23_eofS =
        "\152\uffff";
    static final String DFA23_minS =
        "\1\11\7\uffff\1\0\2\uffff\1\136\1\uffff\1\41\1\146\1\145\1\165\1"+
        "\141\1\41\1\145\1\150\1\157\2\145\2\uffff\1\60\1\uffff\1\41\3\uffff"+
        "\1\100\7\uffff\1\146\1\41\1\uffff\1\41\1\157\1\164\1\157\1\162\1"+
        "\154\1\41\1\143\1\165\1\162\1\156\1\154\1\167\1\164\1\41\3\uffff"+
        "\1\41\1\160\1\41\1\164\1\41\1\163\1\165\1\41\1\145\1\157\1\151\3"+
        "\41\1\60\1\55\1\41\2\145\1\162\1\41\1\167\1\164\1\uffff\2\41\1\60"+
        "\4\41\1\uffff\1\41\1\157\1\162\1\55\1\145\1\156\1\151\2\164\1\145"+
        "\1\41\1\162\1\41";
    static final String DFA23_maxS =
        "\1\176\7\uffff\1\ufffe\2\uffff\1\136\1\uffff\1\172\1\146\1\157\1"+
        "\165\1\141\1\172\1\145\1\162\1\157\1\151\1\145\2\uffff\1\71\1\uffff"+
        "\1\172\3\uffff\1\100\7\uffff\1\146\1\172\1\uffff\1\172\1\157\1\164"+
        "\1\157\1\162\1\154\1\172\1\143\1\171\1\162\1\156\1\154\1\167\1\164"+
        "\1\172\3\uffff\1\172\1\160\1\172\1\164\1\172\1\163\1\165\1\172\1"+
        "\145\1\157\1\151\2\172\1\41\2\71\1\172\2\145\1\162\1\172\1\167\1"+
        "\164\1\uffff\2\172\1\71\4\172\1\uffff\1\172\1\157\1\162\1\55\1\145"+
        "\1\170\1\151\2\164\1\145\1\172\1\162\1\172";
    static final String DFA23_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\11\1\12\1\uffff\1"+
        "\14\13\uffff\1\15\1\16\1\uffff\1\17\1\uffff\1\24\1\26\1\27\1\uffff"+
        "\1\32\1\33\1\34\1\20\1\10\1\13\1\25\2\uffff\1\21\17\uffff\1\17\1"+
        "\30\1\31\27\uffff\1\22\7\uffff\1\23\15\uffff";
    static final String DFA23_specialS =
        "\152\uffff}>";
    static final String[] DFA23_transitionS = {
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
            "\1\55\11\uffff\1\54",
            "\1\56",
            "\1\57",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\1\60\14\35\1\61\14\35",
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
            "\1\102",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
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
            "\1\115",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
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
            "\1\144\11\uffff\1\143",
            "\1\145",
            "\1\146",
            "\1\147",
            "\1\150",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\151",
            "\1\35\1\uffff\2\35\5\uffff\2\35\1\uffff\16\35\1\uffff\4\35\1"+
            "\uffff\32\35\4\uffff\1\35\1\uffff\32\35"
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
            return "1:1: Tokens : ( OPEN_PAREN | CLOSE_PAREN | AMPERSAND | LEFT_SQUARE_BRACKET | RIGHT_SQUARE_BRACKET | LEFT_CURLY_BRACKET | RIGHT_CURLY_BRACKET | BACKSLASH | CIRCUMFLEX | COMMERCIAL_AT | NUMBER_SIGN | APOSTROPHE | SPECIAL_FORM | STRING | NUMBER | CHARACTER | HEXDIGIT | NIL | BOOLEAN | SYMBOL | METADATA_TYPEHINT | KEYWORD | SYNTAX_QUOTE | UNQUOTE_SPLICING | UNQUOTE | COMMENT | SPACE | LAMBDA_ARG );";
        }
    }
 

}