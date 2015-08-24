/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Andrea RICHIARDI - initial implementation
 *******************************************************************************/
package ccw.preferences;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.databinding.preference.PreferencePageSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import ccw.CCWPlugin;
import ccw.core.StaticStrings;
import ccw.editors.clojure.folding.FoldingDescriptor;
import ccw.editors.clojure.folding.FoldingModel;

/**
 * Configures Clojure's folding preferences.
 *
 */
public class FoldingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    public static String DEFAULT_FOLDING_DESCRIPTORS =
              "({:id :fold-parens"
            + " :enabled true"
            + " :loc-tags #{:list}"
            + " :label \"" + Messages.FoldingPreferencePage_fold_parens_label + "\""
            + " :description \"" + Messages.FoldingPreferencePage_fold_parens_description + "\"}"
            + "{:id :fold-double-apices "
            + " :enabled true"
            + " :loc-tags #{:string}"
            + " :label \"" + Messages.FoldingPreferencePage_fold_double_apex_label + "\""
            + " :description \"" + Messages.FoldingPreferencePage_fold_double_apex_description + "\"})";

    // Injected
    private @Inject @Named(StaticStrings.CCW_CONTEXT_VALUE_FOLDINGMODEL) FoldingModel fModel; // Model
    
    // Manually Wired
    FoldingViewModel fViewModel; // ViewModel

    // Widgets
    private CheckboxTableViewer fFoldingTableViewer;

    private Group fGrpSummary;
    private Label fSummaryFoldTargetLabel;
    private Label fSummaryDescriptionLabel;

    @Override
    public void init(IWorkbench workbench) {
        // AR - Note: init() will always be called once, the constructor can be called multiple times
        ContextInjectionFactory.inject(this, CCWPlugin.getEclipseContext());
        fViewModel = new FoldingViewModel(fModel);
        
        setPreferenceStore(CCWPlugin.getDefault().getPreferenceStore());
    }

    /**
     * @wbp.parser.constructor
     */
    public FoldingPreferencePage() {
    }
    
    /**
     * Creates page for folding preferences.
     *
     * @param parent
     *            the parent composite
     * @return the control for the preference page
     */
    public Control createContents(Composite parent) {

        ScrolledPageContent scrolled = new ScrolledPageContent(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);

        Composite cointainerComposite = new Composite(scrolled, SWT.NONE);
        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        cointainerComposite.setLayout(fillLayout);

        // Table
        Composite tableComposite = new Composite(cointainerComposite, SWT.NONE);

        fFoldingTableViewer = CheckboxTableViewer.newCheckList(tableComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
                | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
        ColumnViewerToolTipSupport.enableFor(fFoldingTableViewer, ToolTip.NO_RECREATE);

        // Setup
        fFoldingTableViewer.setUseHashlookup(true);
        fFoldingTableViewer.getTable().setHeaderVisible(true);
        fFoldingTableViewer.getTable().setLinesVisible(true);

        TableColumnLayout layout = new TableColumnLayout();
        tableComposite.setLayout(layout);

        TableViewerColumn viewerColumn = new TableViewerColumn(fFoldingTableViewer, SWT.NONE);
        TableColumn column = viewerColumn.getColumn();
        column.setText(Messages.FoldingPreferencePage_labelColumnTitle);
        column.setResizable(true);
        column.setMoveable(true);
        column.setResizable(true);
        layout.setColumnData(column, new ColumnWeightData(35, true));

        viewerColumn = new TableViewerColumn(fFoldingTableViewer, SWT.NONE);
        column = viewerColumn.getColumn();
        column.setText(Messages.FoldingPreferencePage_descriptionColumnTitle);
        column.setResizable(true);
        column.setMoveable(true);
        layout.setColumnData(column, new ColumnWeightData(40, true));

        // Summary
        Composite summaryComposite = new Composite(cointainerComposite, SWT.NONE);
        summaryComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

        fGrpSummary = new Group(summaryComposite, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginLeft = 4;
        gridLayout.marginRight = 4;
        gridLayout.horizontalSpacing = 8;
        gridLayout.verticalSpacing = 8;
        gridLayout.marginTop = 4;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        fGrpSummary.setLayout(gridLayout);

        // Summary Label
        fSummaryFoldTargetLabel = new Label(fGrpSummary, SWT.HORIZONTAL);
        GridData fieldLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        fieldLayoutData.widthHint = 120;
        fSummaryFoldTargetLabel.setLayoutData(fieldLayoutData);

        // Summary Description
        fSummaryDescriptionLabel = new Label(fGrpSummary, SWT.BORDER | SWT.WRAP);
        fieldLayoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        fieldLayoutData.heightHint = 100;
        fieldLayoutData.horizontalSpan = 2;
        fSummaryDescriptionLabel.setLayoutData(fieldLayoutData);

        summaryComposite.pack();

        // End Summary
        
        scrolled.setContent(cointainerComposite);
        final Point size = cointainerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        scrolled.setMinSize(size.x, size.y);

        Dialog.applyDialogFont(scrolled);

        initDataBindings();

        return scrolled;
    }

    protected DataBindingContext initDataBindings() {

        final DataBindingContext context = new DataBindingContext();

        // Page Support
        PreferencePageSupport.create(this, context);

        // ///////////////////////////
        // Label/Content providers \\
        // ///////////////////////////

        ObservableListContentProvider contentProvider = new ObservableListContentProvider();

        IObservableMap[] columnsObservables = Properties.observeEach(contentProvider.getKnownElements(),
                FoldingViewModel.displayDomain);

        ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(columnsObservables);

        fFoldingTableViewer.setLabelProvider(labelProvider);
        fFoldingTableViewer.setContentProvider(contentProvider);
        fFoldingTableViewer.setInput(fViewModel.observableList);

        /////////////////////
        // Selection logic \\
        /////////////////////

        final IObservableValue selected = ViewersObservables.observeSingleSelection(fFoldingTableViewer);

        final IObservableValue isSelected = new ComputedValue(Boolean.TYPE) {
            @Override
            protected Object calculate() {
                return Boolean.valueOf(selected.getValue() != null);
            }
        };

        context.bindValue(WidgetProperties.enabled().observe(fGrpSummary), isSelected);
        
        if (!fViewModel.observableList.isEmpty()) {
            selected.setValue(fViewModel.observableList.get(0));
        }

        /////////////////
        // Check logic \\
        /////////////////

        context.bindSet(ViewersObservables.observeCheckedElements(fFoldingTableViewer, FoldingDescriptor.class),
                fViewModel.checkedSet);

        /////////////////
        // UI Bindings \\
        /////////////////

        context.bindValue(WidgetProperties.text().observe(fSummaryFoldTargetLabel),
                FoldingViewModel.labelProperty.observeDetail(selected));

        context.bindValue(WidgetProperties.text().observe(fSummaryDescriptionLabel),
                FoldingViewModel.descriptionProperty.observeDetail(selected));

        return context;
    }

    public boolean performOk() {
        fModel.persistDescriptors(fViewModel.observableList);
        
        boolean result = true;
        try {
            Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE).node(CCWPlugin.PLUGIN_ID).flush();
        } catch (BackingStoreException e) {
            CCWPlugin.logError("Saving Preferences failed", e);
            result = false;
        }
        return result;
    }
}
