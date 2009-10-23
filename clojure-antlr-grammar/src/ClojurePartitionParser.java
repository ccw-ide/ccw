// $ANTLR 3.0.1 o:/clojure/basic-clojure-grammar/src/ClojurePartition.g 2008-11-19 23:17:12

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.stringtemplate.*;
import org.antlr.stringtemplate.language.*;
import java.util.HashMap;
public class ClojurePartitionParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "PARTITION_STRING", "PARTITION_COMMENT", "PARTITION_CODE"
    };
    public static final int PARTITION_STRING=4;
    public static final int PARTITION_CODE=6;
    public static final int EOF=-1;
    public static final int PARTITION_COMMENT=5;

        public ClojurePartitionParser(TokenStream input) {
            super(input);
        }
        
    protected StringTemplateGroup templateLib =
      new StringTemplateGroup("ClojurePartitionParserTemplates", AngleBracketTemplateLexer.class);

    public void setTemplateLib(StringTemplateGroup templateLib) {
      this.templateLib = templateLib;
    }
    public StringTemplateGroup getTemplateLib() {
      return templateLib;
    }
    /** allows convenient multi-value initialization:
     *  "new STAttrMap().put(...).put(...)"
     */
    public static class STAttrMap extends HashMap {
      public STAttrMap put(String attrName, Object value) {
        super.put(attrName, value);
        return this;
      }
      public STAttrMap put(String attrName, int value) {
        super.put(attrName, new Integer(value));
        return this;
      }
    }

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "o:/clojure/basic-clojure-grammar/src/ClojurePartition.g"; }


    public static class file_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start file
    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:28:1: file : ( PARTITION_STRING | PARTITION_COMMENT | PARTITION_CODE )* ;
    public final file_return file() throws RecognitionException {
        file_return retval = new file_return();
        retval.start = input.LT(1);

        try {
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:28:5: ( ( PARTITION_STRING | PARTITION_COMMENT | PARTITION_CODE )* )
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:29:9: ( PARTITION_STRING | PARTITION_COMMENT | PARTITION_CODE )*
            {
            // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:29:9: ( PARTITION_STRING | PARTITION_COMMENT | PARTITION_CODE )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=PARTITION_STRING && LA1_0<=PARTITION_CODE)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // o:/clojure/basic-clojure-grammar/src/ClojurePartition.g:
            	    {
            	    if ( (input.LA(1)>=PARTITION_STRING && input.LA(1)<=PARTITION_CODE) ) {
            	        input.consume();
            	        errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recoverFromMismatchedSet(input,mse,FOLLOW_set_in_file141);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end file


 

    public static final BitSet FOLLOW_set_in_file141 = new BitSet(new long[]{0x0000000000000072L});

}