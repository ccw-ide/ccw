package clojuredev.editors.rulesbased;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class ClojureDocumentProvider extends FileDocumentProvider {

    @Override
    protected IDocument createDocument(Object element) throws CoreException {
        IDocument document = super.createDocument(element);
        if (document != null) {
            IDocumentPartitioner partitioner = new ClojurePartitioner();
            document.setDocumentPartitioner(partitioner);
            partitioner.connect(document);
        }
        else {
            return null;
        }
        return document;
    }

}
