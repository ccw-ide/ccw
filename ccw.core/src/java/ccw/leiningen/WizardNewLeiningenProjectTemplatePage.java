package ccw.leiningen;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import ccw.CCWPlugin;
import ccw.util.ClojureInvoker;

public final class WizardNewLeiningenProjectTemplatePage extends WizardPage {
	
	private static final String checkProjectName = "check-project-name";

	private final ClojureInvoker wizard = ClojureInvoker.newInvoker(
			                                         CCWPlugin.getDefault(),
			                                         "ccw.leiningen.wizard");


	private final NewLeiningenProjectWizard containingWizard;
	
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    private Text projectNameText;
	
	private Button defaultLocationCheckbox;
	private Text locationText;

	private Text templateNameText;
	private String initialTemplateNameTextValue = "default";
	
	private Listener templateNameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};

	public WizardNewLeiningenProjectTemplatePage(
			NewLeiningenProjectWizard newLeiningenProjectWizard, String pageName) {
		super(pageName);
		this.containingWizard = newLeiningenProjectWizard;
	}

	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        
        initializeDialogUnits(parent);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
                IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

        FormLayout layout = new FormLayout();
		composite.setLayout(layout);
        
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite projectGroup = createProjectNameGroup(composite);
        FormData projectLayoutData = new FormData();
		projectGroup.setLayoutData(projectLayoutData);
		
		Composite createInGroup = createCreateInGroup(composite);

		FormData createInLayoutData = new FormData();
		createInLayoutData.top = new FormAttachment(projectGroup);
		createInLayoutData.left = new FormAttachment(0, 0);
		createInLayoutData.right = new FormAttachment(100, 0);
		createInGroup.setLayoutData(createInLayoutData);
		
        Composite leinTemplateGroup = createLeinTemplateGroup(composite);
        FormData leinTemplateLayoutData = new FormData();
        leinTemplateLayoutData.top = new FormAttachment(createInGroup);
        leinTemplateLayoutData.left = new FormAttachment(0, 0);
        leinTemplateLayoutData.right = new FormAttachment(100, 0);
        leinTemplateGroup.setLayoutData(leinTemplateLayoutData);
        
        setPageComplete(validatePage());
        
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        
        setControl(composite);

		Dialog.applyDialogFont(getControl());
	}
	
    private Composite createCreateInGroup(Composite parent) {
    	Composite composite = new Composite(parent, SWT.NONE);
    	FormLayout layout = new FormLayout();
    	layout.marginWidth = 5;
    	layout.marginHeight = 10;
    	layout.spacing = 5;
    	composite.setLayout(layout);
    	
    	Label label = new Label(composite, SWT.NONE);
    	label.setText("Create in:");
    	FormData labelData = new FormData();
    	label.setLayoutData(labelData);
    	
    	defaultLocationCheckbox = new Button(composite, SWT.CHECK);
    	defaultLocationCheckbox.setText("default location");
    	defaultLocationCheckbox.setSelection(true);
    	FormData defaultLocationCheckboxData = new FormData();
    	defaultLocationCheckboxData.left = new FormAttachment(label);
    	defaultLocationCheckboxData.top = new FormAttachment(label, 0, SWT.CENTER);
    	defaultLocationCheckbox.setLayoutData(defaultLocationCheckboxData);
    	
    	locationText = new Text(composite, SWT.BORDER);
    	FormData locationData = new FormData();
    	locationData.top = new FormAttachment(defaultLocationCheckbox);
    	locationData.left = new FormAttachment(defaultLocationCheckbox, 0, SWT.LEFT);
    	locationText.setLayoutData(locationData);
    	locationText.setText(getDefaultParentLocation());
    	locationText.setFont(parent.getFont());
    	locationText.setToolTipText("Location in which the project directory will be created");
    	locationText.setEnabled(false);
    	
    	locationText.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
    	
    	final Button browse = new Button(composite, SWT.PUSH);
    	FormData browseData = new FormData();
    	browseData.top = new FormAttachment(locationText, 0, SWT.CENTER);
    	browseData.right = new FormAttachment(100, 0);
    	browse.setLayoutData(browseData);
    	browse.setText("Browse ...");
    	browse.setFont(parent.getFont());
    	browse.setEnabled(false);
    	browse.setToolTipText("Click to open a popup for choosing the location the project directory will be created in");
    	
    	locationData.right = new FormAttachment(browse);
    	
    	defaultLocationCheckbox.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				locationText.setEnabled(!defaultLocationCheckbox.getSelection());
				browse.setEnabled(!defaultLocationCheckbox.getSelection());
				if (defaultLocationCheckbox.getSelection()) {
					locationText.setText(getDefaultParentLocation());
				}
				setPageComplete(validatePage());
			}
			@Override public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
    	
    	browse.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(browse.getShell(), SWT.NONE);
				dialog.setText("Choose Project parent directory");
				dialog.setMessage("Select a parent directory within with the project's directory will be created");
				dialog.setFilterPath(locationText.getText());
				String result = dialog.open();
				if (result != null) {
					locationText.setText(result);
					setPageComplete(validatePage());
				}
			}
			@Override public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
    	
		return composite;
	}
    
    private String getDefaultParentLocation() {
    	return ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
    }

    public String getSafeProjectFieldValue() {
        if (projectNameText == null) {
			return "";
		}

        return getProjectNameFieldValue();
    }
    
	public String getProjectName() {
		String name = getSafeProjectFieldValue();
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
     * Returns the value of the project name field
     * with leading and trailing spaces removed.
     * @return the project name in the field
     */
    private String getProjectNameFieldValue() {
        if (projectNameText == null) {
			return ""; //$NON-NLS-1$
		}

        return projectNameText.getText().trim();
    }

    private Listener nameModifyListener = new Listener() {
        public void handleEvent(Event e) {
            setPageComplete(validatePage());
        }
    };

    /**
     * Creates the project name specification controls.
     * @param parent the parent composite
     */
    private final Composite createProjectNameGroup(Composite parent) {
        // project specification group
        Composite projectGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        projectGroup.setLayout(layout);

        // new project label
        Label projectLabel = new Label(projectGroup, SWT.NONE);
        projectLabel.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel);
        projectLabel.setFont(parent.getFont());

        // new project name entry field
        projectNameText = new Text(projectGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        projectNameText.setLayoutData(data);
        projectNameText.setFont(parent.getFont());

        projectNameText.addListener(SWT.Modify, nameModifyListener);
        BidiUtils.applyBidiProcessing(projectNameText, BidiUtils.BTD_DEFAULT);
        
        return projectGroup;
    }

	public Composite createLeinTemplateGroup(Composite parent) {
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);

		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText("Leiningen template:");
		projectLabel.setFont(parent.getFont());

		templateNameText = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		templateNameText.setLayoutData(data);
		templateNameText.setFont(parent.getFont());

		if (initialTemplateNameTextValue != null) {
			templateNameText.setText(initialTemplateNameTextValue);
		}
		
		templateNameText.addListener(SWT.Modify, templateNameModifyListener);
		
		return projectGroup;
	}

	/**
	 * @return the template name to use, potentially initialized to its
	 *         default value if empty
	 */
	public String computeTemplateName() {
		String fieldValue = getTemplateNameFieldValue();
		
		if (fieldValue == null || fieldValue.equals("")) {
			return initialTemplateNameTextValue;
		}

		return fieldValue;
	}

	/**
	 * Returns the value of the project name field with leading and trailing
	 * spaces removed.
	 * 
	 * @return the project name in the field
	 */
	private String getTemplateNameFieldValue() {
		if (templateNameText == null) {
			return ""; //$NON-NLS-1$
		}

		return templateNameText.getText().trim();
	}

	protected boolean validatePage() {
        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

        String projectFieldContents = getProjectName();
        if (projectFieldContents.trim().equals("")) { //$NON-NLS-1$
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

        IProject projectHandle = getProjectHandle();
        if (projectHandle.exists()) {
            setErrorMessage("A project with name '" + projectFieldContents + "' already exists in the workspace.");
            return false;
        }
        
		String validLocationMessage = checkValidLocation(
				projectHandle, 
				this.defaultLocationCheckbox.getSelection(),
				this.locationText.getText(),
				getProjectName());
		if (validLocationMessage != null) {
			setErrorMessage(validLocationMessage);
			return false;
		}

		String mess = (String) wizard._(checkProjectName, getProjectName());
		if (mess != null) {
			setErrorMessage(mess);
			return false;
		}
		
		setErrorMessage(null);
		if (this.getProjectName().endsWith("jure")) {
			setMessage("'" + getProjectName() + "'" + " is a discouraged project name (ends with \"jure\")", WARNING);
		} else {
			setMessage(null);
		}
		
		return true;
	}
	
    /**
	 * Creates a project resource handle for the current project name field
	 * value. The project handle is created relative to the workspace root.
	 * <p>
	 * This method does not create the project resource; this is the
	 * responsibility of <code>IProject::create</code> invoked by the new
	 * project resource wizard.
	 * </p>
	 * 
	 * @return the new project resource handle
	 */
    public IProject getProjectHandle() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(
                getProjectName());
    }

    /**
	 * Check if the entry in the widget location is valid. If it is valid return
	 * null. Otherwise return a string that indicates the problem.
	 * 
	 * @see Copied from ProjectContentsLocationArea
	 * 
	 * @return String
	 */
	public String checkValidLocation(IProject project, boolean isDefaultParentLocation, String projectParentLocation, String projectFolderName) {

		if (projectParentLocation == null || projectParentLocation.trim().equals("")) {
			return "A location to create the project folder in must be specified";
		}
		File parentFolder = new File(projectParentLocation);
		
		if (!parentFolder.exists()) {
			return "Parent folder '" + projectParentLocation + "' does not exist";
		}
		
		File file = new File(parentFolder, projectFolderName);
		URI uri = file.toURI();
		if (uri == null) {
			return IDEWorkbenchMessages.ProjectLocationSelectionDialog_locationError;
		}
		
		if (file.exists()) {
			return "Cannot create a project at an existing location (" + file.getAbsolutePath() + ")";
		}

		IStatus locationStatus = ResourcesPlugin.getWorkspace()
				.validateProjectLocationURI(project,
						// We need to test parent location with default parent location to prevent false positives
						(isDefaultParentLocation || projectParentLocation.equals(getDefaultParentLocation())) 
						? null 
					    : uri);

		if (!locationStatus.isOK()) {
			return locationStatus.getMessage();
		}

		return null;
	}
	
	/**
	 * @return the raw entered project name, e.g. for Leiningen it can contain
	 *         a groupId, as in my.groupId/myArtifactId
	 */
	public String getLeiningenProjectName() {
		return getSafeProjectFieldValue();
	}
	
    /**
     * Returns the useDefaults.
     * @return boolean
     */
    public boolean useDefaultProjectParentLocation() {
    	return defaultLocationCheckbox.getSelection();
    }
    
    /**
     * The specific location for the project folder, or null if default location
     */
    public URI getLocationURI() {
    	if (defaultLocationCheckbox.getSelection()) {
    		return null;
    	} else {
    		String parentDir = locationText.getText();
    		String projectFolder = getProjectName();
    		return new File(parentDir, projectFolder).toURI();
    	}
    }

    @Override
    public boolean isPageComplete() {
    	// TODO Auto-generated method stub
    	return super.isPageComplete();
    }
}