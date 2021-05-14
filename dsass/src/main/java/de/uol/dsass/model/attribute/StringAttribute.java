package de.uol.dsass.model.attribute;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;

import java.util.List;

/**
 * Represents a string attribute.
 */
public class StringAttribute extends Attribute<String> {
    /** List of string attributes. */
    private List<String> samples;

    public StringAttribute(FieldDefinition gqlAttribute, ObjectTypeDefinition parentType) {
        super(gqlAttribute, parentType);
    }

    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }
}
