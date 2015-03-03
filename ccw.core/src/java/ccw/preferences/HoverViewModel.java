package ccw.preferences;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;

import ccw.editors.clojure.hovers.HoverDescriptor;
import ccw.editors.clojure.hovers.HoverModel;
import ccw.util.UiUtils;

/**
 * The ViewModel of the Hover Preference page.
 * 
 * @author Andrea Richiardi
 *
 */
@Singleton
public final class HoverViewModel {

    // Observables
    public final IObservableSet checkedSet;
    public final IObservableList hoverObservableList;

    // Properties
    public final static IValueProperty hoverModifierString = BeanProperties.value(HoverDescriptor.class, "modifierString");
    public final static IValueProperty hoverStateMask = BeanProperties.value(HoverDescriptor.class, "stateMask");
    public final static IValueProperty hoverLabel = BeanProperties.value(HoverDescriptor.class, "label");
    public final static IValueProperty hoverEnabled = BeanProperties.value(HoverDescriptor.class, "enabled");
    public final static IValueProperty hoverDescription = BeanProperties.value(HoverDescriptor.class, "description");
    public final static IValueProperty[] hoverDisplayDomain = new IValueProperty[] { hoverLabel, hoverModifierString, hoverDescription };

    // Validators
    public final StateMaskValidator stateMaskValidator;

    @SuppressWarnings("unchecked")
    HoverViewModel(HoverModel hoverModel) {

        hoverObservableList = hoverModel.observableHoverDescriptors();
        hoverObservableList.addListChangeListener(new IListChangeListener() {
            @Override
            public void handleListChange(ListChangeEvent event) {
                updateCheckedSet((List<HoverDescriptor>) event.getObservableList());
            }
        });
        
        checkedSet = new WritableSet();
        updateCheckedSet((List<HoverDescriptor>) hoverObservableList);
        
        checkedSet.addSetChangeListener(new ISetChangeListener() {
            private SetDiff fDiff;

            @Override
            public void handleSetChange(SetChangeEvent event) {
                fDiff = event.diff;

                for (HoverDescriptor hd : (Set<HoverDescriptor>) fDiff.getAdditions()) {
                    hd.setEnabled(true);
                }

                for (HoverDescriptor hd : (Set<HoverDescriptor>) fDiff.getRemovals()) {
                    hd.setEnabled(false);
                }
            }
        });

        stateMaskValidator = new StateMaskValidator();
    }

    private void updateCheckedSet(List<HoverDescriptor> descriptors) {
        checkedSet.clear();
        
        for (HoverDescriptor hd : descriptors) {
            if (hd.isEnabled() == Boolean.TRUE) {
                checkedSet.add(hd);
            }
        }
    }
    
    public static class ModifierToStateMaskConverter extends Converter {

        public ModifierToStateMaskConverter() {
            super(String.class, Integer.class);
        }

        @Override
        public Object convert(Object object) {
            Integer stateMask = SWT.DEFAULT;
            if (object instanceof String) {
                String s = (String) object;
                stateMask = s != null ? UiUtils.computeStateMask(s) : SWT.NONE;
            }
            return stateMask;
        }
    }

    public static class StateMaskToModifierConverter extends Converter {

        public StateMaskToModifierConverter() {
            super(Integer.class, String.class);
        }

        @Override
        public Object convert(Object object) {
            String modifierString = null;
            if (object instanceof Integer) {
                Integer i = (Integer) object;
                modifierString = UiUtils.getModifierString(i);
            }
            return modifierString != null ? modifierString : "";
        }
    }

    class ModifierStringValidator implements IValidator {
        @Override
        public IStatus validate(Object modifiers) {
            if (modifiers instanceof String) {
                int stateMask = UiUtils.computeStateMask((String) modifiers);
                if (!isStateMaskUnique(stateMask)) {
                    return stateMaskError(stateMask);
                }
            }
            return ValidationStatus.ok();
        }
    }

    class StateMaskValidator implements IValidator {
        @Override
        public IStatus validate(Object stateMask) {
            if (stateMask instanceof Integer) {
                int sm = (Integer) stateMask;
                if (!isStateMaskUnique(sm)) {
                    return stateMaskError(sm);
                }
            }
            return ValidationStatus.ok();
        }
    }

    public boolean isStateMaskUnique(int stateMask) {
        Iterator<HoverDescriptor> it = checkedSet.iterator();
        int count = 0;
        while (it.hasNext()) {
            HoverDescriptor next = it.next();
            if (next.getStateMask().equals(stateMask) && count++ > 0) {
                return false;
            }
        }
        return true;
    }

    public IStatus stateMaskError(int stateMask) {
        if (stateMask == SWT.NONE || stateMask == SWT.DEFAULT) {
            return ValidationStatus.error(Messages.HoverPreferencePage_errorDefaultHoverNotUnique);
        } else {
            String modifiers = UiUtils.getModifierString(stateMask);
            if (modifiers != null && !modifiers.isEmpty()) {
                return ValidationStatus.error(Messages.bind(Messages.HoverPreferencePage_errorStatusMaskAlreadyInUse,
                        modifiers));
            } else {
                return ValidationStatus.error(Messages.HoverPreferencePage_errorDefaultHoverNotUnique);
            }
        }
    }
}
