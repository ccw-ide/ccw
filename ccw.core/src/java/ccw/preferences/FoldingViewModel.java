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

import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.property.value.IValueProperty;

import ccw.editors.clojure.folding.FoldingDescriptor;
import ccw.editors.clojure.folding.FoldingModel;

/**
 * The ViewModel of the Hover Preference page.
 * 
 * @author Andrea Richiardi
 *
 */
@Singleton
public final class FoldingViewModel {

    // Observables
    public final IObservableSet checkedSet;
    public final IObservableList observableList;

    // Properties
    public final static IValueProperty labelProperty = BeanProperties.value(FoldingDescriptor.class, "label");
    public final static IValueProperty enabledProperty = BeanProperties.value(FoldingDescriptor.class, "enabled");
    public final static IValueProperty descriptionProperty = BeanProperties.value(FoldingDescriptor.class, "description");
    public final static IValueProperty[] displayDomain = new IValueProperty[] { labelProperty, descriptionProperty };

    // Validators
    // None

    @SuppressWarnings("unchecked")
    FoldingViewModel(FoldingModel model) {

        observableList = model.getObservableDescriptors();
        observableList.addListChangeListener(new IListChangeListener() {
            @Override
            public void handleListChange(ListChangeEvent event) {
                updateCheckedSet((List<FoldingDescriptor>) event.getObservableList());
            }
        });

        checkedSet = new WritableSet();
        updateCheckedSet((List<FoldingDescriptor>) observableList);
        
        checkedSet.addSetChangeListener(new ISetChangeListener() {
            private SetDiff fDiff;

            @Override
            public void handleSetChange(SetChangeEvent event) {
                fDiff = event.diff;

                for (FoldingDescriptor hd : (Set<FoldingDescriptor>) fDiff.getAdditions()) {
                    hd.setEnabled(true);
                }

                for (FoldingDescriptor hd : (Set<FoldingDescriptor>) fDiff.getRemovals()) {
                    hd.setEnabled(false);
                }
            }
        });
    }

    private void updateCheckedSet(List<FoldingDescriptor> descriptors) {
        checkedSet.clear();
        
        for (FoldingDescriptor hd : descriptors) {
            if (hd.isEnabled() == Boolean.TRUE) {
                checkedSet.add(hd);
            }
        }
    }
}
