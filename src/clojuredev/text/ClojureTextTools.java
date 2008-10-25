/*
 * ScalaTextTools.java
 * 
 * Created on 03.11.2004
 *
 * Status: done
 */

package clojuredev.text;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.SWT;

import clojuredev.ClojuredevPlugin;
import clojuredev.editors.ClojureColorConstants;
import clojuredev.editors.ClojureColorProvider;

/**
 * @author Marc Moser
 * 
 * Some text tools
 */
public class ClojureTextTools {

	private ClojureColorProvider colorProvider;
//	private ClojurePartitionScanner partitionScanner;
//	private AbstractClojureScanner singlelineCommentScanner, multilineCommentScanner,
//		                         docCommentScanner, xmlScanner;

	public ClojureTextTools() {
		super();

	}

//	public IDocumentPartitioner createDocumentPartitioner() {
//		String[] types = new String[] {
//				ClojurePartitionScanner.MULTI_LINE_COMMENT,
//				ClojurePartitionScanner.DOC_COMMENT,
//				ClojurePartitionScanner.SINGLE_LINE_COMMENT,
//				ClojurePartitionScanner.XML };
//
//		return new DefaultPartitioner(getPartitionScanner(), types);
//	}

//	protected IPartitionTokenScanner getPartitionScanner() {
//		if (partitionScanner == null) {
//			partitionScanner = new ClojurePartitionScanner();
//		}
//		return partitionScanner;
//	}


//	public AbstractClojureScanner getXMLScanner() {
//		if (xmlScanner == null) {
//			xmlScanner = new SingleTokenScalaCodeScanner(ScalaColorConstants.SCALA_XML);
//		}
//		return xmlScanner;
//	}
//
//	protected ITokenScanner getMultilineCommentScanner() {
//		if (multilineCommentScanner == null) {
//			multilineCommentScanner = new SingleTokenScalaCodeScanner(ScalaColorConstants.SCALA_MULTI_LINE_COMMENT);
//		}
//		return multilineCommentScanner;
//	}

//	protected ITokenScanner getSinglelineCommentScanner() {
//		if (singlelineCommentScanner == null) {
//			singlelineCommentScanner = new SingleTokenScalaCodeScanner(ClojureColorConstants.SCALA_SINGLE_LINE_COMMENT);
//		}
//		return singlelineCommentScanner;
//	}

//	protected ITokenScanner getDocCommentScanner() {
//		if (docCommentScanner == null) {
//			docCommentScanner = new SingleTokenScalaCodeScanner(ScalaColorConstants.SCALA_DOC_COMMENT);
//		}
//		return docCommentScanner;
//	}

	public ClojureColorProvider getColorProvider() {
		if (colorProvider == null) {
			colorProvider = new ClojureColorProvider();
		}
		return colorProvider;
	}

	
	public IPreferenceStore getPreferenceStore() {
		return ClojuredevPlugin.getDefault().getPreferenceStore();
	}

	public TextAttribute createTextAttribute(String colorKey) {
		boolean bold = getPreferenceStore().getBoolean(
				colorKey + ClojureColorConstants.CLOJURE_ISBOLD_APPENDIX);
		boolean italics = getPreferenceStore().getBoolean(
				colorKey + ClojureColorConstants.CLOJURE_ISITALICS_APPENDIX);
		boolean underline = getPreferenceStore().getBoolean(
				colorKey + ClojureColorConstants.CLOJURE_ISUNDERLINE_APPENDIX);

		boolean strikethrough = getPreferenceStore().getBoolean(
				colorKey + ClojureColorConstants.CLOJURE_ISSTRIKETHROUGH_APPENDIX);
		
		int style = SWT.NORMAL;
		if (bold) style = style | SWT.BOLD;
		if (italics) style = style | SWT.ITALIC;
		if (underline) style = style | TextAttribute.UNDERLINE;
		if (strikethrough) style = style | TextAttribute.STRIKETHROUGH;
		
		return new TextAttribute(getColorProvider().getColor(colorKey), null, style);
	}

//	public String stripDoc(String comment) {
//		comment = comment.trim();
//		assert(comment.charAt(0) == '/');
//		assert(comment.charAt(1) == '*');
//		assert(comment.charAt(2) == '*');
//		comment = comment.substring(3);
//		assert(comment.charAt(comment.length() - 2) == '*');
//		assert(comment.charAt(comment.length() - 1) == '/');
//
//		comment = comment.substring(0, comment.length() - 2);
//		//StringTokenizer tok;
//		while (true) {
//			int idx = comment.indexOf('*');
//			if (idx == -1) break;
//			comment = comment.substring(0, idx) + comment.substring(idx + 1);
//		}
//		int idx = 0;
//		while (true) {
//			if (idx == comment.length()) break;
//			idx = comment.indexOf('@', idx);
//			if (idx == -1) break;
//			
//			// compute word
//			int jdx = idx + 1;
//			while (jdx < comment.length() && 
//					Character.isJavaIdentifierPart(comment.charAt(jdx))) jdx++;
//			
//			String tag = comment.substring(idx + 1, jdx);
//			comment = comment.substring(0, idx) + "<br><b>" +
//				"@" + tag + "</b>" + comment.substring(idx + 1 + tag.length());
//
//			idx = idx + ("<br><bf></bf>").length() + tag.length();
//		}
//		
//		
//		return comment;
//	}

	
}
