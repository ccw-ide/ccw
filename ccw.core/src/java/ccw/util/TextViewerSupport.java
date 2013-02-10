package ccw.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Largely inspired by David Green:
 * http://greensopinion.blogspot.fr/2009/10/key-bindings-in-eclipse-editors.html
 */
public class TextViewerSupport implements FocusListener, DisposeListener {

	private final TextViewer textViewer;
	private final IHandlerService handlerService;
	private final List<IHandlerActivation> handlerActivations = 
			new ArrayList<IHandlerActivation>();

	public TextViewerSupport(TextViewer textViewer,
			IHandlerService handlerService) {
		this.textViewer = textViewer;
		this.handlerService = handlerService;
		StyledText textWidget = textViewer.getTextWidget();
		textWidget.addFocusListener(this);
		textWidget.addDisposeListener(this);

		if (textViewer.getTextWidget().isFocusControl()) {
			activateContext();
		}
	}

	public void focusLost(FocusEvent e) {
		deactivateContext();
	}

	public void focusGained(FocusEvent e) {
		activateContext();
	}

	public void widgetDisposed(DisposeEvent e) {
		deactivateContext();
	}

	protected void activateContext() {
		if (handlerActivations.isEmpty()) {
			activateHandler(ISourceViewer.QUICK_ASSIST,
					ITextEditorActionDefinitionIds.QUICK_ASSIST);
			activateHandler(ISourceViewer.CONTENTASSIST_PROPOSALS,
					ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
			activateHandler(ITextOperationTarget.CUT,
					IWorkbenchCommandConstants.EDIT_CUT);
			activateHandler(ITextOperationTarget.COPY,
					IWorkbenchCommandConstants.EDIT_COPY);
			activateHandler(ITextOperationTarget.PASTE,
					IWorkbenchCommandConstants.EDIT_PASTE);
			// We don't add DELETE, because if we do so, then the DELETE
			// is done twice ...
			//			activateHandler(ITextOperationTarget.DELETE,
			//					IWorkbenchCommandConstants.EDIT_DELETE);
			activateHandler(ITextOperationTarget.UNDO,
					IWorkbenchCommandConstants.EDIT_UNDO);
			activateHandler(ITextOperationTarget.REDO,
					IWorkbenchCommandConstants.EDIT_REDO);
			activateHandler(ITextOperationTarget.SELECT_ALL,
					IWorkbenchCommandConstants.EDIT_SELECT_ALL);
			activateHandler(ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION,
					IWorkbenchCommandConstants.EDIT_CONTEXT_INFORMATION);
			// activateHandler(ITextViewer,
			// IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
		}
	}

	protected void activateHandler(int operation, String actionDefinitionId) {
		StyledText textWidget = textViewer.getTextWidget();
		IHandler actionHandler = createActionHandler(operation,
				actionDefinitionId);
		IHandlerActivation handlerActivation = handlerService.activateHandler(
				actionDefinitionId, actionHandler,
				new ActiveFocusControlExpression(textWidget));

		handlerActivations.add(handlerActivation);
	}

	private IHandler createActionHandler(final int operation,
			String actionDefinitionId) {
		Action action = new Action() {
			@Override
			public void run() {
				if (textViewer.canDoOperation(operation)) {
					textViewer.doOperation(operation);
				}
			}
		};
		action.setActionDefinitionId(actionDefinitionId);
		return new ActionHandler(action);
	}

	protected void deactivateContext() {
		if (!handlerActivations.isEmpty()) {
			for (IHandlerActivation activation : handlerActivations) {
				handlerService.deactivateHandler(activation);
				activation.getHandler().dispose();
			}
			handlerActivations.clear();
		}
	}

	/**
	 * An expression that evaluates to true if and only if the current focus
	 * control is the one provided. Has a very high priority in order to ensure
	 * proper conflict resolution.
	 */
	public class ActiveFocusControlExpression extends Expression {

		private Control focusControl;

		public ActiveFocusControlExpression(Control control) {
			focusControl = control;
		}

		/*
		 * In collectExpressionInfo we ensure that the expression indicates that
		 * it uses the default variable. This gives the expression a very high
		 *  priority. Since the expression is only enabled when our control has 
		 *  focus, we've ensured that undo is directed to our text control at 
		 *  the right time.
		 */
		@Override
		public void collectExpressionInfo(ExpressionInfo info) {
			info.markDefaultVariableAccessed(); // give it a very high priority
			info.addVariableNameAccess(ISources.ACTIVE_SHELL_NAME);
			info.addVariableNameAccess(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
		}

		@Override
		public EvaluationResult evaluate(IEvaluationContext context)
				throws CoreException {
			if (Display.getCurrent() != null && focusControl.isFocusControl()) {
				return EvaluationResult.TRUE;
			}
			return EvaluationResult.FALSE;
		}
	}
}