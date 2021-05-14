package de.uol.dsass.model.query;

import de.uol.dsass.model.attribute.Attribute;

/**
 * Represents a filter method for a specific attribute in a Query
 * @param <T>
 */
public class Filter<T> {
    /** The attribute which the filter applies to */
    private Attribute<T> attribute;
    /** The query which supports this filter */
    private Query query;
    /** The filter type. */
    private AvailableMethods method;
    /** The value for filtering. */
    private T value;

    public Filter(Attribute<T> attribute, Query query, AvailableMethods method, T value) {
        this.attribute = attribute;
        this.query = query;
        this.method = method;
        this.value = value;
    }

    public Attribute<T> getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute<T> attribute) {
        this.attribute = attribute;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public AvailableMethods getMethod() {
        return method;
    }

    public void setMethod(AvailableMethods method) {
        this.method = method;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
