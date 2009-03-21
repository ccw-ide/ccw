package clojuredev.lexers;
//$ANTLR 3.0.1 o:/clojure/basic-clojure-grammar/src/Clojure.g 2009-03-21 01:14:27

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ClojureParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "OPEN_PAREN", "CLOSE_PAREN", "SPECIAL_FORM", "STRING", "NUMBER", "CHARACTER", "NIL", "BOOLEAN", "NAME", "SYMBOL", "METADATA_TYPEHINT", "SYMBOL_HEAD", "SYMBOL_REST", "KEYWORD", "SYNTAX_QUOTE", "UNQUOTE_SPLICING", "UNQUOTE", "COMMENT", "SPACE", "LAMBDA_ARG", "'&'", "'['", "']'", "'{'", "'}'", "'\\''", "'^'", "'@'", "'#'"
    };
    public static final int SYNTAX_QUOTE=18;
    public static final int KEYWORD=17;
    public static final int SYMBOL=13;
    public static final int METADATA_TYPEHINT=14;
    public static final int SYMBOL_HEAD=15;
    public static final int NUMBER=8;
    public static final int OPEN_PAREN=4;
    public static final int SPACE=22;
    public static final int EOF=-1;
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
    public static final int UNQUOTE_SPLICING=19;
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:102:1: literal : ( STRING | NUMBER | CHARACTER | NIL | BOOLEAN | KEYWORD );
    public final void literal() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:102:8: ( STRING | NUMBER | CHARACTER | NIL | BOOLEAN | KEYWORD )
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:145:1: file : ( form )* ;
    public final void file() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:145:5: ( ( form )* )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:146:9: ( form )*
            {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:146:9: ( form )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==OPEN_PAREN||(LA1_0>=SPECIAL_FORM && LA1_0<=BOOLEAN)||LA1_0==SYMBOL||(LA1_0>=KEYWORD && LA1_0<=COMMENT)||(LA1_0>=LAMBDA_ARG && LA1_0<=25)||LA1_0==27||(LA1_0>=29 && LA1_0<=32)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:146:11: form
            	    {
            	    pushFollow(FOLLOW_form_in_file857);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:150:1: form : ({...}? LAMBDA_ARG | literal | COMMENT | '&' | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set );
    public final void form() throws RecognitionException {
        Token s=null;

        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:150:6: ({...}? LAMBDA_ARG | literal | COMMENT | '&' | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set )
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
            case 24:
                {
                alt4=4;
                }
                break;
            case 32:
                {
                switch ( input.LA(2) ) {
                case 30:
                    {
                    alt4=5;
                    }
                    break;
                case 27:
                    {
                    alt4=8;
                    }
                    break;
                case OPEN_PAREN:
                case STRING:
                case 29:
                    {
                    alt4=7;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("150:1: form : ({...}? LAMBDA_ARG | literal | COMMENT | '&' | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set );", 4, 5, input);

                    throw nvae;
                }

                }
                break;
            case OPEN_PAREN:
            case SPECIAL_FORM:
            case SYMBOL:
            case 25:
            case 27:
                {
                alt4=5;
                }
                break;
            case SYNTAX_QUOTE:
            case UNQUOTE_SPLICING:
            case UNQUOTE:
            case 29:
            case 30:
            case 31:
                {
                alt4=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("150:1: form : ({...}? LAMBDA_ARG | literal | COMMENT | '&' | ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map ) | macroForm | dispatchMacroForm | set );", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:151:3: {...}? LAMBDA_ARG
                    {
                    if ( !(this.inLambda) ) {
                        throw new FailedPredicateException(input, "form", "this.inLambda");
                    }
                    match(input,LAMBDA_ARG,FOLLOW_LAMBDA_ARG_in_form890); 

                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:152:10: literal
                    {
                    pushFollow(FOLLOW_literal_in_form901);
                    literal();
                    _fsp--;


                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:154:7: COMMENT
                    {
                    match(input,COMMENT,FOLLOW_COMMENT_in_form927); 

                    }
                    break;
                case 4 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:155:9: '&'
                    {
                    match(input,24,FOLLOW_24_in_form937); 

                    }
                    break;
                case 5 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:156:9: ( metadataForm )? ( SPECIAL_FORM | s= SYMBOL | list | vector | map )
                    {
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:156:9: ( metadataForm )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==32) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:156:9: metadataForm
                            {
                            pushFollow(FOLLOW_metadataForm_in_form947);
                            metadataForm();
                            _fsp--;


                            }
                            break;

                    }

                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:156:23: ( SPECIAL_FORM | s= SYMBOL | list | vector | map )
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
                    case 25:
                        {
                        alt3=4;
                        }
                        break;
                    case 27:
                        {
                        alt3=5;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("156:23: ( SPECIAL_FORM | s= SYMBOL | list | vector | map )", 3, 0, input);

                        throw nvae;
                    }

                    switch (alt3) {
                        case 1 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:156:25: SPECIAL_FORM
                            {
                            match(input,SPECIAL_FORM,FOLLOW_SPECIAL_FORM_in_form952); 

                            }
                            break;
                        case 2 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:156:40: s= SYMBOL
                            {
                            s=(Token)input.LT(1);
                            match(input,SYMBOL,FOLLOW_SYMBOL_in_form958); 
                             symbols.add(s.getText()); 

                            }
                            break;
                        case 3 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:156:81: list
                            {
                            pushFollow(FOLLOW_list_in_form964);
                            list();
                            _fsp--;


                            }
                            break;
                        case 4 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:156:88: vector
                            {
                            pushFollow(FOLLOW_vector_in_form968);
                            vector();
                            _fsp--;


                            }
                            break;
                        case 5 :
                            // o:/clojure/basic-clojure-grammar/src/Clojure.g:156:97: map
                            {
                            pushFollow(FOLLOW_map_in_form972);
                            map();
                            _fsp--;


                            }
                            break;

                    }


                    }
                    break;
                case 6 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:157:9: macroForm
                    {
                    pushFollow(FOLLOW_macroForm_in_form984);
                    macroForm();
                    _fsp--;


                    }
                    break;
                case 7 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:158:9: dispatchMacroForm
                    {
                    pushFollow(FOLLOW_dispatchMacroForm_in_form994);
                    dispatchMacroForm();
                    _fsp--;


                    }
                    break;
                case 8 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:159:9: set
                    {
                    pushFollow(FOLLOW_set_in_form1004);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:162:1: macroForm : ( quoteForm | metaForm | derefForm | syntaxQuoteForm | {...}? unquoteSplicingForm | {...}? unquoteForm );
    public final void macroForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:162:10: ( quoteForm | metaForm | derefForm | syntaxQuoteForm | {...}? unquoteSplicingForm | {...}? unquoteForm )
            int alt5=6;
            switch ( input.LA(1) ) {
            case 29:
                {
                alt5=1;
                }
                break;
            case 30:
                {
                alt5=2;
                }
                break;
            case 31:
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
                    new NoViableAltException("162:1: macroForm : ( quoteForm | metaForm | derefForm | syntaxQuoteForm | {...}? unquoteSplicingForm | {...}? unquoteForm );", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:163:9: quoteForm
                    {
                    pushFollow(FOLLOW_quoteForm_in_macroForm1035);
                    quoteForm();
                    _fsp--;


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:164:9: metaForm
                    {
                    pushFollow(FOLLOW_metaForm_in_macroForm1045);
                    metaForm();
                    _fsp--;


                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:165:9: derefForm
                    {
                    pushFollow(FOLLOW_derefForm_in_macroForm1055);
                    derefForm();
                    _fsp--;


                    }
                    break;
                case 4 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:166:9: syntaxQuoteForm
                    {
                    pushFollow(FOLLOW_syntaxQuoteForm_in_macroForm1065);
                    syntaxQuoteForm();
                    _fsp--;


                    }
                    break;
                case 5 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:167:7: {...}? unquoteSplicingForm
                    {
                    if ( !( this.syntaxQuoteDepth > 0 ) ) {
                        throw new FailedPredicateException(input, "macroForm", " this.syntaxQuoteDepth > 0 ");
                    }
                    pushFollow(FOLLOW_unquoteSplicingForm_in_macroForm1075);
                    unquoteSplicingForm();
                    _fsp--;


                    }
                    break;
                case 6 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:168:7: {...}? unquoteForm
                    {
                    if ( !( this.syntaxQuoteDepth > 0 ) ) {
                        throw new FailedPredicateException(input, "macroForm", " this.syntaxQuoteDepth > 0 ");
                    }
                    pushFollow(FOLLOW_unquoteForm_in_macroForm1085);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:171:1: dispatchMacroForm : ( regexForm | varQuoteForm | {...}? lambdaForm );
    public final void dispatchMacroForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:171:18: ( regexForm | varQuoteForm | {...}? lambdaForm )
            int alt6=3;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==32) ) {
                switch ( input.LA(2) ) {
                case STRING:
                    {
                    alt6=1;
                    }
                    break;
                case 29:
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
                        new NoViableAltException("171:1: dispatchMacroForm : ( regexForm | varQuoteForm | {...}? lambdaForm );", 6, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("171:1: dispatchMacroForm : ( regexForm | varQuoteForm | {...}? lambdaForm );", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:172:9: regexForm
                    {
                    pushFollow(FOLLOW_regexForm_in_dispatchMacroForm1112);
                    regexForm();
                    _fsp--;


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:173:9: varQuoteForm
                    {
                    pushFollow(FOLLOW_varQuoteForm_in_dispatchMacroForm1122);
                    varQuoteForm();
                    _fsp--;


                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:174:9: {...}? lambdaForm
                    {
                    if ( !(!this.inLambda) ) {
                        throw new FailedPredicateException(input, "dispatchMacroForm", "!this.inLambda");
                    }
                    pushFollow(FOLLOW_lambdaForm_in_dispatchMacroForm1134);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:177:1: list : o= OPEN_PAREN ( form )* c= CLOSE_PAREN ;
    public final void list() throws RecognitionException {
        Token o=null;
        Token c=null;

        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:177:5: (o= OPEN_PAREN ( form )* c= CLOSE_PAREN )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:177:9: o= OPEN_PAREN ( form )* c= CLOSE_PAREN
            {
            o=(Token)input.LT(1);
            match(input,OPEN_PAREN,FOLLOW_OPEN_PAREN_in_list1155); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:177:22: ( form )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==OPEN_PAREN||(LA7_0>=SPECIAL_FORM && LA7_0<=BOOLEAN)||LA7_0==SYMBOL||(LA7_0>=KEYWORD && LA7_0<=COMMENT)||(LA7_0>=LAMBDA_ARG && LA7_0<=25)||LA7_0==27||(LA7_0>=29 && LA7_0<=32)) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:177:22: form
            	    {
            	    pushFollow(FOLLOW_form_in_list1157);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            c=(Token)input.LT(1);
            match(input,CLOSE_PAREN,FOLLOW_CLOSE_PAREN_in_list1163); 
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:180:1: vector : '[' ( form )* ']' ;
    public final void vector() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:180:7: ( '[' ( form )* ']' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:180:9: '[' ( form )* ']'
            {
            match(input,25,FOLLOW_25_in_vector1181); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:180:13: ( form )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==OPEN_PAREN||(LA8_0>=SPECIAL_FORM && LA8_0<=BOOLEAN)||LA8_0==SYMBOL||(LA8_0>=KEYWORD && LA8_0<=COMMENT)||(LA8_0>=LAMBDA_ARG && LA8_0<=25)||LA8_0==27||(LA8_0>=29 && LA8_0<=32)) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:180:13: form
            	    {
            	    pushFollow(FOLLOW_form_in_vector1183);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            match(input,26,FOLLOW_26_in_vector1186); 

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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:183:1: map : '{' ( form form )* '}' ;
    public final void map() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:183:4: ( '{' ( form form )* '}' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:183:9: '{' ( form form )* '}'
            {
            match(input,27,FOLLOW_27_in_map1205); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:183:13: ( form form )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==OPEN_PAREN||(LA9_0>=SPECIAL_FORM && LA9_0<=BOOLEAN)||LA9_0==SYMBOL||(LA9_0>=KEYWORD && LA9_0<=COMMENT)||(LA9_0>=LAMBDA_ARG && LA9_0<=25)||LA9_0==27||(LA9_0>=29 && LA9_0<=32)) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:183:14: form form
            	    {
            	    pushFollow(FOLLOW_form_in_map1208);
            	    form();
            	    _fsp--;

            	    pushFollow(FOLLOW_form_in_map1210);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            match(input,28,FOLLOW_28_in_map1214); 

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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:186:1: quoteForm : '\\'' form ;
    public final void quoteForm() throws RecognitionException {
         this.syntaxQuoteDepth++; 
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:189:5: ( '\\'' form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:189:8: '\\'' form
            {
            match(input,29,FOLLOW_29_in_quoteForm1247); 
            pushFollow(FOLLOW_form_in_quoteForm1249);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:192:1: metaForm : '^' form ;
    public final void metaForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:192:9: ( '^' form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:192:13: '^' form
            {
            match(input,30,FOLLOW_30_in_metaForm1263); 
            pushFollow(FOLLOW_form_in_metaForm1265);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:195:1: derefForm : '@' form ;
    public final void derefForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:195:10: ( '@' form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:195:13: '@' form
            {
            match(input,31,FOLLOW_31_in_derefForm1282); 
            pushFollow(FOLLOW_form_in_derefForm1284);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:198:1: syntaxQuoteForm : SYNTAX_QUOTE form ;
    public final void syntaxQuoteForm() throws RecognitionException {
         this.syntaxQuoteDepth++; 
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:201:5: ( SYNTAX_QUOTE form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:202:9: SYNTAX_QUOTE form
            {
            match(input,SYNTAX_QUOTE,FOLLOW_SYNTAX_QUOTE_in_syntaxQuoteForm1324); 
            pushFollow(FOLLOW_form_in_syntaxQuoteForm1326);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:205:1: unquoteForm : UNQUOTE form ;
    public final void unquoteForm() throws RecognitionException {
         this.syntaxQuoteDepth--; 
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:208:5: ( UNQUOTE form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:209:9: UNQUOTE form
            {
            match(input,UNQUOTE,FOLLOW_UNQUOTE_in_unquoteForm1366); 
            pushFollow(FOLLOW_form_in_unquoteForm1368);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:212:1: unquoteSplicingForm : UNQUOTE_SPLICING form ;
    public final void unquoteSplicingForm() throws RecognitionException {
         this.syntaxQuoteDepth--; 
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:215:5: ( UNQUOTE_SPLICING form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:216:9: UNQUOTE_SPLICING form
            {
            match(input,UNQUOTE_SPLICING,FOLLOW_UNQUOTE_SPLICING_in_unquoteSplicingForm1408); 
            pushFollow(FOLLOW_form_in_unquoteSplicingForm1410);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:219:1: set : '#' '{' ( form )* '}' ;
    public final void set() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:219:4: ( '#' '{' ( form )* '}' )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:219:9: '#' '{' ( form )* '}'
            {
            match(input,32,FOLLOW_32_in_set1429); 
            match(input,27,FOLLOW_27_in_set1431); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:219:17: ( form )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==OPEN_PAREN||(LA10_0>=SPECIAL_FORM && LA10_0<=BOOLEAN)||LA10_0==SYMBOL||(LA10_0>=KEYWORD && LA10_0<=COMMENT)||(LA10_0>=LAMBDA_ARG && LA10_0<=25)||LA10_0==27||(LA10_0>=29 && LA10_0<=32)) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/Clojure.g:219:17: form
            	    {
            	    pushFollow(FOLLOW_form_in_set1433);
            	    form();
            	    _fsp--;


            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match(input,28,FOLLOW_28_in_set1436); 

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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:222:1: regexForm : '#' STRING ;
    public final void regexForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:222:10: ( '#' STRING )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:222:13: '#' STRING
            {
            match(input,32,FOLLOW_32_in_regexForm1449); 
            match(input,STRING,FOLLOW_STRING_in_regexForm1451); 

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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:225:1: metadataForm : '#' '^' ( map | SYMBOL | KEYWORD | STRING ) ;
    public final void metadataForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:225:13: ( '#' '^' ( map | SYMBOL | KEYWORD | STRING ) )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:226:9: '#' '^' ( map | SYMBOL | KEYWORD | STRING )
            {
            match(input,32,FOLLOW_32_in_metadataForm1475); 
            match(input,30,FOLLOW_30_in_metadataForm1477); 
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:226:17: ( map | SYMBOL | KEYWORD | STRING )
            int alt11=4;
            switch ( input.LA(1) ) {
            case 27:
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
                    new NoViableAltException("226:17: ( map | SYMBOL | KEYWORD | STRING )", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:226:18: map
                    {
                    pushFollow(FOLLOW_map_in_metadataForm1480);
                    map();
                    _fsp--;


                    }
                    break;
                case 2 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:226:24: SYMBOL
                    {
                    match(input,SYMBOL,FOLLOW_SYMBOL_in_metadataForm1484); 

                    }
                    break;
                case 3 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:226:31: KEYWORD
                    {
                    match(input,KEYWORD,FOLLOW_KEYWORD_in_metadataForm1486); 

                    }
                    break;
                case 4 :
                    // o:/clojure/basic-clojure-grammar/src/Clojure.g:226:39: STRING
                    {
                    match(input,STRING,FOLLOW_STRING_in_metadataForm1488); 

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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:229:1: varQuoteForm : '#' '\\'' form ;
    public final void varQuoteForm() throws RecognitionException {
        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:229:13: ( '#' '\\'' form )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:230:9: '#' '\\'' form
            {
            match(input,32,FOLLOW_32_in_varQuoteForm1509); 
            match(input,29,FOLLOW_29_in_varQuoteForm1511); 
            pushFollow(FOLLOW_form_in_varQuoteForm1513);
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
    // o:/clojure/basic-clojure-grammar/src/Clojure.g:233:1: lambdaForm : '#' list ;
    public final void lambdaForm() throws RecognitionException {
        
        this.inLambda = true;

        try {
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:240:5: ( '#' list )
            // o:/clojure/basic-clojure-grammar/src/Clojure.g:240:7: '#' list
            {
            match(input,32,FOLLOW_32_in_lambdaForm1540); 
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
    public static final BitSet FOLLOW_form_in_file857 = new BitSet(new long[]{0x00000001EBBE2FD2L});
    public static final BitSet FOLLOW_LAMBDA_ARG_in_form890 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_form901 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMENT_in_form927 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_form937 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metadataForm_in_form947 = new BitSet(new long[]{0x000000000A002050L});
    public static final BitSet FOLLOW_SPECIAL_FORM_in_form952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYMBOL_in_form958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_list_in_form964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vector_in_form968 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_map_in_form972 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macroForm_in_form984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dispatchMacroForm_in_form994 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_form1004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quoteForm_in_macroForm1035 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metaForm_in_macroForm1045 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derefForm_in_macroForm1055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_syntaxQuoteForm_in_macroForm1065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unquoteSplicingForm_in_macroForm1075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unquoteForm_in_macroForm1085 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regexForm_in_dispatchMacroForm1112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varQuoteForm_in_dispatchMacroForm1122 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lambdaForm_in_dispatchMacroForm1134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OPEN_PAREN_in_list1155 = new BitSet(new long[]{0x00000001EBBE2FF0L});
    public static final BitSet FOLLOW_form_in_list1157 = new BitSet(new long[]{0x00000001EBBE2FF0L});
    public static final BitSet FOLLOW_CLOSE_PAREN_in_list1163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_vector1181 = new BitSet(new long[]{0x00000001EFBE2FD0L});
    public static final BitSet FOLLOW_form_in_vector1183 = new BitSet(new long[]{0x00000001EFBE2FD0L});
    public static final BitSet FOLLOW_26_in_vector1186 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_map1205 = new BitSet(new long[]{0x00000001FBBE2FD0L});
    public static final BitSet FOLLOW_form_in_map1208 = new BitSet(new long[]{0x00000001EBBE2FD0L});
    public static final BitSet FOLLOW_form_in_map1210 = new BitSet(new long[]{0x00000001FBBE2FD0L});
    public static final BitSet FOLLOW_28_in_map1214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_quoteForm1247 = new BitSet(new long[]{0x00000001EBBE2FD0L});
    public static final BitSet FOLLOW_form_in_quoteForm1249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_30_in_metaForm1263 = new BitSet(new long[]{0x00000001EBBE2FD0L});
    public static final BitSet FOLLOW_form_in_metaForm1265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_derefForm1282 = new BitSet(new long[]{0x00000001EBBE2FD0L});
    public static final BitSet FOLLOW_form_in_derefForm1284 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYNTAX_QUOTE_in_syntaxQuoteForm1324 = new BitSet(new long[]{0x00000001EBBE2FD0L});
    public static final BitSet FOLLOW_form_in_syntaxQuoteForm1326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNQUOTE_in_unquoteForm1366 = new BitSet(new long[]{0x00000001EBBE2FD0L});
    public static final BitSet FOLLOW_form_in_unquoteForm1368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNQUOTE_SPLICING_in_unquoteSplicingForm1408 = new BitSet(new long[]{0x00000001EBBE2FD0L});
    public static final BitSet FOLLOW_form_in_unquoteSplicingForm1410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_set1429 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_set1431 = new BitSet(new long[]{0x00000001FBBE2FD0L});
    public static final BitSet FOLLOW_form_in_set1433 = new BitSet(new long[]{0x00000001FBBE2FD0L});
    public static final BitSet FOLLOW_28_in_set1436 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_regexForm1449 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_STRING_in_regexForm1451 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_metadataForm1475 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_metadataForm1477 = new BitSet(new long[]{0x0000000008022080L});
    public static final BitSet FOLLOW_map_in_metadataForm1480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYMBOL_in_metadataForm1484 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KEYWORD_in_metadataForm1486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_metadataForm1488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_varQuoteForm1509 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_varQuoteForm1511 = new BitSet(new long[]{0x00000001EBBE2FD0L});
    public static final BitSet FOLLOW_form_in_varQuoteForm1513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_lambdaForm1540 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_list_in_lambdaForm1542 = new BitSet(new long[]{0x0000000000000002L});

}