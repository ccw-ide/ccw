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
	

	private static final String ERROR_MESSAGE_NO_REPL_FOUND = "Impossible to connect to running REPL.";
	private static final String ERROR_MESSAGE_INTERNAL_ERROR = "Internal clojure-dev plugin error. Please file an issue in the tracker system";
	private static final String ERROR_MESSAGE_COMMUNICATION_ERROR = "Communication problem with the REPL. Would you consider kill it and launch a fresh one?";
	private static final String ERROR_MESSAGE_NULL_PREFIX = "Incorrect prefix found. Probably an error with clojure-dev plugin. Please file an issue in the tracker system";
	private static final String ERROR_MESSAGE_NO_NAMESPACE_FOUND = "clojure-dev was not available to guess the namespace this file is attached to. Please report a request for enhancement in the tracker system";
	private static final String ERROR_MESSAGE_TOO_MANY_COMPLETIONS = "Too many proposals found. Only first " + MAX_JAVA_SEARCH_RESULT_NUMBER + " found are shown";
	private static final String MESSAGE_JAVA_COMPLETION = "Completion for all available java methods";
	private static final String MESSAGE_CLOJURE_COMPLETION = "Completion for symbols visible from current namespace";
	
	private final AntlrBasedClojureEditor editor;
	private final ContentAssistant assistant;
	
	public ClojureProposalProcessor(AntlrBasedClojureEditor editor, ContentAssistant assistant) {
		this.editor = editor;
		this.assistant = assistant;
	}
	
	private String errorMessage;
	private String prefix;
	private int wordStart; // TODO rename this as prefixOffset

	/**
	 * Installs fields prefix and prefixOffset.
	 * @return true if computation ok, false if unable to proceed 
	 * */
	private boolean computePrefix(ITextViewer viewer, int offset) throws BadLocationException {
		wordStart = offset - 1;
		IDocument doc = viewer.getDocument();
		while (!invalidSymbolCharacter(doc.getChar(wordStart))) {
			wordStart--;
			if (wordStart < 0) break;
		}
		wordStart++;
		if (wordStart < 0) return false;
		prefix = doc.get(wordStart, offset - wordStart);
		System.out.println("found wordPrefix:'" + prefix + "'");
		return true; 
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		errorMessage = "";
		assistant.setStatusMessage("");
		try {
			if (!computePrefix(viewer, offset)) {
				return null;
			}
			
			final int PREFIX_MIN_LENGTH = 3;
			if (prefix.length() < PREFIX_MIN_LENGTH) {
				ClojuredevPlugin.logWarning("completion proposal asked for a prefix whose length "
						+ "is less than the authorized one (" + PREFIX_MIN_LENGTH + "):'" + prefix + "'");
				return null;
			}
			
			final String nsPart;
			final String symbolPrefix;
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
			
			final List<List> dynamicSymbols = dynamicComplete(nsPart, symbolPrefix, fullyQualified); //parse(doc.get());
			final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			// Add dynamic completion proposals
			for (List l: dynamicSymbols) {
				String s = (String) l.get(0);
//				if (s.startsWith(symbolPrefix)) {
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
					CompletionProposal cp = new CompletionProposal(s, wordStart, prefix.length(), s.length(), null, displayString, null, additionalString.toString());
					
					proposals.add(cp);
//				}
			}
			
			assistant.setStatusMessage(MESSAGE_CLOJURE_COMPLETION);
			final int WORD_START = wordStart;
			// Compute and add java completion proposal
			if (prefix.startsWith(".")) {
				final String methodPrefix = prefix.substring(1);
				System.out.println("method prefix:" + methodPrefix );
				boolean isPattern = (methodPrefix.contains("*") || methodPrefix.contains("?"));
				boolean autoAddEndWildcard = isPattern && !methodPrefix.endsWith("*");
				SearchPattern pattern = SearchPattern.createPattern(
						autoAddEndWildcard ? methodPrefix + "*" : methodPrefix,
						IJavaSearchConstants.METHOD, // | IJavaSearchConstants.FIELD,
//						IJavaSearchConstants.TYPE,
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
										WORD_START + 1, null));
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
			}
			if (fullyQualified && !prefix.startsWith(".")) {
				final String methodPrefix = prefix.substring(1);
				System.out.println("method prefix:" + methodPrefix);
				String patternStr = nsPart + "." + symbolPrefix;
				System.out.println("pattern: " + patternStr);
				boolean isPattern = (patternStr.contains("*") || patternStr.contains("?"));
				boolean autoAddEndWildcard = isPattern && !patternStr.endsWith("*");
				SearchPattern pattern = SearchPattern.createPattern(
						autoAddEndWildcard ? patternStr + "*" : patternStr,
						IJavaSearchConstants.METHOD, // | IJavaSearchConstants.FIELD,
//						IJavaSearchConstants.TYPE,
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
										nsPart + "/" + symbolPrefix,
										WORD_START, editor.getDeclaringNamespace()));
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
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
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
//			errorMessage = ERROR_MESSAGE_NO_NAMESPACE_FOUND;
			return Collections.emptyList();
		}
		if (prefix == null) {
//			errorMessage = ERROR_MESSAGE_NULL_PREFIX;
			return Collections.emptyList();
		}
		
		ClojureClient clojureClient = editor.getCorrespondingClojureClient();
		if (clojureClient == null) {
//			errorMessage = ERROR_MESSAGE_NO_REPL_FOUND;
			return Collections.emptyList();
		}
		
		Map result = (Map) clojureClient.remoteLoadRead("(clojuredev.debug.serverrepl/code-complete \"" + namespace + "\" \"" + prefix + "\" " + (findOnlyPublic ? "true" : "false") + ")");
		if (result == null) {
//			errorMessage = null;
			return Collections.emptyList();
		}
		
		if (result.get("response-type").equals(0)) {
			if (result.get("response") == null) {
//				errorMessage = ERROR_MESSAGE_INTERNAL_ERROR;
				return Collections.emptyList();
			} else {
//				errorMessage = null;
				return (List<List>) result.get("response");
			}
		} else {
//			errorMessage = ERROR_MESSAGE_COMMUNICATION_ERROR;
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


///////+++++++++
//IJavaProject javaProject = JavaCore.create(((IFile) editor.getEditorInput().getAdapter(IFile.class)).getProject());
//String packageName = ""; // set it to something if you want it to complete as being inside a package
//String start = "toArr"; // intial string to complete on (could eg. be "Col" to complete on Collection)
//IEvaluationContext context = javaProject.newEvaluationContext();
////ResultCollector rc = new ResultCollector(); // extend this to control what is provide as completions (e.g. to filter out everything but classes or packages)
//CompletionRequestor cr = new CompletionRequestor() {
//	private int counter;
//	@Override
//	public void beginReporting() {
//		super.beginReporting();
//		counter = 0;
//	}
//	@Override
//	public void accept(org.eclipse.jdt.core.CompletionProposal proposal) {
//		System.out.println("one more: " + new String(proposal.getCompletion()));
//		counter++;
//	}
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.core.CompletionRequestor#endReporting()
//	 */
//	@Override
//	public void endReporting() {
//		super.endReporting();
//		System.out.println("counter=" + counter);
//	}
//};
//	
//context.setPackageName("");
////rc.reset(offset, javaProject, null);
//
//try {
//    // cannot send in my own document as it won't compile as java - so we just send in
//    // the smallest snippet possible
//    context.codeComplete(start, start.length(), cr);
//} catch(JavaModelException e) {
//	ClojuredevPlugin.logError(e);
//}
////JavaCompletionProposal[] results = rc.getResults();
////
////// As all completions have made with the assumption on a empty (or almost empty) string
////// we move the replacementoffset on every proposol to fit nicely into our non-java code
////for (int i = 0; i < results.length; i++) {
////    JavaCompletionProposal proposal = results[i];
////    proposal.setReplacementOffset(offset-start.length());
////}
////Arrays.sort(results, JavaCompletionProposalComparator.getInstance());
////proposals.addAll(Arrays.asList(results)); 
//
//
////////-------------





////////////***************
////1. create a fake compilation unit (project is a java project)
//
//IClasspathEntry[] classpath;
//try {
//	classpath = javaProject.getRawClasspath();
//WorkingCopyOwner owner = new WorkingCopyOwner() {};
//ICompilationUnit unit = owner.newWorkingCopy("Foo", classpath, null,null);
////String text = "public class Foo { public void foo() { Sys } }";
//String text = "public class Foo { public void foo() { System.out.pri } }";
//unit.getBuffer().append(text);
//
////2. use the unit to do the completion
////unit.codeComplete(42, cr);
//unit.codeComplete(42, cr);
//
////where offset is the offset into the code snippet I want to complete and the requestor is a CompletionRequestor 
//
//} catch (JavaModelException e) {
//	ClojuredevPlugin.logError(e);
//}
//
//////////////\\\\\\\\\\\\\

