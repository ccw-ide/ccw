/*
 * Adapted from the...
**     ________ ___   / /  ___     Scala Plugin for Eclipse             **
**    / __/ __// _ | / /  / _ |    (c) 2004-2005, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
*
* by Casey Marshall for the Clojure plugin, clojuredev
\*                                                                      */

// Created on 2004-11-08 by Marc Moser
// $Id: ScalaSourceViewerConfiguration.java,v 1.3 2006/02/03 12:42:08 mcdirmid Exp $

package clojuredev.editors.rulesbased;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.text.java.JavaDoubleClickSelector;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import clojuredev.ClojureCore;
import clojuredev.ClojuredevPlugin;

/**
 * @author Marc Moser
 *
 * Scala's SourceViewerConfiguration.
 */
public class ClojureSourceViewerConfiguration extends SourceViewerConfiguration {

//	public static final String CLOJURE_INDENT_SPACES = "clojure_indent_spaces";
//	public static final String CLOJURE_TAB_DISPLAY = "clojure_tab_display";

	@Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[]{
                IDocument.DEFAULT_CONTENT_TYPE,
                ClojurePartitionScanner.SEXP,
                ClojurePartitionScanner.FUNARGS,
                ClojurePartitionScanner.SINGLE_LINE_COMMENT,
        };
    }

    protected ClojureEditor editor;
//    protected JavaColorManager colorManager;

	public ClojureSourceViewerConfiguration(ClojureEditor theTextEditor) {
		super();
		editor = theTextEditor;
//		colorManager = new JavaColorManager();
	}
	
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));


		assistant.setEmptyMessage("No completions available.");
		assistant.enableAutoActivation(false);
		assistant.enableAutoInsert(true);
		assistant.setContextInformationPopupBackground(sourceViewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		assistant.setContextInformationPopupForeground(sourceViewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		assistant.setContextSelectorBackground(sourceViewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		assistant.setContextSelectorForeground(sourceViewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		
//		assistant.setContentAssistProcessor(new ScalaContentAssistant(editor), IDocument.DEFAULT_CONTENT_TYPE);
		return assistant;
	}
	
	
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				DefaultInformationControl ret = new DefaultInformationControl(parent, new HTMLTextPresenter());
				ret.setBackgroundColor(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				ret.setForegroundColor(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				return ret;
			}
		};
	}

	
	
	/**
	 * Returns the visual width of the tab character.
	 * 
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the tab width
	 */
	public int getTabWidth(ISourceViewer sourceViewer) { return getTabWidth(); }
	
	private static String indent = null;
	public static String indent() {
		if (indent == null) {
			indent = "";
			for (int i = 0; i < getTabWidth(); i++) indent += " ";
		}
		return indent;
	}

	public static int getTabWidth() {
	    return 2;
//		return ClojuredevPlugin.getDefault().getPreferenceStore().getInt(CLOJURE_TAB_DISPLAY);
	}
	

	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { indent() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
	    PresentationReconciler reconciler= new PresentationReconciler();
	    
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new ClojureCodeScanner());
        reconciler.setDamager(dr, ClojurePartitionScanner.SEXP);
        reconciler.setRepairer(dr, ClojurePartitionScanner.SEXP);
        
        dr = new DefaultDamagerRepairer(new RuleBasedScanner(){
            {
                Color commentColor = new Color(Display.getCurrent(), 0, 127, 0);
                TextAttribute commentAttribute = new TextAttribute(commentColor, null, SWT.ITALIC);
                IToken commentToken = new Token(commentAttribute);
                
                List<IRule> rules = new ArrayList<IRule>();
                rules.add(new EndOfLineRule(";", commentToken));
                setRules(rules.toArray(new IRule[]{}));
            }
        });
        reconciler.setDamager(dr, ClojurePartitionScanner.SINGLE_LINE_COMMENT);
        reconciler.setRepairer(dr, ClojurePartitionScanner.SINGLE_LINE_COMMENT);
        
	    return reconciler;
	}
	
//		ScalaCodeScanner scanner = new ScalaCodeScanner(editor);
//		PresentationReconciler reconciler = new PresentationReconciler();
//		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
//		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
//		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
//
//		dr = new DefaultDamagerRepairer(getMultilineCommentScanner());
//		reconciler.setDamager(dr, ClojurePartitionScanner.MULTI_LINE_COMMENT);
//		reconciler.setRepairer(dr, ClojurePartitionScanner.MULTI_LINE_COMMENT);
//
//		dr = new DefaultDamagerRepairer(getSinglelineCommentScanner());
//		reconciler.setDamager(dr, ClojurePartitionScanner.SINGLE_LINE_COMMENT);
//		reconciler.setRepairer(dr, ClojurePartitionScanner.SINGLE_LINE_COMMENT);
//		if (false) {
//			dr = new DefaultDamagerRepairer(getXMLScanner());
//			reconciler.setDamager(dr, ClojurePartitionScanner.XML);
//			reconciler.setRepairer(dr, ClojurePartitionScanner.XML);
//		}
//		dr = new DefaultDamagerRepairer(getDocCommentScanner());
//		reconciler.setDamager(dr, ClojurePartitionScanner.DOC_COMMENT);
//		reconciler.setRepairer(dr, ClojurePartitionScanner.DOC_COMMENT);
//
//		return reconciler;
//	}

//	public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, String contentType) {
//		// we don't do anything special for contentTypes different
//		// from scala source code
//		String partitioning= getConfiguredDocumentPartitioning(sourceViewer);
//		if (ClojurePartitionScanner.XML.equals(contentType)                 ||
//			ClojurePartitionScanner.MULTI_LINE_COMMENT.equals(contentType)  ||
//			ClojurePartitionScanner.SINGLE_LINE_COMMENT.equals(contentType) ||
//			ClojurePartitionScanner.DOC_COMMENT.equals(contentType))
//			return new IAutoEditStrategy[] { new DefaultIndentLineAutoEditStrategy() };
//		return new IAutoEditStrategy[] { 
//				// new SmartSemicolonAutoEditStrategy(partitioning),
////				new ScalaAutoIndentStrategy(partitioning, getProject()), 
//				new IAutoEditStrategy() {
//					public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
//						if (command.text == null) return;
//						if (command.text.equals("\t")) {
//							int idx = command.offset - 1;
//							while (idx > 0) {
//								char c = document.get().charAt(idx);
//								if (c == '\n' || c == '\r') {
//									int jdx = command.offset;
//									while (true) {
//										if (jdx >= document.getLength()) return;
//										c = document.get().charAt(jdx);
//									  if (!Character.isWhitespace(c)) break;
//									  if (c == '\n' || c == '\r') return;
//										jdx++;
//									}
////									ClojureProject sproject = ((ClojureEditor.SourceViewer0) sourceViewer).editor().project();
////									if (sproject == null) return;
////									ScalaIndenter indenter = ScalaAutoIndentStrategy.indenter(document, sproject.getJavaProject());
////									String indent = indenter.computeIndentation(idx + 1).toString();
//									command.offset = idx + 1;
//									command.length = jdx - command.offset;
//									command.text = indent;
//									// TODO: retabulate line.
//									//command.caretOffset = jdx;
//									command.shiftsCaret = true;
//									// command.text = "";
//									return;
//									
//								} else if (!Character.isWhitespace(c)) break;
//								idx--;
//							}
//						}
//						command.text = command.text.replace("\t", indent());
//						
//					}
//				}};
//	}

	private IJavaProject getProject() {
		if (editor == null) return null;
		IProject project = editor.iproject();
		try {
			return ClojureCore.getJavaProject(project);
		} catch (Throwable t) {
			ClojuredevPlugin.logError(t);
			return null;
		}
	}


//	protected ITokenScanner getXMLScanner() {
//		return ClojuredevPlugin.textTools().getXMLScanner();
//	}
//	protected ITokenScanner getMultilineCommentScanner() {
//		return ClojuredevPlugin.textTools().getMultilineCommentScanner();
//	}
//
//	protected ITokenScanner getSinglelineCommentScanner() {
//		return ClojuredevPlugin.textTools().getSinglelineCommentScanner();
//	}
//
//	protected ITokenScanner getDocCommentScanner() {
//		return ClojuredevPlugin.textTools().getDocCommentScanner();
//	}

//	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
//		return new String[] {	IDocument.DEFAULT_CONTENT_TYPE,
//							ClojurePartitionScanner.MULTI_LINE_COMMENT,
//							ClojurePartitionScanner.SINGLE_LINE_COMMENT,
//							ClojurePartitionScanner.DOC_COMMENT,
//							ClojurePartitionScanner.XML};
//	}

	protected IPreferenceStore getPreferenceStore() {
		return ClojuredevPlugin.getDefault().getPreferenceStore();
	}

	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] {""};
	}

//	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
//		return new ScalaTextHover();
//	}

	
	private JavaDoubleClickSelector fJavaDoubleClickSelector;
	/*
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		// XXX: do it here
		if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
			if (fJavaDoubleClickSelector == null) {
				fJavaDoubleClickSelector= new JavaDoubleClickSelector();
				fJavaDoubleClickSelector.setSourceVersion(JavaCore.VERSION_1_4);
			}
			return fJavaDoubleClickSelector;
		}
		return super.getDoubleClickStrategy(sourceViewer, contentType);
	}

//	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
//		return new IHyperlinkDetector[] { new ScalaHyperlinkDetector() };
//	}

}
