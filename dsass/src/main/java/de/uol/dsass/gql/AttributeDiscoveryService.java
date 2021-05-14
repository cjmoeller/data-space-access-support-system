package de.uol.dsass.gql;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.uol.dsass.model.*;
import de.uol.dsass.model.attribute.*;
import de.uol.dsass.model.query.AvailableMethods;
import de.uol.dsass.model.query.Query;
import de.uol.dsass.model.query.QueryArgument;
import graphql.language.*;

import java.io.IOException;
import java.util.*;

/**
 * This class implements the GraphQL Attribute discovery service. This is used to analyse the attributes of a data source.
 * This involves a lot of manual JsonObject navigation...
 */
public class AttributeDiscoveryService {
    private final List<ObjectTypeDefinition> schema;

    /**
     * Constructor
     *
     * @param schema GraphQL schema representation
     */
    public AttributeDiscoveryService(List<ObjectTypeDefinition> schema) {
        this.schema = schema;
    }

    /**
     * This is the main discovery function that analyses the attributes of a data source.
     * @param dataSource the data source object from the schema repository
     * @return the data source object with enriched information on the attributes and available queries
     * @throws IOException
     */
    public DataSource discoverAttributesAndQueries(DataSource dataSource) throws IOException {
        List<Attribute> attributes = new ArrayList<>();
        List<Query> queries = new ArrayList<>();
        //first check for availables queries and types in the schema:
        for (ObjectTypeDefinition otd : schema) {
            if (!otd.getName().equals("Query")) {
                for (FieldDefinition fd : otd.getFieldDefinitions()) {
                    Attribute newAttribute = AttributeFactory.createAttributeFromGQL(fd, otd);
                    attributes.add(newAttribute);
                }
            } else {

                for (FieldDefinition fd : otd.getFieldDefinitions()) {
                    Type type = fd.getType();
                    if (type.getNamedChildren().getChildren().get("type") != null) { //Only works for non-primitive types
                        TypeName returnType = (TypeName) type.getNamedChildren().getChildren().get("type").get(0);
                        Query query = new Query(fd.getName(), returnType.getName());
                        queries.add(query);
                    }
                }
            }
        }
        //add queryable attributes to query
        for (Attribute a : attributes) {
            queries.stream().filter(query -> query.getType().equals(a.getParentType().getName()))
                    .forEach(query -> query.getQueryableAttributes().add(a));
        }

        //obtain some sample data for each query (will later be used by the vocabulary matcher):
        GraphQLQueryHelper helper = new GraphQLQueryHelper(dataSource.getHostURL());
        for (Query q : queries) {
            for (Attribute a : q.getQueryableAttributes()) {
                String queryGQL = helper.generateGQLQuery(q, a);
                String result = helper.postGQLQuery(queryGQL);
                JsonObject convertedObject = new Gson().fromJson(result, JsonObject.class);
                JsonArray dataArr = convertedObject.get("data").getAsJsonObject().get(q.getName()).getAsJsonArray();
                if (a instanceof IntegerAttribute) {
                    List<Integer> samples = new ArrayList<>();
                    for (JsonElement element : dataArr) {
                        JsonElement value = element.getAsJsonObject().get(a.getName());
                        if (!value.isJsonNull())
                            samples.add(element.getAsJsonObject().get(a.getName()).getAsInt());
                    }
                    a.setSamples(samples);
                } else if (a instanceof FloatAttribute) {
                    List<Float> samples = new ArrayList<>();
                    for (JsonElement element : dataArr) {
                        JsonElement value = element.getAsJsonObject().get(a.getName());
                        if (!value.isJsonNull())
                            samples.add(element.getAsJsonObject().get(a.getName()).getAsFloat());
                    }
                    a.setSamples(samples);
                } else if (a instanceof StringAttribute) {
                    List<String> samples = new ArrayList<>();
                    for (JsonElement element : dataArr) {
                        JsonElement value = element.getAsJsonObject().get(a.getName());
                        if (!value.isJsonNull())
                            samples.add(element.getAsJsonObject().get(a.getName()).getAsString());
                    }
                    a.setSamples(samples);
                }
            }
        }
        //check which filters are supported by the data source.
        //We are specifically interested in range queries - others can be implemented here in an analog fashion
        String filterMethods = helper.postGQLQuery(helper.getAvailableFilterMethodsQuery());
        Gson gson = new Gson();
        String filterString = gson.fromJson(filterMethods, JsonObject.class).get("data").getAsJsonObject().get("getAvailableFilterMethods").getAsString();
        JsonObject filters = gson.fromJson(filterString, JsonObject.class);
        for (Query q : queries) {
            if (filters.has(q.getName())) {
                JsonObject attributesObject = filters.get(q.getName()).getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entrySet = attributesObject.entrySet();
                List<String> attributeNames = new ArrayList<>();
                for (Map.Entry<String, JsonElement> el : entrySet) {
                    attributeNames.add(el.getKey());
                }
                for (String attributeName : attributeNames) {
                    Optional<Attribute> matchingAttr = attributes.stream().filter(attribute -> attribute.getName().equals(attributeName)).findFirst();
                    if (matchingAttr.isPresent()) {
                        JsonArray supportedMethods = entrySet.stream().filter(p -> p.getKey().equals(attributeName)).findFirst().get().getValue().getAsJsonArray();
                        List<AvailableMethods> methods = new ArrayList<>();
                        for (JsonElement el2 : supportedMethods) {
                            methods.add(AvailableMethods.valueOf(el2.getAsString()));
                        }
                        QueryArgument newArgument = new QueryArgument(methods, matchingAttr.get());
                        q.getArguments().add(newArgument);
                    }
                }

            }
        }
        //Add the gathered information to the data source:
        DataInterface di = new DataInterface(attributes, queries);
        dataSource.setDataInterface(di);
        return dataSource;
    }


}
