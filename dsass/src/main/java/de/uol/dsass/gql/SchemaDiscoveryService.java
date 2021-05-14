package de.uol.dsass.gql;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import graphql.language.ObjectTypeDefinition;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This service uses GraphQL's introspection functionality to analyse the schema of a data source.
 */
public class SchemaDiscoveryService {

    private GraphQLQueryHelper helper;

    /**
     * Constructor
     *
     * @param hostURL url of the data source
     */
    public SchemaDiscoveryService(String hostURL) {
        this.helper = new GraphQLQueryHelper(hostURL);
    }

    /**
     * Returns the result of the introspection.
     *
     * @return
     * @throws IOException
     */
    public List<ObjectTypeDefinition> startIntrospection() throws IOException {
        String result = helper.postGQLQuery(introspectionQuery);
        Gson gson = new Gson();
        Type empMapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> resultMap = gson.fromJson(result, empMapType);
        Map<String, Object> schemaMap = (Map<String, Object>) resultMap.get("data");
        IntrospectionResultToSchema rts = new IntrospectionResultToSchema();
        Document resultDoc = rts.createSchemaDefinition(schemaMap);
        List<ObjectTypeDefinition> schemaDef = resultDoc.getDefinitions().stream()
                .filter(d -> d instanceof ObjectTypeDefinition)
                .map(ObjectTypeDefinition.class::cast)
                .collect(Collectors.toList());
        return schemaDef;
    }

    /**
     * Introspection query.
     */
    private final String introspectionQuery = "query IntrospectionQuery {\n" +
            "  __schema {\n" +
            "    queryType {\n" +
            "      name\n" +
            "    }\n" +
            "    mutationType {\n" +
            "      name\n" +
            "    }\n" +
            "    subscriptionType {\n" +
            "      name\n" +
            "    }\n" +
            "    types {\n" +
            "      ...FullType\n" +
            "    }\n" +
            "    directives {\n" +
            "      name\n" +
            "      description\n" +
            "      locations\n" +
            "      args {\n" +
            "        ...InputValue\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "fragment FullType on __Type {\n" +
            "  kind\n" +
            "  name\n" +
            "  description\n" +
            "  fields(includeDeprecated: true) {\n" +
            "    name\n" +
            "    description\n" +
            "    args {\n" +
            "      ...InputValue\n" +
            "    }\n" +
            "    type {\n" +
            "      ...TypeRef\n" +
            "    }\n" +
            "    isDeprecated\n" +
            "    deprecationReason\n" +
            "  }\n" +
            "  inputFields {\n" +
            "    ...InputValue\n" +
            "  }\n" +
            "  interfaces {\n" +
            "    ...TypeRef\n" +
            "  }\n" +
            "  enumValues(includeDeprecated: true) {\n" +
            "    name\n" +
            "    description\n" +
            "    isDeprecated\n" +
            "    deprecationReason\n" +
            "  }\n" +
            "  possibleTypes {\n" +
            "    ...TypeRef\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "fragment InputValue on __InputValue {\n" +
            "  name\n" +
            "  description\n" +
            "  type {\n" +
            "    ...TypeRef\n" +
            "  }\n" +
            "  defaultValue\n" +
            "}\n" +
            "\n" +
            "fragment TypeRef on __Type {\n" +
            "  kind\n" +
            "  name\n" +
            "  ofType {\n" +
            "    kind\n" +
            "    name\n" +
            "    ofType {\n" +
            "      kind\n" +
            "      name\n" +
            "      ofType {\n" +
            "        kind\n" +
            "        name\n" +
            "        ofType {\n" +
            "          kind\n" +
            "          name\n" +
            "          ofType {\n" +
            "            kind\n" +
            "            name\n" +
            "            ofType {\n" +
            "              kind\n" +
            "              name\n" +
            "              ofType {\n" +
            "                kind\n" +
            "                name\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}