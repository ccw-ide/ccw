/**
 * 
 */
package clojuredev.editors.antlrbased;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

/**
 * @author ogenvo
 *
 */
public class YmlTextAutoEditStrategy implements IAutoEditStrategy {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
	 */
	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		System.out.println("customizeDocumentCommand " + command.offset + " " + command.length + " [" + command.text + "]");
		if (command.length == 0 && command.text != null && endsWithDelimiter(document, command.text)){
			// End of line
			
		}
	//		smartIndentAfterNewLine(d, c);
		else if ("}".equals(command.text)) { 
//			smartInsertAfterBracket(d, c);
		}
	}
	private boolean endsWithDelimiter(IDocument document, String txt) {
		String[] delimiters= document.getLegalLineDelimiters();
		if (delimiters != null){
			return TextUtilities.endsWith(delimiters, txt) > -1;
		}
		return false;
	}

}
