package de.uol.dsass.gql;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.uol.dsass.model.attribute.Attribute;
import de.uol.dsass.model.query.Query;
import okhttp3.*;

import java.io.IOException;

/**
 * This helper class generates and executes GraphQL-queries that are used to automatically retrieve data from a data source.
 */
public class GraphQLQueryHelper {

    private static MediaType JSON;
    private OkHttpClient client;
    private String hostURL;

    /**
     * Constructor
     * @param hostURL url of the data source
     */
    public GraphQLQueryHelper(String hostURL) {
        this.hostURL = hostURL;
        this.client = new OkHttpClient();
        JSON = MediaType.parse("application/json; charset=utf-8");
    }

    /**
     * Container function to post a custom gql-Query
     * @param query the query
     * @return the query result json as string
     * @throws IOException
     */
    public String postGQLQuery(String query) throws IOException {
        query = "{\"query\":\"" + query + "\",\"variables\":null}";
        RequestBody body = RequestBody.create(JSON, query); // new
        // RequestBody body = RequestBody.create(JSON, json); // old
        Request request = new Request.Builder()
                .url(this.hostURL)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * Generates a gql-Query from a query object and selects a specific attribute
     * @param q - the query object
     * @param a - the attribute to select
     * @return
     */
    public String generateGQLQuery(Query q, Attribute a) {
        String query = "{ \n";
        query += q.getName() + "{ \n";
        query += a.getGqlAttribute().getName() + "\n";
        query += "} \n }";
        return query;
    }

    /**
     * Generates a gql range query.
     * @param q the query object
     * @param a the attribute to select
     * @param lower the lower limit of the range
     * @param upper the upper limit of the range
     * @return
     */
    public String generateGQLRangeQuery(Query q, Attribute a, Float lower, Float upper) {
        JsonObject inner = new JsonObject();
        inner.addProperty("min", lower);
        inner.addProperty("max", upper);
        JsonObject outer = new JsonObject();
        outer.add(a.getName(), inner);

        Gson gson = new Gson();
        String filter = gson.toJson(outer);
        //escaping:
        filter = filter.replace("\"", "\\\\\\\"");
        String query = "{ \n";
        query += q.getName() + "(filter: \\\"" + filter + "\\\")" + "{ \n";
        query += a.getGqlAttribute().getName() + "\n";
        query += "} \n }";
        return query;
    }

    /**
     * Query for the data source to get its available filter methods.
     * @return
     */
    public String getAvailableFilterMethodsQuery() {
        return "{getAvailableFilterMethods}";
    }

}
