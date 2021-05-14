package de.uol.dsass.model.attribute;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an integer attribute.
 */
public class IntegerAttribute extends Attribute<Integer> {
    /** List of integer samples. */
    private List<Integer> samples;

    public IntegerAttribute(FieldDefinition gqlAttribute, ObjectTypeDefinition parentType) {
        super(gqlAttribute, parentType);
        samples = new ArrayList<>();
    }

    public List<Integer> getSamples() {
        return samples;
    }

    public void setSamples(List<Integer> samples) {
        this.samples = samples;
    }
}
