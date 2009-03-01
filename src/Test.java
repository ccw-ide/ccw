import java.io.InputStream;
import java.util.List;

import org.antlr.runtime.*;

import clojuredev.lexers.ClojureLexer;
import clojuredev.lexers.ClojureParser;

public class Test {
    public static void main(String[] args) throws Exception {
//        // Create an input character stream from standard in
//    	InputStream fileIO =  Thread.currentThread().getContextClassLoader().getResourceAsStream("ants.clj");
//        ANTLRInputStream input = new ANTLRInputStream(fileIO);
//        // Create an ExprLexer that feeds from that stream
//        ClojurePartitionLexer lexer = new ClojurePartitionLexer(input);
//        
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//
//        List<CommonToken> toks = tokens.getTokens();
//        for (CommonToken t: toks) {
//        	System.out.println("line [" + t.getLine() + "], col [" 
//        			+ t.getCharPositionInLine() + "], type [" + t.getType() + "] :'" + t.getText() + "'");
//        }
		ClojureLexer lex = new ClojureLexer(new ANTLRStringStream(text));
       	CommonTokenStream tokens = new CommonTokenStream(lex);

       	ClojureParser parser = new ClojureParser(tokens);
		
        try {
        	System.out.println("begin parse");
            parser.file();
        	System.out.println("end parse");
        } catch (RecognitionException e)  {
            e.printStackTrace();
        }
		
    
    }
}