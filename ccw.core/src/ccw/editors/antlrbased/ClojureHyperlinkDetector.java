package ccw.editors.antlrbased;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

public class ClojureHyperlinkDetector extends AbstractHyperlinkDetector
		implements IHyperlinkDetector {
	public static final String ID = "ccw.texteditor.hyperlinkDetector";
	public static final String TARGET_ID = "ccw.ui.clojureCode";
	
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		System.out.println("asked for hyperlink detection in clojure code !");
		System.out.println("region: " + region);
		try {
			System.out.println("text: " + textViewer.getDocument()
					.get(region.getOffset(), region.getLength()));
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("");
		// TODO Auto-generated method stub
		return null;
	}

}
