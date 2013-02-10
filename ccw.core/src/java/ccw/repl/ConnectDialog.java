package ccw.repl;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ccw.util.StringUtils;

public class ConnectDialog extends Dialog {
	private static final String URL_SETTING = "url";
	private static final String URL_SETTING_DEFAULT = "nrepl://127.0.0.1";
	
	private Text rawUrl;
    private String url;
    
    private static final String CONNECT_DIALOG_SECTION = "ccw.repl.ConnectDialog";
    
    private IDialogSettings dialogSettings;
    
    public ConnectDialog(Shell parentShell, IDialogSettings dialogSettings) {
        super(parentShell);
        setBlockOnOpen(true);
        
        if (dialogSettings != null) {
        	this.dialogSettings = DialogSettings.getOrCreateSection(dialogSettings, CONNECT_DIALOG_SECTION);
        }
    }
    
    @Override
    protected boolean isResizable() {
    	return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Connect to a REPL");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        
        Composite composite = (Composite) super.createDialogArea(parent);
        
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.numColumns = 1;
        
        {
	        Label l = new Label(composite, 0);
	        GridData gd = new GridData();
	        l.setLayoutData(gd);
	        
	        l.setText("nRepl URL:");
        }
        {
	        rawUrl = new Text(composite, SWT.BORDER);
	        GridData gd = new GridData();
	        gd.grabExcessHorizontalSpace = true;
	        gd.horizontalAlignment = SWT.FILL;
	        gd.widthHint = 300;
	        rawUrl.setLayoutData(gd);
        }
        
        {
        	Label l;
        	
        	l = new Label(composite, SWT.NONE);
        	l.setText("Example: nrepl://127.0.0.1:5678");
        	l.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
        	
        	l = new Label(composite, SWT.NONE);
        	l.setText("Example: http://yourapp.herokuapp.com/repl");
        	l.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
        }
        
        initValues();
        
        composite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				saveValues();
			}
		});
        
        applyDialogFont(composite);
        
        return composite;
    }
    
    private void initValues() {
    	setText(rawUrl, dialogSettings.get(URL_SETTING), URL_SETTING_DEFAULT);
    }
    
    
    private void setText(Text w, String value, String defaultValue) {
    	if (dialogSettings == null) return;
    	w.setText((value!=null) ? value : defaultValue);
    }

    private void saveValues() {
    	saveValue(rawUrl, URL_SETTING, URL_SETTING_DEFAULT);
    }
    
    private void saveValue(Text w, String key, String defaultValue) {
    	if (dialogSettings == null) return;
    	String value = w.getText();
		dialogSettings.put(
				key, 
				StringUtils.isEmpty(value) ? defaultValue : value);
    }

    protected void okPressed () {
        url = rawUrl.getText();
        super.okPressed();
    }
    
    public String getURL () {
        return url;
    }
}
