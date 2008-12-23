package clojuredev.lexers;
// $ANTLR 3.0.1 o:/clojure/basic-clojure-grammar/src/Clojure.g 2008-12-09 23:11:22

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ClojureParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "OPEN_PAREN", "CLOSE_PAREN", "SPECIAL_FORM", "STRING", "NUMBER", "CHARACTER", "NIL", "BOOLEAN", "NAME", "SYMBOL", "SYMBOL_HEAD", "SYMBOL_REST", "KEYWORD", "SYNTAX_QUOTE", "UNQUOTE_SPLICING", "UNQUOTE", "COMMENT", "SPACE", "LAMBDA_ARG", "'&'", "'['", "']'", "'{'", "'}'", "'\\''", "'^'", "'@'", "'#'"
    };
    public static final int SYNTAX_QUOTE=17;
    public static final int KEYWORD=16;
    public static final int SYMBOL=13;
    public static final int SYMBOL_HEAD=14;
    public static final int NUMBER=8;
    public static final int OPEN_PAREN=4;
    public static final int SPACE=21;
    public static final int EOF=-1;
    public static final int CHARACTER=9;
    public static final int NAME=12;
    public static final int BOOLEAN=11;
    public static final int NIL=10;
    public static final int UNQUOTE=19;
    public static final int LAMBDA_ARG=22;
    public static final int SPECIAL_FORM=6;
    public static final int CLOSE_PAREN=5;
    public static final int SYMBOL_REST=15;
    public static final int COMMENT=20;
    public static final int UNQUOTE_SPLICING=18;
    public static final int STRING=7;

        public ClojureParser(TokenStream input) {
            super(input);
        }
        

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "o:/clojure/basic-clojure-grammar/src/Clojure.g"; }

    
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:106:1: literal : ( STRING | NUMBER | CHARACTER | NIL | BOOLEAN | KEYWORD );
    public final void literal() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:106:8: ( STRING | NUMBER | CHARACTER | NIL | BOOLEAN | KEYWORD )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:
            {
            if ( (input.LA(1)>=STRING && input.LA(1)<=BOOLEAN)||input.LA(1)==KEYWORD ) {
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:154:1: file : ( form )* ;
    public final void file() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:154:5: ( ( form )* )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:155:9: ( form )*
            {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:155:9: ( form )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==OPEN_PAREN||(LA1_0>=SPECIAL_FORM && LA1_0<=BOOLEAN)||LA1_0==SYMBOL||(LA1_0>=KEYWORD && LA1_0<=COMMENT)||(LA1_0>=LAMBDA_ARG && LA1_0<=24)||LA1_0==26||(LA1_0>=28 && LA1_0<=31)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:155:11: form
            	    {
            	    pushFollow(FOLLOW_form_in_file855);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:159:1: form : ({...}? LAMBDA_ARG | literal | COMMENT | '&' | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set );
    public final void form() throws RecognitionException {
        Token s=null;

        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:159:6: ({...}? LAMBDA_ARG | literal | COMMENT | '&' | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set )
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
            case 23:
                {
                alt4=4;
                }
                break;
            case 31:
                {
                switch ( input.LA(2) ) {
                case 29:
                    {
                    alt4=5;
                    }
                    break;
                case 26:
                    {
                    alt4=8;
                    }
                    break;
                case OPEN_PAREN:
                case STRING:
                case 28:
                    {
                    alt4=7;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("159:1: form : ({...}? LAMBDA_ARG | literal | COMMENT | '&' | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set );", 4, 5, input);

                    throw nvae;
                }

                }
                break;
            case OPEN_PAREN:
            case SPECIAL_FORM:
            case SYMBOL:
            case 24:
            case 26:
                {
                alt4=5;
                }
                break;
            case SYNTAX_QUOTE:
            case UNQUOTE_SPLICING:
            case UNQUOTE:
            case 28:
            case 29:
            case 30:
                {
                alt4=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("159:1: form : ({...}? LAMBDA_ARG | literal | COMMENT | '&' | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set );", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:160:3: {...}? LAMBDA_ARG
                    {
                    if ( !(this.inLambda) ) {
                        throw new FailedPredicateException(input, "form", "this.inLambda");
                    }
                    match(input,LAMBDA_ARG,FOLLOW_LAMBDA_ARG_in_form888); 

                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:161:10: literal
                    {
                    pushFollow(FOLLOW_literal_in_form899);
                    literal();
                    _fsp--;


                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:163:7: COMMENT
                    {
                    match(input,COMMENT,FOLLOW_COMMENT_in_form925); 

                    }
                    break;
                case 4 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:164:9: '&'
                    {
                    match(input,23,FOLLOW_23_in_form935); 

                    }
                    break;
                case 5 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:9: ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map )
                    {
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:9: ( metadataForm )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==31) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:9: metadataForm
                            {
                            pushFollow(FOLLOW_metadataForm_in_form945);
                            metadataForm();
                            _fsp--;


                            }
                            break;

                    }

                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:23: ( SPECIAL_FORM | s= SYMBOL | list | vector | map )
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
                    case 24:
                        {
                        alt3=4;
                        }
                        break;
                    case 26:
                        {
                        alt3=5;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("165:23: ( SPECIAL_FORM | s= SYMBOL | list | vector | map )", 3, 0, input);

                        throw nvae;
                    }

                    switch (alt3) {
                        case 1 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:25: SPECIAL_FORM
                            {
                            match(input,SPECIAL_FORM,FOLLOW_SPECIAL_FORM_in_form950); 

                            }
                            break;
                        case 2 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:40: s= SYMBOL
                            {
                            s=(Token)input.LT(1);
                            match(input,SYMBOL,FOLLOW_SYMBOL_in_form956); 
                             symbols.add(s.getText()); 

                            }
                            break;
                        case 3 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:72: list
                            {
                            pushFollow(FOLLOW_list_in_form962);
                            list();
                            _fsp--;


                            }
                            break;
                        case 4 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:79: vector
                            {
                            pushFollow(FOLLOW_vector_in_form966);
                            vector();
                            _fsp--;


                            }
                            break;
                        case 5 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:88: map
                            {
                            pushFollow(FOLLOW_map_in_form970);
                            map();
                            _fsp--;


                            }
                            break;

                    }


                    }
                    break;
                case 6 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:166:9: macroForm
                    {
                    pushFollow(FOLLOW_macroForm_in_form982);
                    macroForm();
                    _fsp--;


                    }
                    break;
                case 7 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:167:9: dispatchMacroForm
                    {
                    pushFollow(FOLLOW_dispatchMacroForm_in_form992);
                    dispatchMacroForm();
                    _fsp--;


                    }
                    break;
                case 8 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:168:9: set
                    {
                    pushFollow(FOLLOW_set_in_form1002);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:171:1: macroForm : ( quoteForm | metaForm | derefForm | syntaxQuoteForm | {...}? unquoteSplicingForm | {...}? unquoteForm );
    public final void macroForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:171:10: ( quoteForm | metaForm | derefForm | syntaxQuoteForm | {...}? unquoteSplicingForm | {...}? unquoteForm )
            int alt5=6;
            switch ( input.LA(1) ) {
            case 28:
                {
                alt5=1;
                }
                break;
            case 29:
                {
                alt5=2;
                }
                break;
            case 30:
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
                    new NoViableAltException("171:1: macroForm : ( quoteForm | metaForm | derefForm | syntaxQuoteForm | {...}? unquoteSplicingForm | {...}? unquoteForm );", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:172:9: quoteForm
                    {
                    pushFollow(FOLLOW_quoteForm_in_macroForm1033);
                    quoteForm();
                    _fsp--;


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:173:9: metaForm
                    {
                    pushFollow(FOLLOW_metaForm_in_macroForm1043);
                    metaForm();
                    _fsp--;


                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:174:9: derefForm
                    {
                    pushFollow(FOLLOW_derefForm_in_macroForm1053);
                    derefForm();
                    _fsp--;


                    }
                    break;
                case 4 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:175:9: syntaxQuoteForm
                    {
                    pushFollow(FOLLOW_syntaxQuoteForm_in_macroForm1063);
                    syntaxQuoteForm();
                    _fsp--;


                    }
                    break;
                case 5 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:176:7: {...}? unquoteSplicingForm
                    {
                    if ( !( this.syntaxQuoteDepth > 0 ) ) {
                        throw new FailedPredicateException(input, "macroForm", " this.syntaxQuoteDepth > 0 ");
                    }
                    pushFollow(FOLLOW_unquoteSplicingForm_in_macroForm1073);
                    unquoteSplicingForm();
                    _fsp--;


                    }
                    break;
                case 6 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:177:7: {...}? unquoteForm
                    {
                    if ( !( this.syntaxQuoteDepth > 0 ) ) {
                        throw new FailedPredicateException(input, "macroForm", " this.syntaxQuoteDepth > 0 ");
                    }
                    pushFollow(FOLLOW_unquoteForm_in_macroForm1083);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:180:1: dispatchMacroForm : ( regexForm | varQuoteForm | {...}? lambdaForm );
    public final void dispatchMacroForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:180:18: ( regexForm | varQuoteForm | {...}? lambdaForm )
            int alt6=3;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==31) ) {
                switch ( input.LA(2) ) {
                case STRING:
                    {
                    alt6=1;
                    }
                    break;
                case 28:
                    {
                    alt6=2;
                    }
                    break;
                case OPEN_PAREN:
                    {
                    alt6=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("180:1: dispatchMacroForm : ( regexForm | varQuoteForm | {...}? lambdaForm );", 6, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("180:1: dispatchMacroForm : ( regexForm | varQuoteForm | {...}? lambdaForm );", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:181:9: regexForm
                    {
                    pushFollow(FOLLOW_regexForm_in_dispatchMacroForm1110);
                    regexForm();
                    _fsp--;


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:182:9: varQuoteForm
                    {
                    pushFollow(FOLLOW_varQuoteForm_in_dispatchMacroForm1120);
                    varQuoteForm();
                    _fsp--;


                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:183:9: {...}? lambdaForm
                    {
                    if ( !(!this.inLambda) ) {
                        throw new FailedPredicateException(input, "dispatchMacroForm", "!this.inLambda");
                    }
                    pushFollow(FOLLOW_lambdaForm_in_dispatchMacroForm1132);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:186:1: list : o= OPEN_PAREN ( form )* c= CLOSE_PAREN ;
    public final void list() throws RecognitionException {
        Token o=null;
        Token c=null;

        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:186:5: (o= OPEN_PAREN ( form )* c= CLOSE_PAREN )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:186:9: o= OPEN_PAREN ( form )* c= CLOSE_PAREN
            {
            o=(Token)input.LT(1);
            match(input,OPEN_PAREN,FOLLOW_OPEN_PAREN_in_list1153); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:186:22: ( form )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==OPEN_PAREN||(LA7_0>=SPECIAL_FORM && LA7_0<=BOOLEAN)||LA7_0==SYMBOL||(LA7_0>=KEYWORD && LA7_0<=COMMENT)||(LA7_0>=LAMBDA_ARG && LA7_0<=24)||LA7_0==26||(LA7_0>=28 && LA7_0<=31)) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:186:22: form
            	    {
            	    pushFollow(FOLLOW_form_in_list1155);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            c=(Token)input.LT(1);
            match(input,CLOSE_PAREN,FOLLOW_CLOSE_PAREN_in_list1161); 
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:189:1: vector : '[' ( form )* ']' ;
    public final void vector() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:189:7: ( '[' ( form )* ']' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:189:9: '[' ( form )* ']'
            {
            match(input,24,FOLLOW_24_in_vector1179); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:189:13: ( form )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==OPEN_PAREN||(LA8_0>=SPECIAL_FORM && LA8_0<=BOOLEAN)||LA8_0==SYMBOL||(LA8_0>=KEYWORD && LA8_0<=COMMENT)||(LA8_0>=LAMBDA_ARG && LA8_0<=24)||LA8_0==26||(LA8_0>=28 && LA8_0<=31)) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:189:13: form
            	    {
            	    pushFollow(FOLLOW_form_in_vector1181);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            match(input,25,FOLLOW_25_in_vector1184); 

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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:192:1: map : '{' ( form form )* '}' ;
    public final void map() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:192:4: ( '{' ( form form )* '}' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:192:9: '{' ( form form )* '}'
            {
            match(input,26,FOLLOW_26_in_map1203); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:192:13: ( form form )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==OPEN_PAREN||(LA9_0>=SPECIAL_FORM && LA9_0<=BOOLEAN)||LA9_0==SYMBOL||(LA9_0>=KEYWORD && LA9_0<=COMMENT)||(LA9_0>=LAMBDA_ARG && LA9_0<=24)||LA9_0==26||(LA9_0>=28 && LA9_0<=31)) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:192:14: form form
            	    {
            	    pushFollow(FOLLOW_form_in_map1206);
            	    form();
            	    _fsp--;

            	    pushFollow(FOLLOW_form_in_map1208);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            match(input,27,FOLLOW_27_in_map1212); 

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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:195:1: quoteForm : '\\'' form ;
    public final void quoteForm() throws RecognitionException {
         this.syntaxQuoteDepth++; 
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:198:5: ( '\\'' form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:198:8: '\\'' form
            {
            match(input,28,FOLLOW_28_in_quoteForm1245); 
            pushFollow(FOLLOW_form_in_quoteForm1247);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:201:1: metaForm : '^' form ;
    public final void metaForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:201:9: ( '^' form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:201:13: '^' form
            {
            match(input,29,FOLLOW_29_in_metaForm1261); 
            pushFollow(FOLLOW_form_in_metaForm1263);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:204:1: derefForm : '@' form ;
    public final void derefForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:204:10: ( '@' form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:204:13: '@' form
            {
            match(input,30,FOLLOW_30_in_derefForm1280); 
            pushFollow(FOLLOW_form_in_derefForm1282);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:207:1: syntaxQuoteForm : SYNTAX_QUOTE form ;
    public final void syntaxQuoteForm() throws RecognitionException {
         this.syntaxQuoteDepth++; 
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:210:5: ( SYNTAX_QUOTE form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:211:9: SYNTAX_QUOTE form
            {
            match(input,SYNTAX_QUOTE,FOLLOW_SYNTAX_QUOTE_in_syntaxQuoteForm1322); 
            pushFollow(FOLLOW_form_in_syntaxQuoteForm1324);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:214:1: unquoteForm : UNQUOTE form ;
    public final void unquoteForm() throws RecognitionException {
         this.syntaxQuoteDepth--; 
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:217:5: ( UNQUOTE form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:218:9: UNQUOTE form
            {
            match(input,UNQUOTE,FOLLOW_UNQUOTE_in_unquoteForm1364); 
            pushFollow(FOLLOW_form_in_unquoteForm1366);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:221:1: unquoteSplicingForm : UNQUOTE_SPLICING form ;
    public final void unquoteSplicingForm() throws RecognitionException {
         this.syntaxQuoteDepth--; 
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:224:5: ( UNQUOTE_SPLICING form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:225:9: UNQUOTE_SPLICING form
            {
            match(input,UNQUOTE_SPLICING,FOLLOW_UNQUOTE_SPLICING_in_unquoteSplicingForm1406); 
            pushFollow(FOLLOW_form_in_unquoteSplicingForm1408);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:228:1: set : '#' '{' ( form )* '}' ;
    public final void set() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:228:4: ( '#' '{' ( form )* '}' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:228:9: '#' '{' ( form )* '}'
            {
            match(input,31,FOLLOW_31_in_set1427); 
            match(input,26,FOLLOW_26_in_set1429); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:228:17: ( form )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==OPEN_PAREN||(LA10_0>=SPECIAL_FORM && LA10_0<=BOOLEAN)||LA10_0==SYMBOL||(LA10_0>=KEYWORD && LA10_0<=COMMENT)||(LA10_0>=LAMBDA_ARG && LA10_0<=24)||LA10_0==26||(LA10_0>=28 && LA10_0<=31)) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:228:17: form
            	    {
            	    pushFollow(FOLLOW_form_in_set1431);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match(input,27,FOLLOW_27_in_set1434); 

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


    // $ANTLR start regexForm
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:231:1: regexForm : '#' STRING ;
    public final void regexForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:231:10: ( '#' STRING )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:231:13: '#' STRING
            {
            match(input,31,FOLLOW_31_in_regexForm1447); 
            match(input,STRING,FOLLOW_STRING_in_regexForm1449); 

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
    // $ANTLR end regexForm


    // $ANTLR start metadataForm
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:234:1: metadataForm : '#' '^' ( map | SYMBOL | KEYWORD | STRING ) ;
    public final void metadataForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:234:13: ( '#' '^' ( map | SYMBOL | KEYWORD | STRING ) )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:235:9: '#' '^' ( map | SYMBOL | KEYWORD | STRING )
            {
            match(input,31,FOLLOW_31_in_metadataForm1473); 
            match(input,29,FOLLOW_29_in_metadataForm1475); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:235:17: ( map | SYMBOL | KEYWORD | STRING )
            int alt11=4;
            switch ( input.LA(1) ) {
            case 26:
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
                    new NoViableAltException("235:17: ( map | SYMBOL | KEYWORD | STRING )", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:235:18: map
                    {
                    pushFollow(FOLLOW_map_in_metadataForm1478);
                    map();
                    _fsp--;


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:235:24: SYMBOL
                    {
                    match(input,SYMBOL,FOLLOW_SYMBOL_in_metadataForm1482); 

                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:235:31: KEYWORD
                    {
                    match(input,KEYWORD,FOLLOW_KEYWORD_in_metadataForm1484); 

                    }
                    break;
                case 4 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:235:39: STRING
                    {
                    match(input,STRING,FOLLOW_STRING_in_metadataForm1486); 

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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:238:1: varQuoteForm : '#' '\\'' form ;
    public final void varQuoteForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:238:13: ( '#' '\\'' form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:239:9: '#' '\\'' form
            {
            match(input,31,FOLLOW_31_in_varQuoteForm1507); 
            match(input,28,FOLLOW_28_in_varQuoteForm1509); 
            pushFollow(FOLLOW_form_in_varQuoteForm1511);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:244:1: lambdaForm : '#' list ;
    public final void lambdaForm() throws RecognitionException {
        
        this.inLambda = true;

        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:251:5: ( '#' list )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:251:7: '#' list
            {
            match(input,31,FOLLOW_31_in_lambdaForm1540); 
            pushFollow(FOLLOW_list_in_lambdaForm1542);
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
    public static final BitSet FOLLOW_form_in_file855 = new BitSet(new long[]{0x00000000F5DF2FD2L});
    public static final BitSet FOLLOW_LAMBDA_ARG_in_form888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_form899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMENT_in_form925 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_form935 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metadataForm_in_form945 = new BitSet(new long[]{0x0000000005002050L});
    public static final BitSet FOLLOW_SPECIAL_FORM_in_form950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYMBOL_in_form956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_list_in_form962 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vector_in_form966 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_map_in_form970 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macroForm_in_form982 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dispatchMacroForm_in_form992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_form1002 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quoteForm_in_macroForm1033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metaForm_in_macroForm1043 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derefForm_in_macroForm1053 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_syntaxQuoteForm_in_macroForm1063 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unquoteSplicingForm_in_macroForm1073 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unquoteForm_in_macroForm1083 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regexForm_in_dispatchMacroForm1110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varQuoteForm_in_dispatchMacroForm1120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lambdaForm_in_dispatchMacroForm1132 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OPEN_PAREN_in_list1153 = new BitSet(new long[]{0x00000000F5DF2FF0L});
    public static final BitSet FOLLOW_form_in_list1155 = new BitSet(new long[]{0x00000000F5DF2FF0L});
    public static final BitSet FOLLOW_CLOSE_PAREN_in_list1161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_vector1179 = new BitSet(new long[]{0x00000000F7DF2FD0L});
    public static final BitSet FOLLOW_form_in_vector1181 = new BitSet(new long[]{0x00000000F7DF2FD0L});
    public static final BitSet FOLLOW_25_in_vector1184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_map1203 = new BitSet(new long[]{0x00000000FDDF2FD0L});
    public static final BitSet FOLLOW_form_in_map1206 = new BitSet(new long[]{0x00000000F5DF2FD0L});
    public static final BitSet FOLLOW_form_in_map1208 = new BitSet(new long[]{0x00000000FDDF2FD0L});
    public static final BitSet FOLLOW_27_in_map1212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_quoteForm1245 = new BitSet(new long[]{0x00000000F5DF2FD0L});
    public static final BitSet FOLLOW_form_in_quoteForm1247 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_metaForm1261 = new BitSet(new long[]{0x00000000F5DF2FD0L});
    public static final BitSet FOLLOW_form_in_metaForm1263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_30_in_derefForm1280 = new BitSet(new long[]{0x00000000F5DF2FD0L});
    public static final BitSet FOLLOW_form_in_derefForm1282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYNTAX_QUOTE_in_syntaxQuoteForm1322 = new BitSet(new long[]{0x00000000F5DF2FD0L});
    public static final BitSet FOLLOW_form_in_syntaxQuoteForm1324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNQUOTE_in_unquoteForm1364 = new BitSet(new long[]{0x00000000F5DF2FD0L});
    public static final BitSet FOLLOW_form_in_unquoteForm1366 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNQUOTE_SPLICING_in_unquoteSplicingForm1406 = new BitSet(new long[]{0x00000000F5DF2FD0L});
    public static final BitSet FOLLOW_form_in_unquoteSplicingForm1408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_set1427 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_set1429 = new BitSet(new long[]{0x00000000FDDF2FD0L});
    public static final BitSet FOLLOW_form_in_set1431 = new BitSet(new long[]{0x00000000FDDF2FD0L});
    public static final BitSet FOLLOW_27_in_set1434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_regexForm1447 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_STRING_in_regexForm1449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_metadataForm1473 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_metadataForm1475 = new BitSet(new long[]{0x0000000004012080L});
    public static final BitSet FOLLOW_map_in_metadataForm1478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYMBOL_in_metadataForm1482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KEYWORD_in_metadataForm1484 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_metadataForm1486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_varQuoteForm1507 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_varQuoteForm1509 = new BitSet(new long[]{0x00000000F5DF2FD0L});
    public static final BitSet FOLLOW_form_in_varQuoteForm1511 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_lambdaForm1540 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_list_in_lambdaForm1542 = new BitSet(new long[]{0x0000000000000002L});

}