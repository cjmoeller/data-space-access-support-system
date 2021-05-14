package de.uol.dsass.model.attribute;

import graphql.language.FieldDefinition;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;

/**
 * Factory to create attributes from gql-instances
 */
public class AttributeFactory {
    /**
     * Creates an attribute from a gql-model representation
     *
     * @param gqlAttribute the gql attribute
     * @param parentType   the object type definition
     * @return the attribute
     */
    public static Attribute createAttributeFromGQL(FieldDefinition gqlAttribute, ObjectTypeDefinition parentType) {
        if (gqlAttribute.getType() instanceof NonNullType) {
            return new UnknownAttribute(gqlAttribute, parentType);
        }
        TypeName type = (TypeName) gqlAttribute.getType();
        Attribute newAttribute = null;
        switch (type.getName()) {
            case "Float":
                newAttribute = new FloatAttribute(gqlAttribute, parentType);
                break;
            case "Int":
                newAttribute = new IntegerAttribute(gqlAttribute, parentType);
                break;
            case "String":
                newAttribute = new StringAttribute(gqlAttribute, parentType);
                break;
            default:
                newAttribute = new UnknownAttribute(gqlAttribute, parentType);
        }
        return newAttribute;
    }
}
