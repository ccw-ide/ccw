/*******************************************************************************
 * Copyright (c) 2010 Tuomas KARKKAINEN.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Tuomas KARKKAINEN - initial API and implementation
 *******************************************************************************/
package ccw.editors.antlrbased;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.console.IOConsole;

import ccw.debug.ClojureClient;

public class RunTestsAction extends Action {
    public static final String RUN_TESTS_ID = "RunTestsAction";
	private static final String FAILED_TESTS_COLOR_KEY = "ccw.editors.RunTestsAction.COLOR_KEY";
	private static final String PASSED_TESTS_COLOR_KEY = "ccw.editors.RunTestsAction.COLOR_KEY";
    private final AntlrBasedClojureEditor editor;
    private final ColorRegistry colorRegistry;

    public RunTestsAction(AntlrBasedClojureEditor editor, ColorRegistry colorRegistry) {
        this.editor = editor;
        this.colorRegistry = colorRegistry;
        initColorRegistry();
    }
    private void initColorRegistry() {
    	if (!colorRegistry.hasValueFor(FAILED_TESTS_COLOR_KEY)) {
    		colorRegistry.put(FAILED_TESTS_COLOR_KEY, new RGB(0xff, 0xff, 0xcf));
    	}
    	if (!colorRegistry.hasValueFor(PASSED_TESTS_COLOR_KEY)) {
    		colorRegistry.put(PASSED_TESTS_COLOR_KEY, new RGB(0xcf, 0xff, 0xcf));
    	}
    }
    @Override
    public void run() {
        super.run();
        String lib = editor.getDeclaringNamespace();
        ClojureClient clojure = ClojureClient.newClientForActiveRepl();
        String compilationResult = clojure.remoteLoad(CompileLibAction.compileLibCommand(lib));
        refreshCompilationResults();
        if (compilationResult.contains("\"response-type\" 0,")) {
            runTests(lib, clojure);
        } else {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Compilation_failed);
            setReplBackgroundColor(colorRegistry.get(FAILED_TESTS_COLOR_KEY));
        }
    }

    private void runTests(String lib, ClojureClient clojure) {
        String results = clojure.remoteLoad(runTestsCommand(lib));
        if (results.contains(":fail 0, :error 0")) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Tests_passed);
            setReplBackgroundColor(colorRegistry.get(PASSED_TESTS_COLOR_KEY));
        } else {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Tests_failed);
            setReplBackgroundColor(colorRegistry.get(FAILED_TESTS_COLOR_KEY));
        }
    }

    private void setReplBackgroundColor(Color background) {
        IOConsole replConsole = ClojureClient.findActiveReplConsole(true, editor.getProject());
        replConsole.setBackground(background);
    }

    private void refreshCompilationResults() {
        try {
            IFile editorFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
            editorFile.getProject().getFolder("classes").refreshLocal(IFolder.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public static String runTestsCommand(String libName) {
        return "(clojure.test/run-tests'" + testLibName(libName) + ")";
    }

    public static String testLibName(String libName) {
        if (libName.endsWith("-test")) {
            return libName;
        }
        return libName + "-test";
    }
}
