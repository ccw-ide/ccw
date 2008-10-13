package clojuredev.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class ClojureLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		System.out.println("launch:" + selection.toString());
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		if (input instanceof FileEditorInput) {
			IFile file = ((FileEditorInput)input).getFile();
			
			try {
				Object result = clojure.lang.Compiler.loadFile(file.getLocation().toFile().toString());
				if (result != null) {
					System.out.println(result.toString());
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
