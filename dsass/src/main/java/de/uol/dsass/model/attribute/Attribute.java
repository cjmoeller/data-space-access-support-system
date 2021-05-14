package de.uol.dsass.model.attribute;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;

import java.util.List;

/**
 * Model class for an Attribute
 *
 * @param <T> The type of the Attribute
 */
public abstract class Attribute<T> {
    /**
     * The graphQL model of the attribute (from schema)
     */
    private FieldDefinition gqlAttribute;
    /**
     * The object type definition from gql.
     */
    private ObjectTypeDefinition parentType;

    public Attribute(FieldDefinition gqlAttribute, ObjectTypeDefinition parentType) {
        this.gqlAttribute = gqlAttribute;
        this.parentType = parentType;
    }

    public FieldDefinition getGqlAttribute() {
        return gqlAttribute;
    }

    public void setGqlAttribute(FieldDefinition gqlAttribute) {
        this.gqlAttribute = gqlAttribute;
    }

    public ObjectTypeDefinition getParentType() {
        return parentType;
    }

    public void setParentType(ObjectTypeDefinition parentType) {
        this.parentType = parentType;
    }

    public String getName() {
        return gqlAttribute.getName();
    }

    public abstract void setSamples(List<T> samples);

    public abstract List<T> getSamples();

}
