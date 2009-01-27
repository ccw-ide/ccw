/*******************************************************************************
 * Copyright (c) 2009 Casey Marshall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshall - initial API and implementation
 *******************************************************************************/
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
