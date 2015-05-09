package ccw.core;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.framework.Bundle;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import ccw.CCWPlugin;
import ccw.TraceOptions;
import ccw.util.BundleUtils;
import ccw.util.ClojureInvoker;

/**
 * Called by the Eclipse 4 framework before the Eclipse Model is totally
 * created. Serves as a hook for triggering, when the App startup is complete,
 * the load of user contributions.
 *
 * @author laurentpetit
 */
public class Eclipse4ModelProcessor {

	public Eclipse4ModelProcessor() { }

    @Execute
    public void startUserPlugins(final MApplication app, final EModelService modelService,
    		final IEventBroker eventBroker) throws CoreException {
		CCWPlugin.getTracer().trace(TraceOptions.LOG_INFO,
				"CCW Model Processor called.");

		eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE,
				new EventHandler() {
					@Override
					public void handleEvent(Event event) {
						CCWPlugin.getTracer().trace(TraceOptions.LOG_INFO,
                               "App startup complete, launching user plugins");
						launchUserPluginsWithinJob(app);
					}
				});
    }

    private void launchUserPluginsWithinJob(final MApplication app) {
    	Job j = new WorkspaceJob("CCW User Plugins launcher") {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				try {
					Bundle bundle = BundleUtils.loadAndGetBundle("ccw.core");

					ClojureInvoker e4Model = ClojureInvoker.newInvoker(bundle, "ccw.e4.model");
					e4Model._("application!", app);

			    	ClojureInvoker userPlugins = ClojureInvoker.newInvoker(bundle, "ccw.core.user-plugins");
			    	userPlugins._("start-user-plugins");
				} catch (CoreException e) {
					return CCWPlugin.createErrorStatus("Error while loading Counterclockwise User plugins", e);
				}
				return Status.OK_STATUS;
			}
		};
		j.setUser(false);
		j.schedule();
    }

}
