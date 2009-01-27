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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class ClojurePartitioner extends FastPartitioner {

    public ClojurePartitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
    	super(scanner, legalContentTypes);
    }
    
    @Override
    public void connect(IDocument document, boolean delayInitialization) {
        super.connect(document, delayInitialization);
        printPartitions(document);
    }

    public void printPartitions(IDocument document) {
        StringBuffer buffer = new StringBuffer();

        ITypedRegion[] partitions = computePartitioning(0, document.getLength());
        for (int i = 0; i < partitions.length; i++)
        {
            try
            {
                buffer.append("Partition type: " 
                  + partitions[i].getType() 
                  + ", offset: " + partitions[i].getOffset()
                  + ", length: " + partitions[i].getLength());
                buffer.append("\n");
                buffer.append("Text:\n");
                buffer.append(document.get(partitions[i].getOffset(), 
                 partitions[i].getLength()));
                buffer.append("\n---------------------------\n\n\n");
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
        }
        System.out.print(buffer);
    }
}
