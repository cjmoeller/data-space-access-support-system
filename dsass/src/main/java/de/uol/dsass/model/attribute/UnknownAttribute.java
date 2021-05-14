package de.uol.dsass.model.attribute;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;

import java.util.List;

/**
 * Represents an attribute with an Unknown type.
 */
public class UnknownAttribute extends Attribute {
    public UnknownAttribute(FieldDefinition gqlAttribute, ObjectTypeDefinition parentType) {
        super(gqlAttribute, parentType);
    }

    @Override
    public void setSamples(List samples) {

    }

    @Override
    public List getSamples() {
        return null;
    }
}
