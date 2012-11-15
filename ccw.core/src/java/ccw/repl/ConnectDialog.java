package ccw.repl;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConnectDialog extends Dialog {
    private Text hosts;
    private Text port;
    private int portNumber;
    private String url;
    
    public ConnectDialog(Shell parentShell) {
        super(parentShell);
        setBlockOnOpen(true);
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
        
        hosts = new Text(parent, SWT.BORDER);
        hosts.setText("127.0.0.1"); // don't know much about swt layouts yet :-(
        hosts.setSelection(new Point(0, hosts.getText().length()));
        
        new Label(parent, 0).setText("Port");
        
        port = new Text(parent, SWT.BORDER);
        port.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				String newText = port.getText().substring(0, e.start) + e.text + port.getText().substring(e.end);
				e.doit = newText.matches("\\d*");
			}
		});
        
        port.setFocus();
        return composite;
    }

    protected void okPressed () {
        url = String.format("nrepl://%s:%s", hosts.getText(), port.getText());
        super.okPressed();
    }
    
    public String getURL () {
        return url;
    }
}
