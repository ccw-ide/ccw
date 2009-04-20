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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import clojuredev.ClojuredevPlugin;
import clojuredev.debug.ClojureClient;
import clojuredev.outline.NamespaceBrowser;
import clojuredev.util.ClojureDocUtils;

public class ClojureProposalProcessor implements IContentAssistProcessor {
	private static final int MAX_JAVA_SEARCH_RESULT_NUMBER = 200;
	private static final String ERROR_MESSAGE_TOO_MANY_COMPLETIONS = "Too many proposals found. Only first " + MAX_JAVA_SEARCH_RESULT_NUMBER + " found are shown";
	private static final String MESSAGE_JAVA_COMPLETION = "Completion for all available java methods";
	private static final String MESSAGE_CLOJURE_COMPLETION = "Completion for symbols visible from current namespace";
	
	private final AntlrBasedClojureEditor editor;
	private final ContentAssistant assistant;
	
	private String errorMessage;
	
	public static class PrefixInfo {
		public final String prefix;
		public final int prefixOffset;
		public final boolean fullyQualified;
		public final String nsPart;
		public final String symbolPrefix;
		public PrefixInfo(final AntlrBasedClojureEditor editor, final String prefix, final int prefixOffset) {
			this.prefix = prefix;
			this.prefixOffset = prefixOffset;
			if (prefix.indexOf('/') > 0) {
			    String[] parts = prefix.split("/", 2);
			    nsPart = parts[0];
			    symbolPrefix = parts[1];
			    fullyQualified = true;
			} else {
			    nsPart = editor.getDeclaringNamespace();
			    symbolPrefix = prefix;
			    fullyQualified = false;
			}
		}
	}

	public ClojureProposalProcessor(AntlrBasedClojureEditor editor, ContentAssistant assistant) {
		this.editor = editor;
		this.assistant = assistant;
	}
	
	/**
	 * Installs fields prefix and prefixOffset.
	 * @return true if computation ok, false if unable to proceed 
	 * */
	private PrefixInfo computePrefix(ITextViewer viewer, int offset) throws BadLocationException {
		String prefix = null;
		int prefixOffset = offset - 1;
		IDocument doc = viewer.getDocument();
		while (!invalidSymbolCharacter(doc.getChar(prefixOffset))) {
			prefixOffset--;
			if (prefixOffset < 0) break;
		}
		prefixOffset++;
		if (prefixOffset < 0) return null;
		prefix = doc.get(prefixOffset, offset - prefixOffset);
		System.out.println("found wordPrefix:'" + prefix + "'");
		return new PrefixInfo(editor, prefix, prefixOffset); 
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		errorMessage = "";
		assistant.setStatusMessage("");
		final PrefixInfo prefixInfo;
		try {
			prefixInfo = computePrefix(viewer, offset);
			if (prefixInfo == null) {
				return null;
			}
			
			final int PREFIX_MIN_LENGTH = 3;
			if (prefixInfo.prefix.length() < PREFIX_MIN_LENGTH) {
				ClojuredevPlugin.logWarning("completion proposal asked for a prefix whose length "
						+ "is less than the authorized one (" + PREFIX_MIN_LENGTH + "):'" + prefixInfo + "'");
				return null;
			}
			
			final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			
			proposals.addAll(computeClojureProposals(prefixInfo));
			assistant.setStatusMessage(MESSAGE_CLOJURE_COMPLETION);
			
			if (prefixInfo.prefix.startsWith(".")) {
				proposals.addAll(computeAndAddJavaInstanceMethodCompletionProposal(prefixInfo));
			}
			if (prefixInfo.fullyQualified && !prefixInfo.prefix.startsWith(".")) {
				proposals.addAll(computeAndAddJavaStaticMethodCompletionProposal(prefixInfo));
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private List<ICompletionProposal> computeClojureProposals(PrefixInfo prefixInfo) {
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		final String nsPart;
		final String symbolPrefix;
		boolean fullyQualified = false;
		if (prefixInfo.prefix.indexOf('/') > 0) {
		    String[] parts = prefixInfo.prefix.split("/", 2);
		    nsPart = parts[0];
		    symbolPrefix = parts[1];
		    fullyQualified = true;
		} else {
		    nsPart = editor.getDeclaringNamespace();
		    symbolPrefix = prefixInfo.prefix;
		}
		
		final List<List> dynamicSymbols = dynamicComplete(nsPart, symbolPrefix, fullyQualified); //parse(doc.get());
		// Add dynamic completion proposals
		for (List l: dynamicSymbols) {
			String s = (String) l.get(0);
	//		if (s.startsWith(symbolPrefix)) {
				String displayString = s;
				String additionalString = "";
				if (l.get(2) != null) {
					String ns = (String) (((Map) l.get(2)).get(NamespaceBrowser.KEYWORD_NS));
					if (ns != null && !ns.trim().equals(""))
						displayString += " - " + ns;
	
					additionalString = ClojureDocUtils.getHtmlVarDocInfo(l.get(2));
				}
				if (fullyQualified) {
				    s = nsPart + '/' + s;
				}
				CompletionProposal cp = new CompletionProposal(s, prefixInfo.prefixOffset, prefixInfo.prefix.length(), s.length(), null, displayString, null, additionalString.toString());
				
				proposals.add(cp);
	//		}
		}
		
		return proposals;
	}
	
	private List<ICompletionProposal> computeAndAddJavaInstanceMethodCompletionProposal(
			final PrefixInfo prefixInfo) {
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		
		final String methodPrefix = prefixInfo.prefix.substring(1);
		System.out.println("method prefix:" + methodPrefix );
		boolean isPattern = (methodPrefix.contains("*") || methodPrefix.contains("?"));
		boolean autoAddEndWildcard = isPattern && !methodPrefix.endsWith("*");
		SearchPattern pattern = SearchPattern.createPattern(
				autoAddEndWildcard ? methodPrefix + "*" : methodPrefix,
				IJavaSearchConstants.METHOD, // | IJavaSearchConstants.FIELD,
	//			IJavaSearchConstants.TYPE,
				IJavaSearchConstants.DECLARATIONS,
				isPattern 
					? SearchPattern.R_PATTERN_MATCH 
					: SearchPattern.R_PREFIX_MATCH);
		if (pattern != null) {
			IJavaProject editedFileProject = JavaCore.create(((IFile) editor.getEditorInput().getAdapter(IFile.class)).getProject());
			if (editedFileProject != null) {
				IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { editedFileProject });
				SearchRequestor requestor = new SearchRequestor() {
					private int counter;
					@Override
					public void beginReporting() {
						super.beginReporting();
						System.out.println("begin reporting");
						counter = 0;
					}
					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						counter++;
						if (counter >= MAX_JAVA_SEARCH_RESULT_NUMBER) {
							System.out.println("too much results (>" + MAX_JAVA_SEARCH_RESULT_NUMBER + "), throwing exception");
							throw new CoreException(Status.OK_STATUS);
						}
						proposals.add(new LazyCompletionProposal(
								(IMethod) match.getElement(),
								methodPrefix,
								prefixInfo.prefixOffset + 1, null));
					}
					@Override
					public void endReporting() {
						super.endReporting();
						System.out.println("end reporting : count=" + counter);
					}
					
				};
				SearchEngine searchEngine = new SearchEngine();
				try {
					searchEngine.search(
							pattern, 
							new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant() }, 
							scope, 
							requestor, 
							null /* no progress monitor */);
					assistant.setStatusMessage(MESSAGE_JAVA_COMPLETION);
				} catch (CoreException e) {
					if (e.getStatus() != Status.OK_STATUS) {
						ClojuredevPlugin.logWarning("java code proposal search error in clojure dev", e);
					} else {
						errorMessage = ERROR_MESSAGE_TOO_MANY_COMPLETIONS;
						assistant.setStatusMessage(ERROR_MESSAGE_TOO_MANY_COMPLETIONS);
					}
				}
			}
		}
		return proposals;
	}
	
	private List<ICompletionProposal> computeAndAddJavaStaticMethodCompletionProposal(
			final PrefixInfo prefixInfo) {
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
	
		final String methodPrefix = prefixInfo.prefix.substring(1);
		System.out.println("method prefix:" + methodPrefix);
		String patternStr = prefixInfo.nsPart + "." + prefixInfo.symbolPrefix;
		System.out.println("pattern: " + patternStr);
		boolean isPattern = (patternStr.contains("*") || patternStr.contains("?"));
		boolean autoAddEndWildcard = isPattern && !patternStr.endsWith("*");
		SearchPattern pattern = SearchPattern.createPattern(
				autoAddEndWildcard ? patternStr + "*" : patternStr,
				IJavaSearchConstants.METHOD, // | IJavaSearchConstants.FIELD,
	//			IJavaSearchConstants.TYPE,
				IJavaSearchConstants.DECLARATIONS, 
				isPattern 
					? SearchPattern.R_PATTERN_MATCH 
					: SearchPattern.R_PREFIX_MATCH);
		if (pattern != null) {
			IJavaProject editedFileProject = JavaCore.create(((IFile) editor.getEditorInput().getAdapter(IFile.class)).getProject());
			if (editedFileProject != null) {
				IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { editedFileProject });
				SearchRequestor requestor = new SearchRequestor() {
					private int counter;
					@Override
					public void beginReporting() {
						super.beginReporting();
						System.out.println("begin reporting");
						counter = 0;
					}
					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						counter++;
						if (counter >= MAX_JAVA_SEARCH_RESULT_NUMBER) {
							System.out.println("too much results (>" + MAX_JAVA_SEARCH_RESULT_NUMBER + "), throwing exception");
							throw new CoreException(Status.OK_STATUS);
						}
						proposals.add(new LazyCompletionProposal(
								(IMethod) match.getElement(),
								prefixInfo.nsPart + "/" + prefixInfo.symbolPrefix,
								prefixInfo.prefixOffset, editor.getDeclaringNamespace()));
					}
					@Override
					public void endReporting() {
						super.endReporting();
						System.out.println("end reporting : count=" + counter);
					}
					
				};
				SearchEngine searchEngine = new SearchEngine();
				try {
					searchEngine.search(
							pattern, 
							new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant() }, 
							scope, 
							requestor, 
							null /* no progress monitor */);
					assistant.setStatusMessage(MESSAGE_JAVA_COMPLETION);
				} catch (CoreException e) {
					if (e.getStatus() != Status.OK_STATUS) {
						ClojuredevPlugin.logWarning("java code proposal search error in clojure dev", e);
					} else {
						errorMessage = ERROR_MESSAGE_TOO_MANY_COMPLETIONS;
						assistant.setStatusMessage(ERROR_MESSAGE_TOO_MANY_COMPLETIONS);
					}
				}
			}
		}
		return proposals;
	}

	private class LazyCompletionProposal implements ICompletionProposal {
		private final IMethod method;
		private final String prefix;
		private final int prefixOffset;
		private final String ns;
		private String displayString; 
		private CompletionProposal completionProposal;
		
		
		private CompletionProposal getCompletionProposal() {
			if (completionProposal == null) {
				
				String replacementString = "";
				int separator = prefix.indexOf('/');
				if (separator > 0) {
					IType methodType = method.getDeclaringType();
					String methodTypeName = methodType.getElementName();
					
					String classPrefix = prefix.substring(0, separator);
					boolean fullyQualified = classPrefix.contains((CharSequence) ".");
					if (fullyQualified) {
						replacementString = methodType.getFullyQualifiedName(); 
					} else {
						List<List> dynamicSymbols = 
							(ns!= null) 
								? dynamicComplete(ns, methodTypeName, false)
								: null;

						if (dynamicSymbols != null) {
							// check that the class is indeed available unqualified from the namespace
							// if not, place the qualification
							boolean found = false;
							for (List symbolData: dynamicSymbols) {
								if (symbolData.get(0).equals(methodTypeName)) {
									// TODO check also that it is a real class name, not a function or macro etc.
									replacementString = methodType.getElementName();
									found = true;
									break;
								}
							}
							// If class name not found in ns symbols, use the fully qualified name
							if (!found) {
								replacementString = methodType.getFullyQualifiedName();
							}
						} else {
							replacementString = methodType.getElementName();
						}
					}
					replacementString += "/" + method.getElementName();
				} else {
					replacementString = method.getElementName();
				}
				String additionalString;
				String javadoc = new ProposalInfo(method).getInfo(null);
				if (javadoc != null) {
					additionalString = javadoc;
				} else {
					additionalString = "No javadoc found";
				}
				
				completionProposal = new CompletionProposal(
						replacementString, prefixOffset, prefix.length(), replacementString.length(), 
						null, 
						getDisplayString(), 
						null, 
						additionalString);
			}
			return completionProposal;
		}
		public LazyCompletionProposal(IMethod method, String methodPrefix, int methodPrefixOffset, String ns) {
			this.method = method;
			this.prefix = methodPrefix;
			this.prefixOffset = methodPrefixOffset;
			this.ns = ns;
		}

		public void apply(IDocument document) {
			getCompletionProposal().apply(document);
		}

		public String getAdditionalProposalInfo() {
			return getCompletionProposal().getAdditionalProposalInfo();
		}

		public IContextInformation getContextInformation() {
			return getCompletionProposal().getContextInformation();
		}

		public String getDisplayString() {
			if (displayString == null) {
				displayString = method.getElementName() 
					+ " - " + method.getDeclaringType().getElementName() 
					+ " (" + method.getDeclaringType().getPackageFragment().getElementName() + ")";

			}
			return displayString;
		}

		public Image getImage() {
			return null;
		}

		public Point getSelection(IDocument document) {
			return getCompletionProposal().getSelection(document);
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
			return Collections.emptyList();
		}
		if (prefix == null) {
			return Collections.emptyList();
		}
		
		ClojureClient clojureClient = editor.getCorrespondingClojureClient();
		if (clojureClient == null) {
			return Collections.emptyList();
		}
		
		Map result = (Map) clojureClient.remoteLoadRead("(clojuredev.debug.serverrepl/code-complete \"" + namespace + "\" \"" + prefix + "\" " + (findOnlyPublic ? "true" : "false") + ")");
		if (result == null) {
			return Collections.emptyList();
		}
		
		if (result.get("response-type").equals(0)) {
			if (result.get("response") == null) {
				return Collections.emptyList();
			} else {
				return (List<List>) result.get("response");
			}
		} else {
			return Collections.emptyList();
		}
	}
//	private List<String> getMappedJavaClasses(String namespace, String prefix, boolean findOnlyPublic) {
//		if (namespace == null) {
//			errorMessage = ERROR_MESSAGE_NO_NAMESPACE_FOUND;
//			return Collections.emptyList();
//		}
//		if (prefix == null) {
//			errorMessage = ERROR_MESSAGE_NULL_PREFIX;
//			return Collections.emptyList();
//		}
//		
//		ClojureClient clojureClient = editor.getCorrespondingClojureClient();
//		if (clojureClient == null) {
//			errorMessage = ERROR_MESSAGE_NO_REPL_FOUND;
//			return Collections.emptyList();
//		}
//		
//		Map result = (Map) clojureClient.remoteLoadRead("(clojuredev.debug.serverrepl/code-complete \"" + namespace + "\" \"" + prefix + "\" " + (findOnlyPublic ? "true" : "false") + ")");
//		if (result == null) {
//			errorMessage = null;
//			return Collections.emptyList();
//		}
//		
//		if (result.get("response-type").equals(0)) {
//			if (result.get("response") == null) {
//				errorMessage = ERROR_MESSAGE_INTERNAL_ERROR;
//				return Collections.emptyList();
//			} else {
//				errorMessage = null;
//				return (List<List>) result.get("response");
//			}
//		} else {
//			errorMessage = ERROR_MESSAGE_COMMUNICATION_ERROR;
//			return Collections.emptyList();
//		}
//	}
//
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
		return null;
//		return new char[] { '/' };
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
//		return new char[] { '.' };
	}

	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
