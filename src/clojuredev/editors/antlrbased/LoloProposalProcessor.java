package clojuredev.editors.antlrbased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

import clojuredev.lexers.ClojureLexer;
import clojuredev.lexers.ClojureParser;

public class LoloProposalProcessor implements IContentAssistProcessor {
	ClojureParser parser;
	
	public LoloProposalProcessor() {
		this.parser = parser;
	}

	private List<String> parse(String text) {
		ClojureLexer lex = new ClojureLexer(new ANTLRStringStream(text));
       	CommonTokenStream tokens = new CommonTokenStream(lex);

       	ClojureParser parser = new ClojureParser(tokens);
		
        try {
        	System.out.println("begin parse");
            parser.file();
        	System.out.println("end parse");
        	return Collections.unmodifiableList(parser.getCollectedSymbols());
        } catch (RecognitionException e)  {
            e.printStackTrace();
            return Collections.emptyList();
        }
		
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		int wordStart = offset - 1;
		IDocument doc = viewer.getDocument();
		try {
			
			List<String> symbols = parse(doc.get());
			
			while (doc.getChar(wordStart) != ' ') {
				wordStart--;
				if (wordStart < 0) break;
			}
			wordStart++;
			if (wordStart < 0) return null;
			String wordPrefix = doc.get(wordStart, offset - wordStart);
			System.out.println("found wordPrefix:'" + wordPrefix + "'");
			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			for (String s: symbols) {
				if (s.startsWith(wordPrefix))
					proposals.add(new CompletionProposal(s, wordStart, wordPrefix.length(), wordPrefix.length()));
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return new IContextInformation[] {
				new IContextInformation() {

					@Override
					public String getContextDisplayString() {
						return "my context";
					}

					@Override
					public Image getImage() {
						return null;
					}

					@Override
					public String getInformationDisplayString() {
						return "the information for the context";
					}}
		};
//		// TODO Auto-generated method stub
//		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '/' };
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '.' };
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
