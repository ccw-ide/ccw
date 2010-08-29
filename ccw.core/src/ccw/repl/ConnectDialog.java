package ccw.repl;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConnectDialog extends Dialog {
    private Combo hosts;
    private Text port;
    private String host;
    private int portNumber;
    
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
        
        hosts = new Combo(parent, SWT.BORDER);
        hosts.setText("                       "); // don't know much about swt layouts yet :-(
        hosts.setFocus();
        hosts.setSelection(new Point(0, hosts.getText().length()));
        
        new Label(parent, 0).setText("Port");
        
        port = new Text(parent, SWT.BORDER);
        port.addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
            }
            
            public void keyPressed(KeyEvent e) {
                e.doit = Character.isDigit(e.character);
            }
        });
        
        return composite;
    }

    protected void okPressed () {
        host = hosts.getText();
        
        try {
            portNumber = Integer.parseInt(port.getText());
        } catch (NumberFormatException e) {
            // shouldn't happen given the keylistener above
        }

        super.okPressed();
    }

    public String getHost () {
        return host;
    }
    
    public int getPort () {
        return portNumber;
    }
}
