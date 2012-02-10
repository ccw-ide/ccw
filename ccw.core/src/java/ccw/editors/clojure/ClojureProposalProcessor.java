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
package ccw.editors.clojure;

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
import org.eclipse.jdt.core.IPackageFragment;
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
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;

import ccw.CCWPlugin;
import ccw.repl.NamespaceBrowser;
import ccw.repl.REPLView;
import ccw.util.ClojureDocUtils;
import clojure.tools.nrepl.Connection;
import clojure.tools.nrepl.Connection.Response;

public class ClojureProposalProcessor implements IContentAssistProcessor {
	private static final int MAX_JAVA_SEARCH_RESULT_NUMBER = 50;
	private static final String ERROR_MESSAGE_TOO_MANY_COMPLETIONS = "Too many proposals found. Only first " + MAX_JAVA_SEARCH_RESULT_NUMBER + " found are shown";
	private static final String MESSAGE_JAVA_COMPLETION = "Completion for all available java methods";
	private static final String MESSAGE_CLOJURE_COMPLETION = "Completion for symbols visible from current namespace";
	
	private final IClojureEditor editor;
	private final ContentAssistant assistant;
	
	private String errorMessage;

	final int JAVA_PREFIX_MIN_LENGTH = 4;

	public static class PrefixInfo {
		public final String prefix;
		public final int prefixOffset;
		public final boolean fullyQualified;
		public final String nsPart;
		public final String symbolPrefix;
		public PrefixInfo(final IClojureEditor editor, final String prefix, final int prefixOffset) {
			this.prefix = prefix;
			this.prefixOffset = prefixOffset;
			if (prefix.indexOf('/') > 0) {
			    String[] parts = prefix.split("/", 2);
			    nsPart = parts[0];
			    symbolPrefix = parts[1];
			    fullyQualified = true;
			} else {
			    nsPart = editor.findDeclaringNamespace();
			    symbolPrefix = prefix;
			    fullyQualified = false;
			}
		}
	}

	public ClojureProposalProcessor(IClojureEditor editor, ContentAssistant assistant) {
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
		if (offset == 0) {
			return new PrefixInfo(editor, "", 0);
		}
		while (!invalidSymbolCharacter(doc.getChar(prefixOffset))) {
			prefixOffset--;
			if (prefixOffset < 0) break;
		}
		prefixOffset++;
		if (prefixOffset < 0) return null;
		prefix = doc.get(prefixOffset, offset - prefixOffset);
//		System.out.println("found wordPrefix:'" + prefix + "'");
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
	
			final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			
			if (prefixInfo.prefix.startsWith(".")) {
				//    chercher completion parmi les methodes d'instances des classes declarees dans le namespace courant
			    //      (possibilite d'ecriture du nom de la methode en camelCase)
				// virer l'utilisation de la recherche totale et aller chercher les methodes directement
				// sur le type
				proposals.addAll(computeAndAddJavaInstanceMethodCompletionProposal(prefixInfo));
			} else {
				if (prefixInfo.fullyQualified) {
					proposals.addAll(computeClojureFullyQualifiedSymbolsProposals(prefixInfo));
					//	        chercher completion parmi les methodes statiques (de classes) de classes dont le nom de classe matche ce qui est avant le /, et le nom de m�thode ce qui est apres le /
			        //	          (possibilite d'ecriture du nom de la classe et/ou de la m�thode en camelCase)
					// virer l'utilisation de la recherche totale et aller chercher les methodes directement
					// sur le type
					proposals.addAll(computeAndAddJavaStaticMethodCompletionProposal(prefixInfo, JavaSearchType.STATIC_METHOD));
				} else {
					 /*
			        si contient au moins un point
			            considerer que ce qui est avant le dernier point doit matcher une classe
			            considerer que ce qui est apres le dernier point doit matcher une m�thode d'instance
			            chercher completion parmi les noms de classes du classpath avec la methode
			                si des points
			                    parmi les noms de classes en considerant que c'est totalement qualifie
			                      (possibilite d'ecriture en camel case)
			                sinon
			                    parmi les noms de classes en cherchant dans n'importe quel package
			                      (possibilite d'ecriture en camel case)
			                fin si
			        fin si
				 */
				
				proposals.addAll(computeClojureNamespacesProposals(prefixInfo));

				proposals.addAll(computeClojureSymbolsProposals(prefixInfo));

				
				proposals.addAll(computeAndAddJavaStaticMethodCompletionProposal(prefixInfo, JavaSearchType.PACKAGE));
					/*
				        chercher completion parmi les noms de classes du classpath
				            si des points
				                parmi les noms de classes en considerant que c'est totalement qualifie
				                  (possibilite d'ecriture en camel case)
				            sinon
				                parmi les noms de classes en cherchant dans n'importe quel package
				                  (possibilite d'ecriture en camel case)
				            fin si
					 */
					proposals.addAll(computeAndAddJavaStaticMethodCompletionProposal(prefixInfo, JavaSearchType.CLASS));
				}
			}
			
			assistant.setStatusMessage(MESSAGE_CLOJURE_COMPLETION);
			
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private List<ICompletionProposal> computeClojureFullyQualifiedSymbolsProposals(PrefixInfo prefixInfo) {
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		final List<List> dynamicSymbols = dynamicComplete(prefixInfo.nsPart, 
				prefixInfo.symbolPrefix, editor, prefixInfo.fullyQualified); //parse(doc.get());
		// Add dynamic completion proposals
		for (List l: dynamicSymbols) {
			String s = (String) l.get(0);
			String displayString = s;
			String additionalString = "";
			if (l.get(2) != null) {
				String ns = (String) (((Map) l.get(2)).get(NamespaceBrowser.KEYWORD_NS));
				if (ns != null && !ns.trim().equals(""))
					displayString += " - " + ns;

				additionalString = ClojureDocUtils.getHtmlVarDocInfo(l.get(2));
			}
			if (prefixInfo.fullyQualified) {
			    s = prefixInfo.nsPart + '/' + s;
			}
			String replacement = ((String) l.get(1)).substring(2);
			CompletionProposal cp = new CompletionProposal(replacement, prefixInfo.prefixOffset, 
					prefixInfo.prefix.length(), replacement.length(), null, displayString, null, 
					additionalString.toString());
			
			proposals.add(cp);
		}
		
		return proposals;
	}
	
	private List<ICompletionProposal> computeClojureSymbolsProposals(PrefixInfo prefixInfo) {
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		final List<List> dynamicSymbols = dynamicComplete(prefixInfo.nsPart, 
				prefixInfo.symbolPrefix, editor, prefixInfo.fullyQualified); //parse(doc.get());
		// Add dynamic completion proposals
		for (List l: dynamicSymbols) {
			String s = (String) l.get(0);
			String displayString = s;
			String additionalString = "";
			if (l.get(2) != null) {
				String ns = (String) (((Map) l.get(2)).get(NamespaceBrowser.KEYWORD_NS));
				if (ns != null && !ns.trim().equals(""))
					displayString += " - " + ns;

				additionalString = ClojureDocUtils.getHtmlVarDocInfo(l.get(2));
			}
			if (prefixInfo.fullyQualified) {
			    s = prefixInfo.nsPart + '/' + s;
			}
			CompletionProposal cp = new CompletionProposal(s, prefixInfo.prefixOffset, 
					prefixInfo.prefix.length(), s.length(), null, displayString, null, 
					additionalString.toString());
			
			proposals.add(cp);
		}
		
		return proposals;
	}
	
	private List<ICompletionProposal> computeClojureNamespacesProposals(PrefixInfo prefixInfo) {
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		final List<List> dynamicSymbols = dynamicNamespaceComplete(prefixInfo.prefix);
		// Add dynamic completion proposals
		for (List l: dynamicSymbols) {
			String s = (String) l.get(0);
			String displayString = s + " (namespace)";
			String additionalString = "";
			if (l.get(2) != null) {
				String ns = (String) (((Map) l.get(2)).get(NamespaceBrowser.KEYWORD_NS));
				if (ns != null && !ns.trim().equals(""))
					displayString += " - " + ns;

				additionalString = ClojureDocUtils.getHtmlVarDocInfo(l.get(2));
			}
			if (prefixInfo.fullyQualified) {
			    s = prefixInfo.nsPart + '/' + s;
			}
			CompletionProposal cp = new CompletionProposal(s, prefixInfo.prefixOffset, 
					prefixInfo.prefix.length(), s.length(), null, displayString, null, 
					additionalString);
			
			proposals.add(cp);
		}
		
		return proposals;
	}
	
	private List<ICompletionProposal> computeAndAddJavaInstanceMethodCompletionProposal(
			final PrefixInfo prefixInfo) {
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		
		if (! checkJavaPrefixLength(prefixInfo)) {
			return Collections.emptyList();
		}

		final String methodPrefix = prefixInfo.prefix.substring(1);
//		System.out.println("method prefix:" + methodPrefix );
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
			IJavaProject editedFileProject = editor.getAssociatedProject();
			if (editedFileProject != null) {
				IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { editedFileProject });
				SearchRequestor requestor = new SearchRequestor() {
					private int counter;
					@Override
					public void beginReporting() {
						super.beginReporting();
//						System.out.println("begin reporting");
						counter = 0;
					}
					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						counter++;
						if (counter >= MAX_JAVA_SEARCH_RESULT_NUMBER) {
//							System.out.println("too much results (>" + MAX_JAVA_SEARCH_RESULT_NUMBER + "), throwing exception");
							throw new CoreException(Status.OK_STATUS);
						}
						proposals.add(new MethodLazyCompletionProposal(
								(IMethod) match.getElement(),
								methodPrefix,
								prefixInfo.prefixOffset + 1, null,
								editor));
					}
					@Override
					public void endReporting() {
						super.endReporting();
//						System.out.println("end reporting : count=" + counter);
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
						CCWPlugin.logWarning("java code proposal search error in clojure dev", e);
					} else {
						errorMessage = ERROR_MESSAGE_TOO_MANY_COMPLETIONS;
						assistant.setStatusMessage(ERROR_MESSAGE_TOO_MANY_COMPLETIONS);
					}
				}
			}
		}
		return proposals;
	}
	
	static enum JavaSearchType {
		STATIC_METHOD { 
			public int[] searchFor() { return new int[] { IJavaSearchConstants.METHOD }; }
			public int[] matchRule() { return new int[] { SearchPattern.R_CAMELCASE_MATCH /* | SearchPattern.R_CASE_SENSITIVE */}; }
			public String[] patternStr(PrefixInfo prefixInfo) {
				return new String[] { prefixInfo.nsPart + "." + prefixInfo.symbolPrefix };
			}
			public AbstractLazyCompletionProposal lazyCompletionProposal(PrefixInfo prefixInfo, IClojureEditor editor, SearchMatch match) {
				return new MethodLazyCompletionProposal(
						(IMethod) match.getElement(),
						prefixInfo.nsPart + "/" + prefixInfo.symbolPrefix,
						prefixInfo.prefixOffset, editor.findDeclaringNamespace(),
						editor);
			}
			public int[] prefixMinLength() { return new int[] { 2 }; }
		},
		CLASS {
			public int[] searchFor() { return new int[] { IJavaSearchConstants.TYPE, IJavaSearchConstants.TYPE }; }
			public int[] matchRule() { return new int[] { SearchPattern.R_CAMELCASE_MATCH /* | SearchPattern.R_CASE_SENSITIVE */, SearchPattern.R_PATTERN_MATCH /* | SearchPattern.R_CASE_SENSITIVE */ }; }
			public String[] patternStr(PrefixInfo prefixInfo) {
				return new String[] { prefixInfo.prefix, prefixInfo.prefix + "*" };
			}
			public AbstractLazyCompletionProposal lazyCompletionProposal(PrefixInfo prefixInfo, IClojureEditor editor, SearchMatch match) {
				return new ClassLazyCompletionProposal(
						(IType) match.getElement(),
						prefixInfo.prefix,
						prefixInfo.prefixOffset, editor.findDeclaringNamespace(),
						editor);
			}
			public int[] prefixMinLength() { return new int[] { 1, 3 }; }
		},
		PACKAGE {
			public int[] searchFor() { return new int[] { IJavaSearchConstants.PACKAGE }; }
			public int[] matchRule() { return new int[] { SearchPattern.R_PATTERN_MATCH /* | SearchPattern.R_CASE_SENSITIVE */ }; }
			public String[] patternStr(PrefixInfo prefixInfo) {
				return new String[] { prefixInfo.prefix + "*" };
			}
			public AbstractLazyCompletionProposal lazyCompletionProposal(PrefixInfo prefixInfo, IClojureEditor editor, SearchMatch match) {
				return new PackageLazyCompletionProposal(
						(IPackageFragment) match.getElement(),
						prefixInfo.prefix,
						prefixInfo.prefixOffset, editor.findDeclaringNamespace(),
						editor);
			}
			public int[] prefixMinLength() { return new int[] { 1 }; }
		};
		
		public abstract int[] searchFor();
		public abstract String[] patternStr(PrefixInfo prefixInfo);
		public abstract AbstractLazyCompletionProposal lazyCompletionProposal(PrefixInfo prefixInfo, IClojureEditor editor, SearchMatch match);
		public abstract int[] matchRule();
		/** try to match only if the size of the prefix is equal or greater than this. */
		public abstract int[] prefixMinLength();
	}
	
	private boolean checkJavaPrefixLength(PrefixInfo prefixInfo) {
		if (prefixInfo.prefix.length() < JAVA_PREFIX_MIN_LENGTH) {
			CCWPlugin.logWarning("completion proposal asked for a prefix whose length "
					+ "is less than the authorized one (" + JAVA_PREFIX_MIN_LENGTH + "):'" + prefixInfo + "' for java completions");
			return false;
		} else {
			return true;
		}
	}
	
	private List<ICompletionProposal> computeAndAddJavaStaticMethodCompletionProposal(
			final PrefixInfo prefixInfo, final JavaSearchType searchType) {
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
	
		if (! checkJavaPrefixLength(prefixInfo)) {
			return Collections.emptyList();
		}

		int nbPatterns = searchType.matchRule().length;
		List<SearchPattern> combinedPattern = new ArrayList<SearchPattern>();
		
		for (int i = 0; i < nbPatterns; i++) {
			if (prefixInfo.prefix.length() < searchType.prefixMinLength()[i]) {
				continue;
			}
			SearchPattern pattern = SearchPattern.createPattern(
					searchType.patternStr(prefixInfo)[i],
					searchType.searchFor()[i],
					IJavaSearchConstants.DECLARATIONS, 
					searchType.matchRule()[i]
					);
			combinedPattern.add(pattern);
		}
		
		IJavaProject editedFileProject = editor.getAssociatedProject();
		if (editedFileProject != null) {
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { editedFileProject });
			SearchRequestor requestor = new SearchRequestor() {
				@Override
				public void acceptSearchMatch(SearchMatch match) throws CoreException {
					proposals.add(searchType.lazyCompletionProposal(prefixInfo, editor, match));
				}
			};
			SearchEngine searchEngine = new SearchEngine();
			try {
				for (SearchPattern pattern: combinedPattern) {
					if (pattern != null) {
						searchEngine.search(
								pattern, 
								new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant() }, 
								scope, 
								requestor, 
								null /* no progress monitor */);
						assistant.setStatusMessage(MESSAGE_JAVA_COMPLETION);
					}
				}
			} catch (CoreException e) {
				if (e.getStatus() != Status.OK_STATUS) {
					CCWPlugin.logWarning("java code proposal search error in clojure dev", e);
				} else {
					errorMessage = ERROR_MESSAGE_TOO_MANY_COMPLETIONS;
					assistant.setStatusMessage(ERROR_MESSAGE_TOO_MANY_COMPLETIONS);
				}
			}
		}
		return proposals;
	}
	
	private static abstract class AbstractLazyCompletionProposal implements ICompletionProposal, ICompletionProposalExtension6 {
		private final IMethod method;
		private final String prefix;
		private final int prefixOffset;
		private final String ns;
		private final IClojureEditor editor;
		private String displayString; 
		private CompletionProposal completionProposal;

		protected static StyledString.Styler javaStyler = new StyledString.Styler() {
			public void applyStyles(TextStyle textStyle) {
				textStyle.font = CCWPlugin.getDefault().getJavaSymbolFont();
			}
		};
		
		public StyledString getStyledDisplayString() {
			return new StyledString(getDisplayString(), javaStyler);
		}
		
		protected CompletionProposal getCompletionProposal() {
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
								? dynamicComplete(ns, methodTypeName, editor, false)
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
		
		public AbstractLazyCompletionProposal(IMethod method, String methodPrefix, int methodPrefixOffset, String ns, IClojureEditor editor) {
			this.method = method;
			this.prefix = methodPrefix;
			this.prefixOffset = methodPrefixOffset;
			this.ns = ns;
			this.editor = editor;
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
	
	private static class MethodLazyCompletionProposal extends AbstractLazyCompletionProposal {
		public MethodLazyCompletionProposal(IMethod method, String methodPrefix, int methodPrefixOffset, String ns, IClojureEditor editor) {
			super(method, methodPrefix, methodPrefixOffset, ns, editor);
		}
		public Image getImage() {
			return CCWPlugin.getDefault().getImageRegistry().get(CCWPlugin.PUBLIC_FUNCTION);
		}
	}
	
	private static class ClassLazyCompletionProposal extends AbstractLazyCompletionProposal {
		private final IType method;
		private final String prefix;
		private final int prefixOffset;
		private final String ns;
		private final IClojureEditor editor;
		private String displayString; 
		private CompletionProposal completionProposal;
		
		public Image getImage() {
			return CCWPlugin.getDefault().getImageRegistry().get(CCWPlugin.CLASS);
		}
		
		protected CompletionProposal getCompletionProposal() {
			if (completionProposal == null) {
				
				String replacementString = null;
				Connection client = editor.getCorrespondingREPL().getToolingConnection();
				if (client != null) {
					Map result = (Map)client.send("op", "eval", "code", "(ccw.debug.serverrepl/imported-class \"" + ns + "\" \"" + method.getElementName() + "\")").values().get(0);
					if (result != null && result.get("response-type").equals(0) && result.get("response") != null) {
						replacementString = (String) result.get("response");
					}
				}
				if (replacementString == null) {
					replacementString = method.getFullyQualifiedName();
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
		
		public ClassLazyCompletionProposal(IType method, String methodPrefix, int methodPrefixOffset, String ns, IClojureEditor editor) {
			super(null, methodPrefix, methodPrefixOffset, ns, editor); // TODO vraiment nulle comme technique !
			this.method = method;
			this.prefix = methodPrefix;
			this.prefixOffset = methodPrefixOffset;
			this.ns = ns;
			this.editor = editor;
		}
		
		public String getDisplayString() {
			if (displayString == null) {
				displayString = method.getElementName() 
					+ " (" + method.getPackageFragment().getElementName() + ")";

			}
			return displayString;
		}
	}
	
	private static class PackageLazyCompletionProposal extends AbstractLazyCompletionProposal {
		private final IPackageFragment method;
		private final String prefix;
		private final int prefixOffset;
		private final String ns;
		private final IClojureEditor editor;
		private String displayString; 
		private CompletionProposal completionProposal;
		
		public Image getImage() {
			return CCWPlugin.getDefault().getImageRegistry().get(CCWPlugin.NS);
		}

		protected CompletionProposal getCompletionProposal() {
			if (completionProposal == null) {
				
				String replacementString = method.getElementName();

				String additionalString = "";
				
				completionProposal = new CompletionProposal(
						replacementString, prefixOffset, prefix.length(), replacementString.length(), 
						null, 
						getDisplayString(), 
						null, 
						additionalString);
			}
			return completionProposal;
		}
		
		public PackageLazyCompletionProposal(IPackageFragment method, String methodPrefix, int methodPrefixOffset, String ns, IClojureEditor editor) {
			super(null, methodPrefix, methodPrefixOffset, ns, editor); // TODO vraiment nulle comme technique !
			this.method = method;
			this.prefix = methodPrefix;
			this.prefixOffset = methodPrefixOffset;
			this.ns = ns;
			this.editor = editor;
		}
		
		public String getDisplayString() {
			if (displayString == null) {
				displayString = method.getElementName() + " ( package )";
			}
			return displayString;
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
	private static List<List> dynamicComplete(String namespace, String prefix, IClojureEditor editor, boolean findOnlyPublic) {
		if (namespace == null) {
			return Collections.emptyList();
		}
		if (prefix == null) {
			return Collections.emptyList();
		}
		
		REPLView repl = editor.getCorrespondingREPL();
		if (repl == null) return Collections.emptyList();
		Connection connection = repl.getToolingConnection();
		
		Response response = connection.send("op", "eval", "code", "(ccw.debug.serverrepl/code-complete \"" + namespace + "\" \"" + prefix + "\" " + (findOnlyPublic ? "true" : "false") + ")");
		return (List<List>) extractSingleValue(response, Collections.emptyList());
	}
	private List<List> dynamicNamespaceComplete(String prefix) {
		if (prefix == null) {
			return Collections.emptyList();
		}
        
        REPLView repl = editor.getCorrespondingREPL();
        if (repl == null) return Collections.emptyList();
		Connection connection = repl.getToolingConnection();
		
		Response response = connection.send("op", "eval", "code", "(ccw.debug.serverrepl/code-complete-ns \"" + prefix + "\")");
		return (List<List>) extractSingleValue(response, Collections.emptyList());
	}
	private static Object extractSingleValue(Response response, Object defaultValueIfNil) {
	    List vs = response.values();
		if (vs.isEmpty() || vs.get(0) == null) {
			return defaultValueIfNil;
		} else {
			return vs.get(0);
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
