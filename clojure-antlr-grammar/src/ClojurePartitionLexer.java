// $ANTLR 3.0.1 o:/clojure/basic-clojure-grammar/src/ClojurePartition.g 2008-11-19 23:17:12

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ClojurePartitionLexer extends Lexer {
    public static final int PARTITION_STRING=4;
    public static final int PARTITION_COMMENT=5;
    public static final int PARTITION_CODE=6;
    public static final int EOF=-1;
    public static final int Tokens=7;
    public ClojurePartitionLexer() {;} 
    public ClojurePartitionLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "o:/clojure/basic-clojure-grammar/src/ClojurePartition.g"; }

    // $ANTLR start PARTITION_STRING
    public final void mPARTITION_STRING() throws RecognitionException {
        try {
            int _type = PARTITION_STRING;
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:9:17: ( '\"' (~ '\"' | ( '\\\\' '\"' ) )* '\"' )
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:9:19: '\"' (~ '\"' | ( '\\\\' '\"' ) )* '\"'
            {
            match('\"'); 
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:9:23: (~ '\"' | ( '\\\\' '\"' ) )*
            loop1:
            do {
                int alt1=3;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\\') ) {
                    int LA1_2 = input.LA(2);

                    if ( (LA1_2=='\"') ) {
                        int LA1_4 = input.LA(3);

                        if ( ((LA1_4>='\u0000' && LA1_4<='\uFFFE')) ) {
                            alt1=2;
                        }

                        else {
                            alt1=1;
                        }

                    }
                    else if ( ((LA1_2>='\u0000' && LA1_2<='!')||(LA1_2>='#' && LA1_2<='\uFFFE')) ) {
                        alt1=1;
                    }


                }
                else if ( ((LA1_0>='\u0000' && LA1_0<='!')||(LA1_0>='#' && LA1_0<='[')||(LA1_0>=']' && LA1_0<='\uFFFE')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:9:25: ~ '\"'
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
            	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:9:32: ( '\\\\' '\"' )
            	    {
            	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:9:32: ( '\\\\' '\"' )
            	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:9:33: '\\\\' '\"'
            	    {
            	    match('\\'); 
            	    match('\"'); 

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match('\"'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PARTITION_STRING

    // $ANTLR start PARTITION_COMMENT
    public final void mPARTITION_COMMENT() throws RecognitionException {
        try {
            int _type = PARTITION_COMMENT;
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:12:18: ( ( ';' (~ ( '\\r' | '\\n' ) )* ( '\\r' )? '\\n' )+ )
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:13:9: ( ';' (~ ( '\\r' | '\\n' ) )* ( '\\r' )? '\\n' )+
            {
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:13:9: ( ';' (~ ( '\\r' | '\\n' ) )* ( '\\r' )? '\\n' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==';') ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:13:10: ';' (~ ( '\\r' | '\\n' ) )* ( '\\r' )? '\\n'
            	    {
            	    match(';'); 
            	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:13:14: (~ ( '\\r' | '\\n' ) )*
            	    loop2:
            	    do {
            	        int alt2=2;
            	        int LA2_0 = input.LA(1);

            	        if ( ((LA2_0>='\u0000' && LA2_0<='\t')||(LA2_0>='\u000B' && LA2_0<='\f')||(LA2_0>='\u000E' && LA2_0<='\uFFFE')) ) {
            	            alt2=1;
            	        }


            	        switch (alt2) {
            	    	case 1 :
            	    	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:13:14: ~ ( '\\r' | '\\n' )
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
            	    	    break loop2;
            	        }
            	    } while (true);

            	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:13:30: ( '\\r' )?
            	    int alt3=2;
            	    int LA3_0 = input.LA(1);

            	    if ( (LA3_0=='\r') ) {
            	        alt3=1;
            	    }
            	    switch (alt3) {
            	        case 1 :
            	            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:13:30: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;

            	    }

            	    match('\n'); 

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


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PARTITION_COMMENT

    // $ANTLR start PARTITION_CODE
    public final void mPARTITION_CODE() throws RecognitionException {
        try {
            int _type = PARTITION_CODE;
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:16:15: ( (~ ( '\"' | ';' ) )+ )
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:17:3: (~ ( '\"' | ';' ) )+
            {
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:17:3: (~ ( '\"' | ';' ) )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<=':')||(LA5_0>='<' && LA5_0<='\uFFFE')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:17:3: ~ ( '\"' | ';' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<=':')||(input.LA(1)>='<' && input.LA(1)<='\uFFFE') ) {
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
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PARTITION_CODE

    public void mTokens() throws RecognitionException {
        // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:1:8: ( PARTITION_STRING | PARTITION_COMMENT | PARTITION_CODE )
        int alt6=3;
        int LA6_0 = input.LA(1);

        if ( (LA6_0=='\"') ) {
            alt6=1;
        }
        else if ( (LA6_0==';') ) {
            alt6=2;
        }
        else if ( ((LA6_0>='\u0000' && LA6_0<='!')||(LA6_0>='#' && LA6_0<=':')||(LA6_0>='<' && LA6_0<='\uFFFE')) ) {
            alt6=3;
        }
        else {
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens : ( PARTITION_STRING | PARTITION_COMMENT | PARTITION_CODE );", 6, 0, input);

            throw nvae;
        }
        switch (alt6) {
            case 1 :
                // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:1:10: PARTITION_STRING
                {
                mPARTITION_STRING(); 

                }
                break;
            case 2 :
                // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:1:27: PARTITION_COMMENT
                {
                mPARTITION_COMMENT(); 

                }
                break;
            case 3 :
                // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:1:45: PARTITION_CODE
                {
                mPARTITION_CODE(); 

                }
                break;

        }

    }


 

}