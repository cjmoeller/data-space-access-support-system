package de.uol.dsass.model.query;

import de.uol.dsass.model.attribute.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class to represent a Query.
 */
public class Query {
    /** The attributes that can be selected */
    private List<Attribute> queryableAttributes;
    /** The name of the query. */
    private String name;
    /** The return type of the query. */
    private String type;
    /** The arguments of the query. */
    private List<QueryArgument> arguments;

    public List<Attribute> getQueryableAttributes() {
        return queryableAttributes;
    }

    public List<QueryArgument> getArguments() {
        return arguments;
    }

    public void setArguments(List<QueryArgument> arguments) {
        this.arguments = arguments;
    }

    public void setQueryableAttributes(List<Attribute> queryableAttributes) {
        this.queryableAttributes = queryableAttributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Query(String name, String type) {
        this.name = name;
        this.type = type;
        this.queryableAttributes = new ArrayList<>();
        this.arguments = new ArrayList<>();
    }
}
