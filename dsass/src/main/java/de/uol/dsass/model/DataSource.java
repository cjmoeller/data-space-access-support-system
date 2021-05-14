package de.uol.dsass.model;

import de.uol.dsass.gql.AttributeDiscoveryService;
import de.uol.dsass.gql.SchemaDiscoveryService;
import graphql.language.ObjectTypeDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a (gql-)data source in the data space.
 */
public class DataSource {
    /** The url of the data source*/
    private String hostURL;
    /** A representation of the GraphQL schema of the data source */
    private List<ObjectTypeDefinition> graphQLSchema;
    /** The data interface of the data source */
    private DataInterface dataInterface;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public DataSource(String hostURL, String name) {
        this.hostURL = hostURL;
        this.graphQLSchema = new ArrayList<>();
        this.name = name;
    }

    public DataInterface getDataInterface() {
        return dataInterface;
    }

    public void setDataInterface(DataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public String getHostURL() {
        return hostURL;
    }

    public void setHostURL(String hostURL) {
        this.hostURL = hostURL;
    }

    public List<ObjectTypeDefinition> getGraphQLSchema() {
        return graphQLSchema;
    }

    public void setGraphQLSchema(List<ObjectTypeDefinition> graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    /**
     * Executes the discovery services on the data source.
     * @throws IOException
     */
    public void discover() throws IOException {
        SchemaDiscoveryService service = new SchemaDiscoveryService(hostURL);
        this.graphQLSchema = service.startIntrospection();
        AttributeDiscoveryService ads = new AttributeDiscoveryService(this.graphQLSchema);
        DataSource result = ads.discoverAttributesAndQueries(this);
        this.setDataInterface(result.dataInterface);
    }
}
