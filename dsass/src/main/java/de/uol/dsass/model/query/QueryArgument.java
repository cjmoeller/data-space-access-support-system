package de.uol.dsass.model.query;

import de.uol.dsass.model.attribute.Attribute;

import java.util.List;

/**
 * Model class to represent an argument of a query.
 */
public class QueryArgument {
    /** The available filters for the query argument */
    private List<AvailableMethods> methods;
    /** The associated attribute (the argument) */
    private Attribute associatedAttribute;

    public QueryArgument(List<AvailableMethods> methods, Attribute associatedAttribute) {
        this.methods = methods;
        this.associatedAttribute = associatedAttribute;
    }

    public Attribute getAssociatedAttribute() {
        return associatedAttribute;
    }

    public void setAssociatedAttribute(Attribute associatedAttribute) {
        this.associatedAttribute = associatedAttribute;
    }

    public List<AvailableMethods> getMethods() {
        return methods;
    }

    public void setMethods(List<AvailableMethods> methods) {
        this.methods = methods;
    }
}
