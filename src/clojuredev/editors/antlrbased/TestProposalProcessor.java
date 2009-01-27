/*******************************************************************************
 * Copyright (c) 2009 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
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

public class TestProposalProcessor implements IContentAssistProcessor {
	ClojureParser parser;
	
	public TestProposalProcessor() {
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

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return new IContextInformation[] {
				new IContextInformation() {

					public String getContextDisplayString() {
						return "my context";
					}

					public Image getImage() {
						return null;
					}

					public String getInformationDisplayString() {
						return "the information for the context";
					}}
		};
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '/' };
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '.' };
	}

	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
