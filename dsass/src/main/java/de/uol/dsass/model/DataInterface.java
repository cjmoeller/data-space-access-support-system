package de.uol.dsass.model;

import de.uol.dsass.model.attribute.Attribute;
import de.uol.dsass.model.query.Query;

import java.util.List;

/**
 * This class represents a data interface, which belongs to a specific data source.
 */
public class DataInterface {
    /**
     * The list of available attributes in the interface
     */
    private List<Attribute> attributes;
    /**
     * The list of available queries in the interface.
     */
    private List<Query> queryList;

    public DataInterface(List<Attribute> attributes, List<Query> queryList) {
        this.attributes = attributes;
        this.queryList = queryList;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Query> getQueryList() {
        return queryList;
    }

    public void setQueryList(List<Query> queryList) {
        this.queryList = queryList;
    }
}
