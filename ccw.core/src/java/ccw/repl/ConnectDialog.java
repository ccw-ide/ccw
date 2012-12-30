package ccw.repl;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ccw.util.StringUtils;

public class ConnectDialog extends Dialog {
    private static final String PORT_SETTING_DEFAULT = "";
	private static final String HOST_SETTING_DEFAULT = "127.0.0.1";
	private Text host;
    private Text port;
    private String url;
    
    private static final String CONNECT_DIALOG_SECTION = "ccw.repl.ConnectDialog";
    private static final String PORT_SETTING = "port";
    private static final String HOST_SETTING = "host";
    private IDialogSettings dialogSettings;
    
    public ConnectDialog(Shell parentShell, IDialogSettings dialogSettings) {
        super(parentShell);
        setBlockOnOpen(true);
        
        if (dialogSettings != null) {
        	this.dialogSettings = DialogSettings.getOrCreateSection(dialogSettings, CONNECT_DIALOG_SECTION);
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Connect to REPL");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        
        Composite composite = (Composite) super.createDialogArea(parent);
        
        parent = new Composite(composite, 0);
        parent.setLayout(new GridLayout(2, false));
        new Label(parent, 0).setText("Hostname");
        
        host = new Text(parent, SWT.BORDER);
        
        new Label(parent, 0).setText("Port");
        
        port = new Text(parent, SWT.BORDER);
        port.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				String newText = port.getText().substring(0, e.start) + e.text + port.getText().substring(e.end);
				e.doit = newText.matches("\\d*");
			}
		});
        
        initValues();
        
        composite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				saveValues();
			}
		});
        
        port.setFocus();
        port.setSelection(0, port.getText().length());
        return composite;
    }
    
    private void initValues() {
    	if (dialogSettings == null) return;
    	setText(host, dialogSettings.get(HOST_SETTING), HOST_SETTING_DEFAULT);
    	setText(port, dialogSettings.get(PORT_SETTING), PORT_SETTING_DEFAULT);
    }
    
    
    private void setText(Text w, String value, String defaultValue) {
    	w.setText((value!=null) ? value : defaultValue);
    }

    private void saveValues() {
    	if (dialogSettings == null) return;
    	saveValue(host, HOST_SETTING, HOST_SETTING_DEFAULT);
    	saveValue(port, PORT_SETTING, PORT_SETTING_DEFAULT);
    }
    
    private void saveValue(Text w, String key, String defaultValue) {
    	String value = w.getText();
		dialogSettings.put(
				key, 
				StringUtils.isEmpty(value) ? defaultValue : value);
    }

    protected void okPressed () {
        url = String.format("nrepl://%s:%s", host.getText(), port.getText());
        super.okPressed();
    }
    
    public String getURL () {
        return url;
    }
}
