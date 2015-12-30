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
package ccw.editors.clojure.scanners;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import ccw.CCWPlugin;
import ccw.TraceOptions;
import ccw.util.ITracer;

public class TracingPartitioner extends FastPartitioner {

    public TracingPartitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
    	super(scanner, legalContentTypes);
    }
    
    @Override
    public void connect(IDocument document, boolean delayInitialization) {
        super.connect(document, delayInitialization);

        printPartitions(document);
    }

    private void printPartitions(IDocument document) {

        ITracer tracer = CCWPlugin.getTracer();
        if (tracer.isEnabled(TraceOptions.EDITOR_SCANNERS)) {

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
            tracer.trace(TraceOptions.EDITOR_SCANNERS, buffer);
        }
    }
}
