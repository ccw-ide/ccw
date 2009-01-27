/*******************************************************************************
 * Copyright (c) 2009 Casey Marshal and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshal - initial API and implementation
 *******************************************************************************/
package clojuredev.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;

public class ClojureConsoleFactory implements IConsoleFactory {

    private IConsoleManager fConsoleManager;
    private ClojureConsole fConsole;

    public ClojureConsoleFactory() {
        fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
        fConsoleManager.addConsoleListener(new IConsoleListener() {

            public void consolesAdded(IConsole[] consoles) {
            }

            public void consolesRemoved(IConsole[] consoles) {
                for (int i = 0; i < consoles.length; i++) {
                    if (consoles[i] == fConsole) {
                        // fConsole.saveDocument();
                        fConsole = null;
                    }
                }
            }

        });
    }

    public void openConsole() {
        if (fConsole == null) {
            fConsole = new ClojureConsole();
            // fConsole.initializeDocument();
            fConsoleManager.addConsoles(new IConsole[] { fConsole });
        }
        fConsoleManager.showConsoleView(fConsole);
    }

}
