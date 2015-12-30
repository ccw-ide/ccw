/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrea RICHIARDI - initial implementation
 *******************************************************************************/
package waits;

import org.eclipse.core.resources.IResource;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.utils.internal.Assert;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

/**
 * A condition that waits for the ProjectionAnnotationModel 
 * on the ClojureEditor has one element.
 */
public class ResourceInSync extends DefaultCondition {

    private final IResource resource;

    public ResourceInSync(IResource res) {
        Assert.isNotNull(res, "IResource was null"); //$NON-NLS-1$
        this.resource = res;
    }

    public String getFailureMessage() {
        return "IResource never got in sync."; //$NON-NLS-1$
    }

    public boolean test() throws Exception {
        return UIThreadRunnable.syncExec(new BoolResult() {
            public Boolean run() {
                return resource.isSynchronized(IResource.DEPTH_INFINITE);
            }
        });
    }
}
