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

import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.utils.internal.Assert;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import ccw.editors.clojure.IClojureEditor;

/**
 * A condition that tests whether the ProjectionAnnotationModel 
 * on the ClojureEditor is null.
 */
public class ProjectionAnnotationModelNull extends DefaultCondition {

    private final IClojureEditor clojureEditor;

    public ProjectionAnnotationModelNull(IClojureEditor ce) {
        Assert.isNotNull(ce, "ClojureEditor was null"); //$NON-NLS-1$
        this.clojureEditor = ce;
    }

    public String getFailureMessage() {
        return "ClojureEditor's ProjectionAnnotationModel was always null."; //$NON-NLS-1$
    }

    public boolean test() throws Exception {
        return UIThreadRunnable.syncExec(new BoolResult() {
            public Boolean run() {
                return clojureEditor.getProjectionAnnotationModel() == null;
            }
        });
    }
}
