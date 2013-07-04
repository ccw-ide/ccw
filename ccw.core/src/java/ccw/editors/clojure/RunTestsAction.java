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
package ccw.editors.clojure;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import ccw.CCWPlugin;
import ccw.repl.REPLView;
import clojure.lang.Keyword;
import clojure.tools.nrepl.Connection;
import clojure.tools.nrepl.Connection.Response;

public class RunTestsAction extends Action {
    public static final String RUN_TESTS_ID = "RunTestsAction";
	private static final String FAILED_TESTS_COLOR_KEY = "ccw.editors.RunTestsAction.COLOR_KEY";
	private static final String PASSED_TESTS_COLOR_KEY = "ccw.editors.RunTestsAction.COLOR_KEY";
    private final ClojureEditor editor;
    private final ColorRegistry colorRegistry;

    public RunTestsAction(ClojureEditor editor, ColorRegistry colorRegistry) {
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
        try {
            String lib = editor.findDeclaringNamespace();
            REPLView replView = editor.getCorrespondingREPL();
            Connection repl = replView.getToolingConnection();
            Response compilationResult = repl.send("op", "eval", "code", CompileLibAction.compileLibCommand(lib));
            refreshCompilationResults();
            if (new Long(0).equals(((Map)compilationResult.values().get(0)).get("response-type"))) {
                runTests(lib, repl);
            } else {
                editor.setStatusLineErrorMessage(ClojureEditorMessages.Compilation_failed);
                setReplBackgroundColor(colorRegistry.get(FAILED_TESTS_COLOR_KEY));
            }
        } catch (Exception e) {
            CCWPlugin.logError("Failed running tests against editor " + editor.getPartName(), e);
        }
    }

    private void runTests(String lib, Connection repl) throws Exception {
        Response results = repl.send("op", "eval", "code", runTestsCommand(lib));
        if (((String)results.combinedResponse().get(Keyword.intern("out"))).contains(":fail 0, :error 0")) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Tests_passed);
            setReplBackgroundColor(colorRegistry.get(PASSED_TESTS_COLOR_KEY));
        } else {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Tests_failed);
            setReplBackgroundColor(colorRegistry.get(FAILED_TESTS_COLOR_KEY));
        }
    }

    private void setReplBackgroundColor(Color background) {
        // TODO push this over to REPLView? Changing background colors seems harsh -- how does the bgcolor get reverted?  
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
