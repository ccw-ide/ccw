package clojuredev.editors.rulesbased;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class ClojureDocumentProvider extends FileDocumentProvider {

    @Override
    protected IDocument createDocument(Object element) throws CoreException {
        IDocument document = super.createDocument(element);
        if (document != null) {
        	IDocumentPartitioner partitioner = new ClojurePartitioner(new ClojurePartitionScanner(), 
        			ClojurePartitionScanner.CLOJURE_CONTENT_TYPES);

        	Map<String, IDocumentPartitioner> m = new HashMap<String, IDocumentPartitioner>();
        	m.put(ClojurePartitionScanner.CLOJURE_PARTITIONING, partitioner);
        	
        	TextUtilities.addDocumentPartitioners(document, m);
        }
        else {
            return null;
        }
        return document;
    }

}
