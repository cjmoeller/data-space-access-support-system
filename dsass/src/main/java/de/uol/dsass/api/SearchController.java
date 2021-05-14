package de.uol.dsass.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.uol.dsass.DataSourceRepository;
import de.uol.dsass.gql.GraphQLQueryHelper;
import de.uol.dsass.model.DataSource;
import de.uol.dsass.model.attribute.Attribute;
import de.uol.dsass.model.query.Query;
import de.uol.dsass.model.vocabulary.Vocabulary;
import de.uol.dsass.model.vocabulary.VocabularyItem;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements the API for the DSASS search functionalities.
 */
@RestController
public class SearchController {
    /** Available data sources. */
    @Autowired
    private DataSourceRepository sources;
    /** The data space vocabulary. */
    @Autowired
    private Vocabulary vocabulary;

    /**
     * Search functionality for retrieving data sources by keywords
     * @param keywords the keywords
     * @return a list of data sources as the search result.
     */
    @GetMapping("/searchByKeywords")
    public List<String> getDataSourcesByKeywords(@RequestParam(value = "s") String keywords) {
        String[] words = keywords.split(" ");
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
        List<DataSource> searchResults = new ArrayList<>();
        for (String s : words) {
            for (DataSource ds : this.sources.getDataSourceList()) {
                boolean isMatch = ds.getDataInterface().getAttributes().stream().map(a -> a.getName()).
                        filter(a -> jaroWinklerDistance.apply(a, s) > 0.7).findAny().isPresent();
                if (isMatch)
                    searchResults.add(ds);
            }
        }
        return searchResults.stream().map(ds -> ds.getName()).collect(Collectors.toList());
    }

    /**
     * Returns the data space vocabulary (user-defined vocabularies)
     * @return
     */
    @GetMapping("/getVocabulary")
    public List<String> getVocabulary() {
        return vocabulary.getVocabulary().stream().filter(voc -> !voc.isWeak()).map(v -> v.getName()).collect(Collectors.toList());
    }

    /**
     * Returns the weak vocabulary.
     * @return
     */
    @GetMapping("/getWeakVocabulary")
    public List<String> getWeakVocabulary() {
        return vocabulary.getVocabulary().stream().filter(voc -> voc.isWeak()).map(v -> v.getName()).collect(Collectors.toList());
    }

    /**
     * For a given data source: returns the attributes that can be queried.
     * @param dataSourceName the name of the data source (can be obtained from /searchByKeywords).
     * @return
     */
    @GetMapping("/getQueryableAttributes")
    public List<String> getQueryableAttributes(@RequestParam(value = "dsName") String dataSourceName) {
        DataSource result = this.sources.getDataSourceList().stream().filter(ds -> ds.getName().equals(dataSourceName)).findAny().get();
        if (result == null)
            return new ArrayList<>();
        List<String> attributes = new ArrayList<>();
        for (Query q : result.getDataInterface().getQueryList()) {
            attributes.addAll(q.getQueryableAttributes().stream().map(a -> a.getName()).collect(Collectors.toList()));
        }
        List<String> listWithoutDuplicates = new ArrayList<>(new HashSet<>(attributes));
        return listWithoutDuplicates;
    }

    /**
     * For a given data source: returns a list of (weak) vocabularies that are associated with attributes of that data source
     * @param dataSourceName
     * @return
     */
    @GetMapping("/getKnownAttributes")
    public List<String> getKnownAttributes(@RequestParam(value = "dsName") String dataSourceName) {
        Optional<DataSource> resultOptional = this.sources.getDataSourceList().stream().filter(ds -> ds.getName().equals(dataSourceName)).findAny();
        if (!resultOptional.isPresent())
            return new ArrayList<>();
        DataSource result = resultOptional.get();
        List<Attribute> attributes = new ArrayList<>();
        List<String> resultList = new ArrayList<>();
        for (Query q : result.getDataInterface().getQueryList()) {
            attributes.addAll(q.getQueryableAttributes().stream().collect(Collectors.toList()));
        }
        for (Attribute a : attributes) {
            List<VocabularyItem> itemList = this.vocabulary.findVocabulary(a);
            if (!itemList.isEmpty()) {
                for (VocabularyItem item : itemList) {
                    String vocItem = "";
                    if (item.isWeak())
                        vocItem = a.getName() + " match with " + item.getName() + " is weak";
                    else
                        vocItem = a.getName() + " match with " + item.getName() + " - " + item.getDescription();
                    resultList.add(vocItem);
                }
            }
        }
        List<String> listWithoutDuplicates = new ArrayList<>(new HashSet<>(resultList));
        return listWithoutDuplicates;
    }

    /**
     * Checks the availability of data for a given vocabulary and a numerical range in the data space.
     * @param vocabulary the given vocabulary
     * @param lowerLimit the lower numerical limit
     * @param higherLimit the upper numerical limit
     * @return A list of data sources that contain data for the vocabulary in the specified range.
     * @throws IOException
     */
    @GetMapping("/checkRangeAvailability")
    public List<String> checkRangeAvailability(@RequestParam(value = "v") String vocabulary, @RequestParam(value = "l") Float lowerLimit, @RequestParam(value = "h") Float higherLimit) throws IOException {
        Optional<VocabularyItem> itemOptional = this.vocabulary.getVocabulary().stream().filter(v -> v.getName().equals(vocabulary)).findFirst();
        if (!itemOptional.isPresent())
            return Collections.emptyList();
        List<String> availableDataSources = new ArrayList<>();
        VocabularyItem item = itemOptional.get();
        List<Attribute> attributes = item.getInstances();
        for (Attribute a : attributes) {
            for (DataSource ds : this.sources.getDataSourceList()) {
                for (Query q : ds.getDataInterface().getQueryList()) {
                    if (q.getQueryableAttributes().contains(a)) {
                        //found a query that supports this attribute, now query:
                        GraphQLQueryHelper helper = new GraphQLQueryHelper(ds.getHostURL());
                        String result = helper.postGQLQuery(helper.generateGQLRangeQuery(q, a, lowerLimit, higherLimit));
                        Gson gson = new Gson();
                        JsonObject obj = gson.fromJson(result, JsonObject.class);
                        JsonObject data = obj.get("data").getAsJsonObject();
                        JsonArray arr = data.getAsJsonArray(data.keySet().stream().findFirst().get()).getAsJsonArray();
                        if (arr.size() > 0 && !availableDataSources.stream().filter(s -> s.equals(ds.getName())).findAny().isPresent()) {
                            availableDataSources.add(ds.getName());
                        }
                        System.out.println(result);
                    }
                }
            }
        }
        return availableDataSources;
    }

    /**
     * Assigns an attribute of a data source as an instance to an existing (weak) vocabulary.
     * @param vocabulary
     * @param dataSourceName
     * @param attributeName
     */
    @GetMapping("/assignInstance")
    public void assignInstance(@RequestParam(value = "v") String vocabulary, @RequestParam(value = "ds") String dataSourceName, @RequestParam(value = "a") String attributeName) {
        Optional<VocabularyItem> optVocab = this.vocabulary.getVocabulary().stream().filter(v -> v.getName().equals(vocabulary)).findFirst();
        Optional<DataSource> sourceResult = this.sources.getDataSourceList().stream().filter(ds -> ds.getName().equals(dataSourceName)).findAny();
        if (!optVocab.isPresent() || !sourceResult.isPresent())
            return;
        Optional<Attribute> optAttr = sourceResult.get().getDataInterface().getAttributes().stream().filter(a -> a.getName().equals(attributeName)).findAny();
        if (!optAttr.isPresent())
            return;
        optVocab.get().addInstance(optAttr.get());
    }

}
