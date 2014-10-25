package ccw;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.ui.internal.clone.GitImportWizard;
import org.eclipse.egit.ui.internal.provisional.wizards.GitRepositoryInfo;
import org.eclipse.egit.ui.internal.provisional.wizards.IRepositorySearchResult;
import org.eclipse.egit.ui.internal.provisional.wizards.NoRepositoryInfoException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import ccw.util.DisplayUtil;

public class CCWDropAdapterEarlyStartup implements IStartup {

	private static final int[] PREFERRED_DROP_OPERATIONS = {
		DND.DROP_LINK, DND.DROP_COPY, DND.DROP_MOVE, DND.DROP_DEFAULT };

	private static final int DROP_OPERATIONS = DND.DROP_MOVE | DND.DROP_COPY
			| DND.DROP_LINK | DND.DROP_DEFAULT;

	private final DropTargetListener dropListener = new CreateProjectDropTargetListener();

	private final FileTransfer fileTransfer = FileTransfer.getInstance();
	private final URLTransfer urlTransfer = URLTransfer.getInstance();

	private final WorkbenchListener workbenchListener = new WorkbenchListener();

	private Transfer[] transferAgents;

	@Override
	public void earlyStartup() {
		System.out.println("CCW EARLY STARTUP");
		UIJob registerJob = new UIJob(Display.getDefault(),
				"CCWDropAdapterEarlyStartup") {
			{
				setPriority(Job.SHORT);
				setSystem(true);
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				workbench.addWindowListener(workbenchListener);
				IWorkbenchWindow[] workbenchWindows = workbench
						.getWorkbenchWindows();
				for (IWorkbenchWindow window : workbenchWindows) {
					workbenchListener.hookWindow(window);
				}
				return Status.OK_STATUS;
			}

		};
		registerJob.schedule();
	}

	public void installDropTarget(final Shell shell) {
		hookUrlTransfer(shell, dropListener);
	}

	private DropTarget hookUrlTransfer(final Shell c,
			DropTargetListener dropTargetListener) {
		DropTarget target = findDropTarget(c);
		if (target != null) {
			// target exists, get it and check proper registration
			registerWithExistingTarget(target, fileTransfer);
			registerWithExistingTarget(target, urlTransfer);
		} else {
			target = new DropTarget(c, DROP_OPERATIONS);
			if (transferAgents == null) {
				transferAgents = new Transfer[] { fileTransfer, urlTransfer };
			}
			target.setTransfer(transferAgents);
		}
		registerDropListener(target, dropTargetListener);

		hookChildren(c, dropTargetListener);

		return target;
	}

	private void registerDropListener(DropTarget target,
			DropTargetListener dropTargetListener) {
		target.removeDropListener(dropTargetListener);
		target.addDropListener(dropTargetListener);
	}

	private void hookChildren(Control c, DropTargetListener dropTargetListener) {
		if (c instanceof Composite) {
			Control[] children = ((Composite) c).getChildren();
			for (Control child : children) {
				hookRecursive(child, dropTargetListener);
			}
		}
	}

	private void hookRecursive(Control c, DropTargetListener dropTargetListener) {
		DropTarget target = findDropTarget(c);
		if (target != null) {
			// target exists, get it and check proper registration
			registerWithExistingTarget(target, fileTransfer);
			registerWithExistingTarget(target, urlTransfer);
			registerDropListener(target, dropTargetListener);
		}
		hookChildren(c, dropTargetListener);
	}

	private static void registerWithExistingTarget(DropTarget target,
			Transfer transfer) {
		Transfer[] transfers = target.getTransfer();
		if (transfers == null)
			return;
		for (Transfer t : transfers) {
			if (transfer.getClass().isInstance(t)) {
				return;
			}
		}
		Transfer[] newTransfers = new Transfer[transfers.length + 1];
		System.arraycopy(transfers, 0, newTransfers, 0, transfers.length);
		newTransfers[transfers.length] = transfer;
		target.setTransfer(newTransfers);
	}

	private DropTarget findDropTarget(Control control) {
		Object object = control.getData(DND.DROP_TARGET_KEY);
		if (object instanceof DropTarget) {
			return (DropTarget) object;
		}
		return null;
	}

	private class CreateProjectDropTargetListener extends DropTargetAdapter {

		@Override
		public void dragEnter(DropTargetEvent e) {
			updateDragDetails(e);
		}

		@Override
		public void dragOver(DropTargetEvent e) {
			updateDragDetails(e);
		}

		@Override
		public void dragLeave(DropTargetEvent e) {
			if (e.detail == DND.DROP_NONE) {
				setDropOperation(e);
			}
		}

		@Override
		public void dropAccept(DropTargetEvent e) {
			updateDragDetails(e);
		}

		@Override
		public void dragOperationChanged(DropTargetEvent e) {
			updateDragDetails(e);
		}

		private void setDropOperation(DropTargetEvent e) {
			int allowedOperations = e.operations;
			for (int op : PREFERRED_DROP_OPERATIONS) {
				if ((allowedOperations & op) != 0) {
					e.detail = op;
					traceDropOperation(e.detail);
					return;
				}
			}
			e.detail = allowedOperations;
			traceDropOperation(e.detail);
		}
		private void traceDropOperation(int op) {
/*
			if ((op & DND.DROP_COPY) != 0)
				System.out.println("DROP_COPY");
			if (op  == DND.DROP_DEFAULT)
				System.out.println("DROP_DEFAULT");
			if ((op & DND.DROP_LINK) != 0)
				System.out.println("DROP_LINK");
			if ((op & DND.DROP_MOVE) != 0)
				System.out.println("DROP_MOVE");
			if (op == DND.DROP_NONE)
				System.out.println("DROP_NONE");
*/
		}

		private void updateDragDetails(DropTargetEvent e) {
			if (dropTargetIsValid(e)) {
				setDropOperation(e);
			}
		}

		private boolean dropTargetIsValid(DropTargetEvent e) {
			return fileTransfer.isSupportedType(e.currentDataType) || urlTransfer.isSupportedType(e.currentDataType);
		}

		@Override
		public void drop(DropTargetEvent event) {
			System.out.println("drop " + event.widget.hashCode());
			if (urlTransfer.isSupportedType(event.currentDataType)) {
				// TODO url fetching on windows more complex than that => check how Eclise market place does it
				final String url = getUrlFromEvent(event);
				if (url != null &&
						(url.startsWith("https://github.com/")
						|| url.startsWith("https://bitbucket.org/")
						|| url.startsWith("https://code.google.com/"))) {
					final String sanitizedUrl = sanitizeForGit(url);
					CCWPlugin.getTracer().trace(TraceOptions.LOG_INFO, "URL dropped: " + url + ". Once sanitized: " + sanitizedUrl);
					DisplayUtil.asyncExec(new Runnable() {
						@Override public void run() {
							GitImportWizard w1 = new GitImportWizard(new IRepositorySearchResult() {
								@Override
								public GitRepositoryInfo getGitRepositoryInfo()
										throws NoRepositoryInfoException {
									return new GitRepositoryInfo(sanitizedUrl);
								}
							});
							new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), w1).open();
						}
					});
				} else {
					event.detail = DND.DROP_NONE;
				}
				return;
			}
			if (!fileTransfer.isSupportedType(event.currentDataType)) {
				// ignore
				return;
			}
			if (event.data == null || !dropTargetIsValid(event)) {
				event.detail = DND.DROP_NONE;
				return;
			}
			setDropOperation(event);
			final String[] files = getFilesFromEvent(event);
			final List<File> candidateProjects = new ArrayList<File>();
			collectCandidateProjects(files, candidateProjects);
			if (candidateProjects.size() > 0) {
				DisplayUtil.asyncExec(new Runnable() {
					@Override public void run() {
						String msg;
						if (candidateProjects.size() == 1) {
							msg = "Find and create projects from " + candidateProjects.get(0) + "?";
						} else {
							msg = "Find and create projects from ... ?\n";
							for (File file: candidateProjects) {
								msg += "  - " + file.getAbsolutePath() + "\n";
							}
						}
						boolean c = MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								"Drag & Drop project creation",
								msg);
						if (c) {
						    proceedLeiningenProjectsCreation(candidateProjects);
						}
					}
				});
			} else {
				event.detail = DND.DROP_NONE;
			}
		}

		private final Pattern p = Pattern.compile("(https?://[^/]+/[^/]+/[^/]+).*");
		private String sanitizeForGit(String url) {
			Matcher m = p.matcher(url);
			return m.matches() ? m.group(1) : null;
		}

		// Taken from Eclipse Market Place code, see class
        // org.eclipse.epp.mpc.ui/src/org/eclipse/epp/internal/mpc/ui/wizards/MarketplaceDropAdapter.java
		// in repository ttp://git.eclipse.org/gitroot/mpc/org.eclipse.epp.mpc.git
        // Depending on the form the link and browser/os,
        // we get the url twice in the data separated by new lines
        private String getUrlFromEvent(DropTargetEvent event) {
                Object eventData = event.data;
                if (eventData == null || !(eventData instanceof String)) {
                        return null;
                }
                String[] dataLines = ((String) eventData).split(System.getProperty("line.separator")); //$NON-NLS-1$
                String url = dataLines[0];
                return url;
        }

		private String[] getFilesFromEvent(DropTargetEvent event) {
			Object eventData = event.data;
			if (eventData == null)
				return null;
			if (!(eventData instanceof String[]))
				return null;

			String[] files = (String[]) eventData;

			String[] ret = new String[files.length];

			System.arraycopy(files, 0, ret, 0, files.length);

			return ret;
		}

		private void collectCandidateProjects(String[] files, List<File> collect) {
			for (String f : files) {
				File file = new File(f);
				collectCandidateProjects(file, collect);
			}
		}

		private void collectCandidateProjects(File folder, List<File> collect) {

			if (!folder.exists() || !folder.isDirectory()) {
				return;
			}

			File projectClj = new File(folder, "project.clj");

			if (!projectClj.exists()) {
				// try recursively
				for (File subFolder: getSubFolders(folder)) {
					collectCandidateProjects(subFolder, collect);
				}
			} else {
				// projectClj found, check it's not already an Eclipse project location
				IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(folder.toURI());
				if (containers.length == 0) {
					collect.add(folder);
				} else {
					CCWPlugin.getTracer().trace(TraceOptions.LOG_INFO, "No project will be created for folder '"
							+ folder.getAbsolutePath() + "': project with same folder exists in workspace");
				}
			}
		}

		private File[] getSubFolders(File folder) {
			return folder.listFiles(new FileFilter() {
				@Override public boolean accept(File c) {
					return c.isDirectory();
				}
			});
		}

		private void proceedLeiningenProjectsCreation(List<File> folders) {
			for (File folder: folders) {
				proceedLeiningenProjectCreation(folder);
			}
		}

		private void proceedLeiningenProjectCreation(final File folder) {

			WorkspaceJob wj = new WorkspaceJob("Creation of Eclipse project for " + folder.getAbsolutePath()) {

				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					if (monitor == null) monitor = new NullProgressMonitor();

					final String initialProjectName = folder.getName();
					// find a project name matching the folder name
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					Set<String> projectNames = new HashSet<String>(projects.length);
					for (IProject project: projects) {
						projectNames.add(project.getName());
					}
					String maybeProjectName = initialProjectName;
					int i = 1;
					while (projectNames.contains(maybeProjectName)) {
						maybeProjectName = initialProjectName + i;
						i++;
					}

					final String projectName = maybeProjectName;

					// Let's create the eclipse project
					final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

					IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
					if (folder.getParentFile().getAbsolutePath().equals(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath())) {
						// Special case: when the projects resides inside the workspace path,
						// location must be set to null to assume the "default location"
						desc.setLocation(null);
					} else {
						desc.setLocation(new Path(folder.getAbsolutePath()));
					}
					project.create(desc, monitor);
					project.open(monitor);

					// Add project to current WorkingSet
					IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
					IWorkingSet[] workingSets = workingSetManager.getWorkingSets();
					if (workingSets != null && workingSets.length > 0) {
						workingSetManager
								.addToWorkingSets(
										project,
								workingSets);
					}
					DisplayUtil.asyncExec(new Runnable(){
						@Override public void run() {
							MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									"Project " + projectName + " created",
									"The Eclipse project " + projectName + " has been created for filesystem folder " + folder.getAbsolutePath());
						}});
					return Status.OK_STATUS;
				}
			};
			wj.setPriority(Job.INTERACTIVE);
			wj.setUser(true);
			wj.setRule(ResourcesPlugin.getWorkspace().getRoot());
			wj.schedule();
		}
	}

	private class WorkbenchListener implements IPartListener2, IPageListener,
			IPerspectiveListener, IWindowListener {

		@Override
		public void perspectiveActivated(IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
			pageChanged(page);
		}

		@Override
		public void perspectiveChanged(IWorkbenchPage page,
				IPerspectiveDescriptor perspective, String changeId) {
		}

		@Override
		public void pageActivated(IWorkbenchPage page) {
			pageChanged(page);
		}

		@Override
		public void pageClosed(IWorkbenchPage page) {
		}

		@Override
		public void pageOpened(IWorkbenchPage page) {
			pageChanged(page);
		}

		private void pageChanged(IWorkbenchPage page) {
			if (page == null) {
				return;
			}
			IWorkbenchWindow workbenchWindow = page.getWorkbenchWindow();
			windowChanged(workbenchWindow);
		}

		@Override
		public void windowActivated(IWorkbenchWindow window) {
			windowChanged(window);
		}

		private void windowChanged(IWorkbenchWindow window) {
			if (window == null) {
				return;
			}
			Shell shell = window.getShell();
			runUpdate(shell);
		}

		@Override
		public void windowDeactivated(IWorkbenchWindow window) {
		}

		@Override
		public void windowClosed(IWorkbenchWindow window) {
		}

		@Override
		public void windowOpened(IWorkbenchWindow window) {
			hookWindow(window);
		}

		public void hookWindow(IWorkbenchWindow window) {
			window.addPageListener(this);
			window.addPerspectiveListener(this);
			IPartService partService = (IPartService) window
					.getService(IPartService.class);
			partService.addPartListener(this);
			windowChanged(window);
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		private void partUpdate(IWorkbenchPartReference partRef) {
			IWorkbenchPage page = partRef.getPage();
			pageChanged(page);
		}

		private void runUpdate(final Shell shell) {
			if (shell == null || shell.isDisposed()) {
				return;
			}
			Display display = shell.getDisplay();
			if (display == null || display.isDisposed()) {
				return;
			}
			try {
				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						if (!shell.isDisposed()) {
							installDropTarget(shell);
						}
					}
				});
			} catch (SWTException ex) {
				if (ex.code == SWT.ERROR_DEVICE_DISPOSED) {
					// ignore
					return;
				}
				CCWPlugin.getTracer().trace(TraceOptions.LOG_ERROR, ex, "Exception while trying to install/upgrade drop targets");
			} catch (RuntimeException ex) {
				CCWPlugin.getTracer().trace(TraceOptions.LOG_ERROR, ex, "Exception while trying to install/upgrade drop targets");
			}
		}
	}
}
