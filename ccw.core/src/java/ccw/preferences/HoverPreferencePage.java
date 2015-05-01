/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ccw.preferences;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.databinding.preference.PreferencePageSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.CellEditorProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import ccw.CCWPlugin;
import ccw.core.StaticStrings;
import ccw.editors.clojure.hovers.HoverDescriptor;
import ccw.editors.clojure.hovers.HoverModel;

/**
 * Configures Clojure's hover preferences.
 *
 */
public class HoverPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    // Injected
    private @Inject @Named(StaticStrings.CCW_CONTEXT_VALUE_HOVERMODEL) HoverModel fModel; // Model
    
    // Manually Wired
    HoverViewModel fViewModel; // ViewModel

    // Widgets
    private CheckboxTableViewer fHoverTableViewer;
    private TableViewerColumn fModifierTableViewerColumn;
    private TextCellEditor fHoverModifierStringTextCellEditor;

    private Group fGrpSummary;
    private Label fSummaryHoverNameLabel;
    private Label fSummaryHoverDescriptionLabel;
    private Text fSummaryHoverModifierStringText;

    @Override
    public void init(IWorkbench workbench) {
        // AR - Note: init() will always be called once, the constructor can be called multiple times
        ContextInjectionFactory.inject(this, CCWPlugin.getEclipseContext());
        fViewModel = new HoverViewModel(fModel);
        
        setPreferenceStore(CCWPlugin.getDefault().getPreferenceStore());
    }

    /**
     * @wbp.parser.constructor
     */
    public HoverPreferencePage() {
    }
    
    /**
     * Creates page for hover preferences.
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

        // Hover Table
        Composite hoverComposite = new Composite(cointainerComposite, SWT.NONE);

        fHoverTableViewer = CheckboxTableViewer.newCheckList(hoverComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
                | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
        ColumnViewerToolTipSupport.enableFor(fHoverTableViewer, ToolTip.NO_RECREATE);

        // Cell Editor
        fHoverModifierStringTextCellEditor = new TextCellEditor(fHoverTableViewer.getTable());

        // Setup
        fHoverTableViewer.setUseHashlookup(true);
        fHoverTableViewer.getTable().setHeaderVisible(true);
        fHoverTableViewer.getTable().setLinesVisible(true);

        TableColumnLayout layout = new TableColumnLayout();
        hoverComposite.setLayout(layout);

        TableViewerColumn viewerColumn = new TableViewerColumn(fHoverTableViewer, SWT.NONE);
        TableColumn column = viewerColumn.getColumn();
        column.setText(Messages.HoverPreferencePage_labelColumnTitle);
        column.setResizable(true);
        column.setMoveable(true);
        column.setResizable(true);
        layout.setColumnData(column, new ColumnWeightData(35, true));

        fModifierTableViewerColumn = new TableViewerColumn(fHoverTableViewer, SWT.NONE);
        column = fModifierTableViewerColumn.getColumn();
        column.setText(Messages.HoverPreferencePage_modifierColumnTitle);
        column.setResizable(true);
        column.setMoveable(true);
        layout.setColumnData(column, new ColumnWeightData(25, true));

        viewerColumn = new TableViewerColumn(fHoverTableViewer, SWT.NONE);
        column = viewerColumn.getColumn();
        column.setText(Messages.HoverPreferencePage_descriptionColumnTitle);
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
        fSummaryHoverNameLabel = new Label(fGrpSummary, SWT.HORIZONTAL);
        GridData fieldLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        fieldLayoutData.widthHint = 120;
        fSummaryHoverNameLabel.setLayoutData(fieldLayoutData);

        // Summary Modifier String
        fSummaryHoverModifierStringText = new Text(fGrpSummary, SWT.BORDER);
        fieldLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        fieldLayoutData.widthHint = 120;
        fSummaryHoverModifierStringText.setLayoutData(fieldLayoutData);

        // Summary Description
        fSummaryHoverDescriptionLabel = new Label(fGrpSummary, SWT.BORDER | SWT.WRAP);
        fieldLayoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        fieldLayoutData.heightHint = 100;
        fieldLayoutData.horizontalSpan = 2;
        fSummaryHoverDescriptionLabel.setLayoutData(fieldLayoutData);

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
                HoverViewModel.hoverDisplayDomain);

        ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(columnsObservables);

        fHoverTableViewer.setLabelProvider(labelProvider);
        fHoverTableViewer.setContentProvider(contentProvider);
        fHoverTableViewer.setInput(fViewModel.hoverObservableList);

        //////////////////////////
        // Selected hover logic \\
        //////////////////////////

        final IObservableValue selectedHover = ViewersObservables.observeSingleSelection(fHoverTableViewer);

        final IObservableValue isHoverSelected = new ComputedValue(Boolean.TYPE) {
            @Override
            protected Object calculate() {
                return Boolean.valueOf(selectedHover.getValue() != null);
            }
        };

        context.bindValue(WidgetProperties.enabled().observe(fGrpSummary), isHoverSelected);
        context.bindValue(WidgetProperties.enabled().observe(fSummaryHoverModifierStringText), isHoverSelected);

        final IObservableValue stateMaskOfSelected = HoverViewModel.hoverStateMask.observeDetail(selectedHover);
        final IObservableValue modifierStringOfSelected = HoverViewModel.hoverModifierString
                .observeDetail(selectedHover);

        final UpdateValueStrategy modifierStringStrategyConvert = new UpdateValueStrategy(
                UpdateValueStrategy.POLICY_CONVERT);
        modifierStringStrategyConvert.setConverter(new HoverViewModel.StateMaskToModifierConverter());

        final Binding stateMaskToModifierBinding = context.bindValue(modifierStringOfSelected, stateMaskOfSelected,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modifierStringStrategyConvert);

        if (!fViewModel.hoverObservableList.isEmpty()) {
            selectedHover.setValue(fViewModel.hoverObservableList.get(0));
        }
        
        //////////////////////
        // Validation logic \\
        //////////////////////

        MultiValidator validator = new MultiValidator() {

            @Override
            protected IStatus validate() {
                IStatus status = ValidationStatus.ok();

                if (isHoverSelected.getValue() == Boolean.TRUE) {
                    status = fViewModel.stateMaskValidator.validate(stateMaskOfSelected.getValue());
                }

                if (status.isOK()) {
                    Iterator<HoverDescriptor> it = fViewModel.checkedSet.iterator();
                    while (it.hasNext()) {
                        int stateMask = it.next().getStateMask();
                        if (!fViewModel.isStateMaskUnique(stateMask)) {
                            status = fViewModel.stateMaskError(stateMask);
                        }
                    }
                }
                return status;
            }
        };

        context.addValidationStatusProvider(validator);

        //////////////////////////
        // Checked hovers logic \\
        //////////////////////////

        // AR - because of some ordering issue problem, I am disabling the automatic
        // update from model to target of the checked hovers and...
        final UpdateSetStrategy checkedModelToTargetStrategy = new UpdateSetStrategy(UpdateValueStrategy.POLICY_ON_REQUEST);

        // AR - ... add an explicit binding that will be needed...
        final Binding checkedBindSet = context.bindSet(ViewersObservables.observeCheckedElements(fHoverTableViewer, HoverDescriptor.class),
                fViewModel.checkedSet, null, checkedModelToTargetStrategy);
        
        // AR - ...to manually trigger the update when new elements are added to the provider...
        IObservableSet realizedElements = contentProvider.getRealizedElements();
        realizedElements.addChangeListener(new IChangeListener() {
            @Override
            public void handleChange(ChangeEvent event) {
                checkedBindSet.updateModelToTarget();
            }
        });
        
        /////////////////
        // UI Bindings \\
        /////////////////

        EditingSupport modifierEditorSupport = ObservableValueEditingSupport.create(fHoverTableViewer, context,
                fHoverModifierStringTextCellEditor,
                CellEditorProperties.control().value(WidgetProperties.text(SWT.Modify)),
                HoverViewModel.hoverModifierString);

        fModifierTableViewerColumn.setEditingSupport(modifierEditorSupport);

        final UpdateValueStrategy statusMaskToModifierStrategy = new UpdateValueStrategy();
        statusMaskToModifierStrategy.setConverter(new HoverViewModel.StateMaskToModifierConverter());

        final UpdateValueStrategy modifierToStatusMaskStrategy = new UpdateValueStrategy();
        modifierToStatusMaskStrategy.setConverter(new HoverViewModel.ModifierToStateMaskConverter());

        context.bindValue(WidgetProperties.text().observe(fSummaryHoverNameLabel),
                HoverViewModel.hoverLabel.observeDetail(selectedHover));

        context.bindValue(WidgetProperties.text().observe(fSummaryHoverDescriptionLabel),
                HoverViewModel.hoverDescription.observeDetail(selectedHover));

        context.bindValue(WidgetProperties.text().observe(fSummaryHoverModifierStringText),
                HoverViewModel.hoverStateMask.observeDetail(selectedHover), modifierToStatusMaskStrategy,
                statusMaskToModifierStrategy);

        KeyListener stateMaskKeyListener = new KeyListener() {
            public void keyPressed(KeyEvent e) {

                boolean isPressedKeyModifier = e.keyCode > 0 && e.character == 0
                        && (e.keyCode & SWT.MODIFIER_MASK) != 0;
                boolean isStateKeyModifier = e.stateMask > 0 && (e.stateMask & SWT.MODIFIER_MASK) != 0;

                int mask = SWT.NONE;
                if (isPressedKeyModifier) {
                    mask = e.keyCode;

                    if (isStateKeyModifier) {
                        mask |= e.stateMask;
                    }

                    stateMaskOfSelected.setValue(mask);
                    stateMaskToModifierBinding.updateModelToTarget();
                }
            }

            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.BS || e.keyCode == SWT.DEL) {
                    stateMaskOfSelected.setValue(SWT.NONE);
                    stateMaskToModifierBinding.updateModelToTarget();
                }
            }
        };

        fHoverModifierStringTextCellEditor.getControl().addKeyListener(stateMaskKeyListener);
        fSummaryHoverModifierStringText.addKeyListener(stateMaskKeyListener);

        return context;
    }

    public boolean performOk() {
        fModel.persistHoverDescriptors(fViewModel.hoverObservableList);
        
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
