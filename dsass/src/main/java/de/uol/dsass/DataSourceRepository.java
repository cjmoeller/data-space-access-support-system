package de.uol.dsass;

import de.uol.dsass.matching.VocabularyMatcher;
import de.uol.dsass.model.DataSource;
import de.uol.dsass.model.vocabulary.UnknownVocabulary;
import de.uol.dsass.model.vocabulary.Vocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSourceRepository {
    private List<DataSource> dataSourceList;

    /**
     * Contains the defined vocabulary and the automatically detected "Weak Vocabulary".
     */
    @Autowired
    private Vocabulary vocabulary;

    /**
     * Attributes that could not be assigned to a vocabulary (or weak vocabulary) are added to the "UnknownVocabulary".
     */
    @Autowired
    private UnknownVocabulary unknownVocabulary;

    /**
     * Establishing the  connection to the 3 data sources which are used for the application.
     * The initial vocabulary is instantiated. Afterwards, each attribute of an data source is tried to be matched
     * to the existing vocabulary. If no entry could be found in the vocabulary, an attempt is made to find or create
     * a Weak Vocabulary. If no matching Weak Vocabulary was found or could be created based on the "unknownVocabulary"
     * list, the attribute is added to the "unknownVocabulary".
     *
     * @throws IOException
     */
    @PostConstruct
    public void init() throws IOException {
        dataSourceList = new ArrayList<>();
        DataSource ds1 = new DataSource("http://localhost:9001/graphql", "NB AIS");
        ds1.discover();
        DataSource ds2 = new DataSource("http://localhost:9002/graphql", "USA AIS");
        ds2.discover();
        DataSource ds3 = new DataSource("http://localhost:9003/graphql", "NB Radar");
        ds3.discover();
        this.dataSourceList.add(ds1);
        this.dataSourceList.add(ds2);
        this.dataSourceList.add(ds3);
        setManuallyDefinedVocabularies();

        //Match and create weak vocabularies
        VocabularyMatcher.matchDataSource(ds1, vocabulary, unknownVocabulary, 0.25);
        VocabularyMatcher.matchDataSource(ds2, vocabulary, unknownVocabulary, 0.25);
        VocabularyMatcher.matchDataSource(ds3, vocabulary, unknownVocabulary, 0.25);
        System.out.println();
    }

    /**
     * defines the initial vocabulary which is used for the application.
     * The names were adopted from the NB AIS: mmsi=mmsi, callSi=CallSign, len=length, wid=width
     */
    private void setManuallyDefinedVocabularies() {
        vocabulary.addVocabularyItem(this.dataSourceList.get(0).getDataInterface().getAttributes().get(0), "mmsi",
                "Maritime Mobile Service Identity", this.dataSourceList.get(0).getDataInterface().getAttributes().get(0).getSamples());
        vocabulary.addVocabularyItem(this.dataSourceList.get(0).getDataInterface().getAttributes().get(12), "callSi",
                "IMO CallSign", this.dataSourceList.get(0).getDataInterface().getAttributes().get(12).getSamples());
        vocabulary.addVocabularyItem(this.dataSourceList.get(0).getDataInterface().getAttributes().get(14), "len",
                "Ship Length", this.dataSourceList.get(0).getDataInterface().getAttributes().get(14).getSamples());
        vocabulary.addVocabularyItem(this.dataSourceList.get(0).getDataInterface().getAttributes().get(15), "wid",
                "Ship Width", this.dataSourceList.get(0).getDataInterface().getAttributes().get(15).getSamples());
    }

    public List<DataSource> getDataSourceList() {
        return dataSourceList;
    }

    public void setDataSourceList(List<DataSource> dataSourceList) {
        this.dataSourceList = dataSourceList;
    }
}
