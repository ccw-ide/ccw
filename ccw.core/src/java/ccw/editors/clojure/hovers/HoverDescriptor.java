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
package ccw.editors.clojure.hovers;

import ccw.util.AbstractModelObject;

/**
 * Data structure to hold a (mutable) hover descriptor.
 * @author Andrea Richiardi
 */
public class HoverDescriptor extends AbstractModelObject {

    private String id;
    private String label;
    private Boolean enabled;
    private String description;
    private Integer stateMask;
    private String modifierString;

    public HoverDescriptor(String id, String label, Boolean enabled, String description,
            Integer stateMask, String modifierString) {
        super();
        this.id = id;
        this.description = description;
        this.label = label;
        this.enabled = enabled;
        this.stateMask = stateMask;
        this.modifierString = modifierString;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public Integer getStateMask() {
        return stateMask;
    }

    public String getModifierString() {
        return modifierString;
    }

    public void setId(String id) {
        firePropertyChange("id", this.id, this.id = id);
    }

    public void setDescription(String description) {
        firePropertyChange("description", this.description, this.description = description);
    }

    public void setLabel(String label) {
        firePropertyChange("label", this.label, this.label = label);
    }

    public void setStateMask(Integer stateMask) {
        firePropertyChange("stateMask", this.stateMask, this.stateMask = stateMask);
    }

    public void setModifierString(String modifierString) {
        firePropertyChange("modifierString", this.modifierString, this.modifierString = modifierString);
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        firePropertyChange("enabled", this.enabled, this.enabled = enabled);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HoverDescriptor other = (HoverDescriptor) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", label: " + label + ", enabled: " + enabled + 
                ", stateMask: " + stateMask + ", modifierString: " + modifierString + ", description: " + description + "}";
    }
}