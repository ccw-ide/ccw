package ccw.repl;

import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.ViewPart;

import ccw.CCWPlugin;
import ccw.editors.antlrbased.ClojureSourceViewer;
import ccw.editors.antlrbased.ClojureSourceViewerConfiguration;
import ccw.editors.rulesbased.ClojureDocumentProvider;
import ccw.outline.NamespaceBrowser;
import cemerick.nrepl.Connection;
import clojure.lang.Atom;
import clojure.lang.PersistentTreeMap;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class REPLView extends ViewPart {
    public static final String VIEW_ID = "ccw.view.repl";
    public static final AtomicReference<REPLView> activeREPL = new AtomicReference();

    private static Var log;
    private static Var evalExpression;
    static {
        try {
            Var.find(Symbol.intern("clojure.core/require")).invoke(Symbol.intern("ccw.repl.view-helpers"));
            log = Var.find(Symbol.intern("ccw.repl.view-helpers/log"));
            evalExpression = Var.find(Symbol.intern("ccw.repl.view-helpers/eval-expression"));
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize view helpers.", e);
        }
    }
    
    // TODO would like to eliminate separate log view, but:
    // 1. PareditAutoEditStrategy gets all text from a source document, and bails badly if its not well-formed
    //      (and REPL output is guaranteed to not be well-formed)
    // 2. Even if (1) were fixed/changed, it's not clear to me how to "partition" an IDocument, or compose IDocuments
    //     so that we can have one range that is still *highlighted* for clojure content (and not editable),
    //     and another range that is editable and has full paredit, code completion, etc.
    private StyledText logPanel;
    private ClojureSourceViewer viewer;
    private StyledText viewerWidget;
    private ClojureSourceViewerConfiguration viewerConfig;
    
    private Connection interactive;
    private Connection toolConnection;
    
    private IConsole console;
    private ILaunch launch;
    
    private final Atom requests = new Atom(PersistentTreeMap.EMPTY);
    
    public REPLView () {}
    
    private void copyToLog (StyledText s) {
        int start = logPanel.getCharCount();
        try {
            log.invoke(logPanel, s.getText(), null);
            for (StyleRange sr : s.getStyleRanges()) {
                sr.start += start;
                logPanel.setStyleRange(sr);
            }
        } catch (Exception e) {
            // should never happen
            CCWPlugin.logError("Could not copy expression to log", e);
        }
    }
    
    private void evalExpression () {
        evalExpression(viewerWidget.getText(), false);
        copyToLog(viewerWidget);
        viewerWidget.setText("");
    }
    
    public void evalExpression (String s) {
        // TODO add highlighting of evaluated code pushed from editors
        evalExpression(s, true);
    }
    
    public void evalExpression (String s, boolean copyToLog) {
        try {
            if (s.trim().length() > 0) {
                if (copyToLog) log.invoke(logPanel, s, null);
                evalExpression.invoke(this, logPanel, interactive.conn, requests, s);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void closeView () throws Exception {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        page.hideView(this);
        closeConnections();
    }
    
    public void closeConnections () throws Exception {
        if (interactive != null) interactive.close();
        if (toolConnection != null) toolConnection.close();
    }
    
    public void reconnect () throws Exception {
        closeConnections();
        configure(interactive.host, interactive.port);
    }
    
    public void setCurrentNamespace (String ns) {
        // TODO waaaay better to put a dropdown namespace chooser in the view's toolbar,
        // and this would just change its selection
        setPartName(String.format("REPL @ %s:%s (%s)", interactive.host, interactive.port, ns));
    }
    
    @SuppressWarnings("unchecked")
    public boolean configure (String host, int port) throws Exception {
        try {
            interactive = new Connection(host, port);
            toolConnection = new Connection(host, port);
            setCurrentNamespace("user");
            evalExpression("(println \"Clojure\" (clojure-version))", false);
            return true;
        } catch (ConnectException e) {
            closeView();
            MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    "Could not connect", String.format("Could not connect to REPL @ %s:%s", host, port));
            return false;
        }
    }
    
    /*
     if (CCWPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SWITCH_TO_NS_ON_REPL_STARTUP)) {
            try {
                List<IFile> files = LaunchUtils.getFilesToLaunchList(processConsole.getProcess().getLaunch().getLaunchConfiguration());
                if (files.size() > 0) {
                    String namespace = ClojureCore.getDeclaredNamespace(files.get(0));
                    if (namespace != null) {
                        EvaluateTextAction.evaluateText(this.console, "(in-ns '" + namespace + ")", false);
                    }
                }
            } catch (CoreException e) {
                CCWPlugin.logError("error while trying to guess the ns to which make the REPL console switch", e);
            }
        }
     */
    
    public static REPLView connect () throws Exception {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ConnectDialog dlg = new ConnectDialog(window.getShell());
        
        REPLView repl = null;
        if (dlg.open() == ConnectDialog.OK) {
            // cannot find any way to create a configured/connected REPLView, and install it programmatically
            String host = dlg.getHost();
            int port = dlg.getPort();
            if (host == null || host.length() == 0 || port < 0 || port > 65535) {
                MessageDialog.openInformation(window.getShell(),
                        "Invalid connection info",
                        "You must provide a useful hostname and port number to connect to a REPL.");
            } else {
                repl = connect(host, port);
            }
        }
        
        return repl;
    }
    
    public static REPLView connect (String host, int port) throws Exception {
        REPLView repl = (REPLView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(VIEW_ID, host + "@" + port, IWorkbenchPage.VIEW_ACTIVATE);
        return repl.configure(host, port) ? repl : null;
    }
    
    public Connection getConnection () {
        return interactive;
    }
    
    public Connection getToolingConnection () {
        return toolConnection;
    }
    
    /**
     * Returns the console for the launch that this REPL is associated with, or
     * null if this REPL is using a remote connection.
     */
    public IConsole getConsole () {
        return console;
    }
    
    public void setConsole (IConsole console) {
        this.console = console;
    }
    
    public void showConsole () {
        if (console != null) ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
    }
    
    public ILaunch getLaunch() {
        return launch;
    }

    public void setLaunch(ILaunch launch) {
        this.launch = launch;
    }

    @Override
    public void createPartControl(Composite parent) {
        IPreferenceStore prefs = CCWPlugin.getDefault().getCombinedPreferenceStore();
        
        SashForm split = new SashForm(parent, SWT.VERTICAL);
        
        logPanel = new StyledText(split, SWT.V_SCROLL | SWT.WRAP);
        logPanel.setWrapIndent(4);
        logPanel.setEditable(false);
        logPanel.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        
        viewer = new ClojureSourceViewer(split, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL, prefs) {
            public Connection getCorrespondingREPLConnection () {
                // we'll be connected by the time this is called
                return toolConnection;
            }
        };
        viewerConfig = new ClojureSourceViewerConfiguration(prefs, viewer);
        viewer.configure(viewerConfig);
        getViewSite().setSelectionProvider(viewer);
        viewer.setDocument(ClojureDocumentProvider.configure(new Document()));
        viewerWidget = viewer.getTextWidget();
        
        viewerWidget.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        viewerWidget.addVerifyKeyListener(new REPLInputVerifier());

        split.setWeights(new int[] {80, 20});
        
        /*
         * Need to hook up here to force a re-evaluation of the preferences
         * for the syntax coloring, after the token scanner has been
         * initialized. Otherwise the very first Clojure editor will not
         * have any tokens colored.
         * TODO this is repeated in AntlrBasedClojureEditor...surely we can make the source viewer self-sufficient here 
         */
        viewer.propertyChange(null);
        
        viewerWidget.addFocusListener(new NamespaceRefreshFocusListener());
        logPanel.addFocusListener(new NamespaceRefreshFocusListener());
        
        parent.addDisposeListener(new DisposeListener () {
            public void widgetDisposed(DisposeEvent e) {
                activeREPL.compareAndSet(REPLView.this, null);
            }
        });
    }
    
    @Override
    public void dispose() {
        super.dispose();
        interactive.close();
        toolConnection.close();
    }

    public boolean isDisposed () {
        // TODO we actually want to report whether the viewpart has been closed, not whether or not
        // the platform has disposed the widget peer
        return viewerWidget.isDisposed();
    }

    @Override
    public void setFocus() {
        viewerWidget.setFocus();
    }

    private final class NamespaceRefreshFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            activeREPL.set(REPLView.this);
            NamespaceBrowser.setREPLConnection(toolConnection);
        }

        public void focusLost(FocusEvent e) {}
    }

    private class REPLInputVerifier implements VerifyKeyListener {
        private boolean isEvalEvent (KeyEvent e) {
            if (e.stateMask == SWT.SHIFT) return false;
            
            if (e.keyCode == '\n' || e.keyCode == '\r') {
                return e.stateMask == SWT.CONTROL ||
                    viewerWidget.getSelection().x == viewerWidget.getCharCount();
            }
            
            return false;
        }

        public void verifyKey(VerifyEvent e) {
            if (isEvalEvent(e)) {
                evalExpression();
                e.doit = false;
            }
        }
    }
}
