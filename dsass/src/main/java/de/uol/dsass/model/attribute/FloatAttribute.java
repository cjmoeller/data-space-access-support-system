package de.uol.dsass.model.attribute;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Float attribute
 */
public class FloatAttribute extends Attribute<Float>{
    /** List of Float samples */
    private List<Float> samples;

    public FloatAttribute(FieldDefinition gqlAttribute, ObjectTypeDefinition parentType) {
        super(gqlAttribute, parentType);
        samples = new ArrayList<>();
    }

    public List<Float> getSamples() {
        return samples;
    }

    public void setSamples(List<Float> samples) {
        this.samples = samples;
    }
}
