package ccw.leiningen;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public final class WizardNewLeiningenProjectTemplatePage extends
		WizardNewProjectCreationPage {
	/**
	 * 
	 */
	private final NewLeiningenProjectWizard newLeiningenProjectWizard;
	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	// initial value stores
	private String initialTemplateFieldValue = "default";
	// widgets
	Text templateNameField;
	private Listener templateNameModifyListener = new Listener() {
	    public void handleEvent(Event e) {
	        boolean valid = validatePage();
	        setPageComplete(valid);
	    }
	};

	public WizardNewLeiningenProjectTemplatePage(NewLeiningenProjectWizard newLeiningenProjectWizard, String pageName) {
		super(pageName);
		this.newLeiningenProjectWizard = newLeiningenProjectWizard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		createLeinTemplateGroup((Composite) getControl());
		
		createWorkingSetGroup(
				(Composite) getControl(),
				this.newLeiningenProjectWizard.getSelection(),
				new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
		Dialog.applyDialogFont(getControl());
	}

	/** (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createLeinTemplateGroup(Composite parent) {
	    // project specification group
	    Composite projectGroup = new Composite(parent, SWT.NONE);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 2;
	    projectGroup.setLayout(layout);
	    GridData projectGroupLayoutData = new GridData(GridData.FILL_HORIZONTAL);
	    projectGroupLayoutData.grabExcessVerticalSpace = true;
		projectGroup.setLayoutData(projectGroupLayoutData);

	    // new template label
	    Label projectLabel = new Label(projectGroup, SWT.NONE);
	    projectLabel.setText("Leiningen Template to use:" /*IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel*/);
	    projectLabel.setFont(parent.getFont());

	    // new template name entry field
	    templateNameField = new Text(projectGroup, SWT.BORDER);
	    GridData data = projectGroupLayoutData;
	    data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	    templateNameField.setLayoutData(data);
	    templateNameField.setFont(parent.getFont());

	    // Set the initial value first before listener
	    // to avoid handling an event during the creation.
	    if (initialTemplateFieldValue != null) {
	    	templateNameField.setText(initialTemplateFieldValue);
		}
	    templateNameField.addListener(SWT.Modify, templateNameModifyListener);
	}

	/**
	 * Returns the current project name as entered by the user, or its anticipated
	 * initial value.
	 *
	 * @return the project name, its anticipated initial value, or <code>null</code>
	 *   if no project name is known
	 */
	public String getTemplateName() {
	    if (templateNameField == null) {
			return initialTemplateFieldValue;
		}

	    return getTemplateNameFieldValue();
	}

	/**
	 * Returns the value of the project name field
	 * with leading and trailing spaces removed.
	 * 
	 * @return the project name in the field
	 */
	private String getTemplateNameFieldValue() {
	    if (templateNameField == null) {
			return ""; //$NON-NLS-1$
		}

	    return templateNameField.getText().trim();
	}

	/**
	 * Sets the initial project name that this page will use when
	 * created. The name is ignored if the createControl(Composite)
	 * method has already been called. Leading and trailing spaces
	 * in the name are ignored.
	 * Providing the name of an existing project will not necessarily 
	 * cause the wizard to warn the user.  Callers of this method 
	 * should first check if the project name passed already exists 
	 * in the workspace.
	 * 
	 * @param name initial project name for this page
	 * 
	 * @see IWorkspace#validateName(String, int)
	 * 
	 */
	public void setInitialDefaultTemplate(String name) {
	    if (name == null) {
			initialTemplateFieldValue = null;
		} else {
			initialTemplateFieldValue = name.trim();
	    }
	}

	@Override
	protected boolean validatePage() {
		if (super.validatePage()) {
	        String templateFieldContents = getTemplateNameFieldValue();
	        if (templateFieldContents.equals("")) { //$NON-NLS-1$
	            setErrorMessage(null);
	            setMessage("The Leiningen template name cannot be empty");
	            return false;
	        } else {
	        	return true;
	        }
		} else {
			return false;
		}
	}
}