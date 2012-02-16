/*
 * Original java code: https://www.ibm.com/developerworks/opensource/tutorials/os-eclipse-classpath/ 
 */
package ccw.leiningen;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class LeiningenClasspathContainerPage extends WizardPage implements
		IClasspathContainerPage, IClasspathContainerPageExtension {


    private final static String DEFAULT_EXTS = "jar,zip";
    
    private IJavaProject _proj;
    private Combo _dirCombo;
    private Button _dirBrowseButton;
    private Text _extText;
    private IPath _initPath = null;

    /**
     * Default Constructor - sets title, page name, description
     */
    public LeiningenClasspathContainerPage() {
        super(Messages.PageName, Messages.PageTitle, null);
        setDescription(Messages.PageDesc);
        setPageComplete(true);
    }
    
    public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
        _proj = project;
    }
    
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());
        
        createDirGroup(composite);
        
        createExtGroup(composite);
        
        setControl(composite);    
    }
    
    /**
     * Creates the directory label, combo, and browse button
     * 
     * @param parent the parent widget
     */
    private void createDirGroup(Composite parent) {
        Composite dirSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns = 3;
        dirSelectionGroup.setLayout(layout);
        dirSelectionGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL| GridData.VERTICAL_ALIGN_FILL));

        new Label(dirSelectionGroup, SWT.NONE).setText(Messages.DirLabel);

        _dirCombo = new Combo(dirSelectionGroup, SWT.SINGLE | SWT.BORDER);
        _dirCombo.setText( getInitDir() );                

        _dirBrowseButton= new Button(dirSelectionGroup, SWT.PUSH);
        _dirBrowseButton.setText( Messages.Browse ); 
        _dirBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        _dirBrowseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleDirBrowseButtonPressed();
           }
        });    
        setControl(dirSelectionGroup);
    }
    
    /**
     * Creates the extensions label and text box
     * 
     * @param parent parent widget
     */
    private void createExtGroup(Composite parent) {
        Composite extSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns = 2;
        extSelectionGroup.setLayout(layout);
        extSelectionGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL| GridData.VERTICAL_ALIGN_FILL));

        new Label(extSelectionGroup, SWT.NONE).setText(Messages.ExtLabel);
        
        _extText = new Text(extSelectionGroup,SWT.BORDER);
        _extText.setText(getInitExts()+"                  ");

        setControl(extSelectionGroup);
    }
    
    /**
     * Creates a directory dialog 
     */
    protected void handleDirBrowseButtonPressed() {
        DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setMessage(Messages.DirSelect);
        dialog.setFilterPath(getDirValue());
        String dir = dialog.open();
        if (dir != null) {
            _dirCombo.setText(dir);            
        }            
    }
    
    /**
     * Extracts the initial directory value from a path passed in setSelection()
     * 
     * @return the inital directory value
     */
    private String getInitDir() {
        String projDir = _proj.getProject().getLocation().toString();
        if(_initPath != null && _initPath.segmentCount() > 1 ) {
            return projDir + IPath.SEPARATOR + _initPath.segment(1);
        }
        // else
        return projDir;
        
    }
    
    /**
     * Extracts the initial extensions list from a path passed in setSelection()
     * 
     * @return the intial comma separated list of extensions
     */
    private String getInitExts() {
        if(_initPath != null && _initPath.segmentCount() > 2 ) {
            return _initPath.segment(2);
        }
        // else 
        return DEFAULT_EXTS;
    }
        
    /**
     * @return the current extension list
     */
    protected String getExtValue() {
        return _extText.getText().trim().toLowerCase();
    }
    
    /**
     * @return the current directory
     */
    protected String getDirValue() {
        return _dirCombo.getText();
    }
    
    /**
     * @return directory relative to the parent project
     */
    protected String getRelativeDirValue() {
        int projDirLen = _proj.getProject().getLocation().toString().length();
        return getDirValue().substring( projDirLen );
    }
    
    /**
     * Checks that the directory is a subdirectory of the project being configured
     * 
     * @param dir a directory to validate
     * @return true if the directory is valid
     */
    private boolean isDirValid(String dir) {
        Path dirPath = new Path(dir);
        return _proj.getProject().getLocation().makeAbsolute().isPrefixOf(dirPath);
    }
    
    /**
     * Checks that the list of comma separated extensions are valid.  Must meet the 
     * following:
     *  - non-null and non-empty
     *  - match the regex [a-z_][a-z_,]*
     * 
     * @param exts comma separated list of extensions
     * @return true if the extension list is valid
     */
    private boolean areExtsValid(String exts) {
        if(exts==null || exts.equals("")) {
            return false;
        }        
        //else
        return exts.matches("[a-z_][a-z_,]*");
    }
    
    public boolean finish() {
        if(!areExtsValid(getExtValue())) {
            setErrorMessage(Messages.ExtErr);
            return false;    
        } else if(!isDirValid(getDirValue())) {
            setErrorMessage( NLS.bind(Messages.DirErr, _proj.getProject().getName()));            
            return false;
        }        
        return true;        
    }

    public IClasspathEntry getSelection() {
        String dir = getRelativeDirValue();
        if(dir.equals("")) {
            dir = "-";
        }
        IPath containerPath = new Path("ccw.LEININGEN_CONTAINER").append( "/" + dir + "/" + 
                                                                   getExtValue());
        return JavaCore.newContainerEntry(containerPath);
    }

    public void setSelection(IClasspathEntry containerEntry) {
        if(containerEntry != null) {
            _initPath = containerEntry.getPath();
        }        
    }    
}
