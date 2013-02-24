package ccw.leiningen;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

import ccw.CCWPlugin;
import ccw.util.ClojureInvoker;

public final class WizardNewLeiningenProjectTemplatePage extends
		WizardNewProjectCreationPage {
	
	private static final String checkProjectName = "check-project-name";

	private static final ClojureInvoker wizard = ClojureInvoker.newInvoker(
			                                         CCWPlugin.getDefault(),
			                                         "ccw.leiningen.wizard");


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

	public WizardNewLeiningenProjectTemplatePage(
			NewLeiningenProjectWizard newLeiningenProjectWizard, String pageName) {
		super(pageName);
		this.newLeiningenProjectWizard = newLeiningenProjectWizard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.dialogs.WizardNewProjectCreationPage#createControl(org
	 * .eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		locationArea = new ProjectContentsLocationArea(getErrorReporter(), (Composite) getControl());

		createLeinTemplateGroup((Composite) getControl());

		createWorkingSetGroup((Composite) getControl(),
				this.newLeiningenProjectWizard.getSelection(),
				new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
		Dialog.applyDialogFont(getControl());
	}
	
    /**
	 * Get an error reporter for the receiver.
	 * @return IErrorMessageReporter
	 */
	private IErrorMessageReporter getErrorReporter() {
		return new IErrorMessageReporter(){
			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter#reportError(java.lang.String)
			 */
			public void reportError(String errorMessage, boolean infoOnly) {
				if (infoOnly) {
					setMessage(errorMessage, IStatus.INFO);
					setErrorMessage(null);
				}
				else
					setErrorMessage(errorMessage);
				boolean valid = errorMessage == null;
				if(valid) {
					valid = validatePage();
				}
				
				setPageComplete(valid);
			}
		};
	}


	/**
	 * (non-Javadoc) Method declared on IDialogPage.
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
		projectLabel.setText("Leiningen Template to use:" /*
														 * IDEWorkbenchMessages.
														 * WizardNewProjectCreationPage_nameLabel
														 */);
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
	 * Returns the current project name as entered by the user, or its
	 * anticipated initial value.
	 * 
	 * @return the project name, its anticipated initial value, or
	 *         <code>null</code> if no project name is known
	 */
	public String getTemplateName() {
		if (templateNameField == null) {
			return initialTemplateFieldValue;
		}

		return getTemplateNameFieldValue();
	}

	/**
	 * Returns the value of the project name field with leading and trailing
	 * spaces removed.
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
	 * Sets the initial project name that this page will use when created. The
	 * name is ignored if the createControl(Composite) method has already been
	 * called. Leading and trailing spaces in the name are ignored. Providing
	 * the name of an existing project will not necessarily cause the wizard to
	 * warn the user. Callers of this method should first check if the project
	 * name passed already exists in the workspace.
	 * 
	 * @param name
	 *            initial project name for this page
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
		if (superValidatePage()) {
			String mess = (String) wizard._(checkProjectName, getProjectName());
			if (mess != null) {
				setMessage(mess, ERROR);
				return false;
			}
			
			String templateFieldContents = getTemplateNameFieldValue();
			if (templateFieldContents.equals("")) { //$NON-NLS-1$
				//setErrorMessage("The Leiningen template name cannot be empty");
				setMessage("The Leiningen template name cannot be empty", ERROR);
				return false;
			}
			if (this.getProjectName().endsWith("jure")) {
				setMessage("'" + getProjectName() + "'" + " is a discouraged project name (ends with \"jure\")", WARNING);
			}
			return true;
		} else {
			return false;
		}
	}
	
	private ProjectContentsLocationArea locationArea;
	
	/* Copied from parent class */
    protected boolean superValidatePage() {
        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

        String projectFieldContents = getProjectName();
        if (projectFieldContents.equals("")) { //$NON-NLS-1$
            setErrorMessage(null);
            setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
            return false;
        }

        IStatus nameStatus = workspace.validateName(projectFieldContents,
                IResource.PROJECT);
        if (!nameStatus.isOK()) {
            setErrorMessage(nameStatus.getMessage());
            return false;
        }

        IProject handle = getProjectHandle();
        if (handle.exists()) {
            setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectExistsMessage);
            return false;
        }
                
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				getProjectName());
		locationArea.setExistingProject(project);
		
		String validLocationMessage = locationArea.checkValidLocation();
		if (validLocationMessage != null) { // there is no destination location given
			setErrorMessage(validLocationMessage);
			return false;
		}

        setErrorMessage(null);
        setMessage(null);
        return true;
    }
	
	@Override
	public String getProjectName() {
		String name = super.getProjectName();
		if (name.contains("/")) {
			String[] parts = name.split("/");
			if (parts.length < 2) {
				return "";
			} else {
				return parts[1];
			}
		} else {
			return name;
		}
	}
	
	/**
	 * @return the raw entered project name, e.g. for Leiningen it can contain
	 *         a groupId, as in my.groupId/myArtifactId
	 */
	public String getLeiningenProjectName() {
		return super.getProjectName();
	}
}