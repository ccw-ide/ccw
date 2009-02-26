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
import java.util.Map;

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

import clojuredev.debug.ClojureClient;
import clojuredev.lexers.ClojureLexer;
import clojuredev.lexers.ClojureParser;
import clojuredev.outline.NamespaceBrowser;

public class ClojureProposalProcessor implements IContentAssistProcessor {
	private static final String ERROR_MESSAGE_NO_REPL_FOUND = "Impossible to connect to running REPL.";
	private static final String ERROR_MESSAGE_INTERNAL_ERROR = "Internal clojure-dev plugin error. Please file an issue in the tracker system";
	private static final String ERROR_MESSAGE_COMMUNICATION_ERROR = "Communication problem with the REPL. Would you consider kill it and launch a fresh one?";
	private static final String ERROR_MESSAGE_NULL_PREFIX = "Incorrect prefix found. Probably an error with clojure-dev plugin. Please file an issue in the tracker system";
	private static final String ERROR_MESSAGE_NO_NAMESPACE_FOUND = "clojure-dev was not available to guess the namespace this file is attached to. Please report a request for enhancement in the tracker system";
	private final AntlrBasedClojureEditor editor;
	
	public ClojureProposalProcessor(AntlrBasedClojureEditor editor) {
		this.editor = editor;
	}
	
	private String errorMessage;

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
			while (!invalidSymbolCharacter(doc.getChar(wordStart))) {
				wordStart--;
				if (wordStart < 0) break;
			}
			wordStart++;
			if (wordStart < 0) return null;
			String prefix = doc.get(wordStart, offset - wordStart);
			System.out.println("found wordPrefix:'" + prefix + "'");
			
			String nsPart;
			String symbolPrefix;
			boolean fullyQualified = false;
			if (prefix.indexOf('/') > 0) {
			    String[] parts = prefix.split("/", 2);
			    nsPart = parts[0];
			    symbolPrefix = parts[1];
			    fullyQualified = true;
			} else {
			    nsPart = editor.getDeclaringNamespace();
			    symbolPrefix = prefix;
			}
			
			List<List> dynamicSymbols = dynamicComplete(nsPart, symbolPrefix, fullyQualified); //parse(doc.get());
			
			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			for (List l: dynamicSymbols) {
				String s = (String) l.get(0);
				if (s.startsWith(symbolPrefix)) {
					String displayString = s;
					StringBuilder additionalString = new StringBuilder();
					if (l.get(2) != null) {
						String ns = (String) (((Map) l.get(2)).get(NamespaceBrowser.KEYWORD_NS));
						if (ns != null && !ns.trim().equals(""))
							displayString += " - " + ns;

						String args = (String) (((Map) l.get(2)).get(NamespaceBrowser.KEYWORD_ARGLISTS));
						if (args != null && !args.trim().equals("")) {
							additionalString.append("<p><b>Arguments List(s)</b><br/>");
							
							String[] argsLines = args.split("\n");
							boolean firstLine = true;
							for (String line: argsLines) {
								if (line.startsWith("("))
									line = line.substring(1);
								if (line.endsWith(")"))
									line = line.substring(0, line.length() - 1);
								if (firstLine)
									firstLine = false;
								else
									additionalString.append("<br/>");
								additionalString.append(line);
							}
							additionalString.append("</p><br/>");
						}
						
						String docString = (String) (((Map) l.get(2)).get(NamespaceBrowser.KEYWORD_DOC));
						if (docString != null && !docString.trim().equals(""))
							additionalString.append("<p><b>Documentation</b><br/>").append(docString).append("</p>");
					}
					if (fullyQualified) {
					    s = nsPart + '/' + s;
					}
					CompletionProposal cp = new CompletionProposal(s, wordStart, prefix.length(), s.length(), null, displayString, null, additionalString.toString());
					
					proposals.add(cp);
				}
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}
	private boolean invalidSymbolCharacter(char c) {
		if (Character.isWhitespace(c))
			return true;
		char[] invalidChars = {'(', ')', '[', ']', '{', '}', '\'', '@', '~', '^', '`', '#', '"'};
		for (int i = 0; i < invalidChars.length; i++) {
			if (invalidChars[i] == c)
				return true;
		}
		return false;
	}
	private List<List> dynamicComplete(String namespace, String prefix, boolean findOnlyPublic) {
		if (namespace == null) {
			errorMessage = ERROR_MESSAGE_NO_NAMESPACE_FOUND;
			return Collections.emptyList();
		}
		if (prefix == null) {
			errorMessage = ERROR_MESSAGE_NULL_PREFIX;
			return Collections.emptyList();
		}
		
		ClojureClient clojureClient = editor.getCorrespondingClojureClient();
		if (clojureClient == null) {
			errorMessage = ERROR_MESSAGE_NO_REPL_FOUND;
			return Collections.emptyList();
		}
		
		Map result = (Map) clojureClient.remoteLoadRead("(clojuredev.debug.serverrepl/code-complete \"" + namespace + "\" \"" + prefix + "\" " + (findOnlyPublic ? "true" : "false") + ")");
		if (result == null) {
			errorMessage = null;
			return Collections.emptyList();
		}
		
		if (result.get("response-type").equals(0)) {
			if (result.get("response") == null) {
				errorMessage = ERROR_MESSAGE_INTERNAL_ERROR;
				return Collections.emptyList();
			} else {
				errorMessage = null;
				return (List<List>) result.get("response");
			}
		} else {
			errorMessage = ERROR_MESSAGE_COMMUNICATION_ERROR;
			return Collections.emptyList();
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
		return errorMessage;
	}

}
