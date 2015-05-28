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
package ccw.repl;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Repl provider contract.
 */
public interface IReplProvider {

    /**
     * Gets the corresponding Repl of this object.
     * @return The Repl(View), can be null...
     */
    @Nullable REPLView getCorrespondingREPL();
    
    /**
     * Gets the connection.
     * @return The connection, or null if none
     */
    @Nullable SafeConnection getSafeToolingConnection();
}
