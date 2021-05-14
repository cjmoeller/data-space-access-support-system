package de.uol.dsass.model.vocabulary;

import de.uol.dsass.model.attribute.Attribute;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The data space vocabulary, including weak and non-weak vocabularies.
 */
@Component
public class Vocabulary {
    private List<VocabularyItem> vocabulary;

    public Vocabulary() {
        vocabulary = new ArrayList<>();
    }

    /**
     * Adds a new vocabulary (item) to the data space vocabulary
     * @param groundTruth this will be used as the reference instance for the new vocabulary
     * @param name the name of the vocabulary (item)
     * @param description a description of the vocabulary (item)
     * @param samples a list of samples
     */
    public void addVocabularyItem(Attribute groundTruth, String name, String description, List samples) {
        VocabularyItem newItem = new VocabularyItem();
        newItem.setDescription(description);
        newItem.setName(name);
        newItem.setGroundTruth(groundTruth);
        newItem.setSamples(samples);
        this.vocabulary.add(newItem);
    }

    public List<VocabularyItem> getVocabulary() {
        return vocabulary;
    }

    /**
     * Finds the vocabulary that belongs the specified attribute (if existent)
     * @param attr the attribute
     * @return empty list - if no matches found
     */
    public List<VocabularyItem> findVocabulary(Attribute attr) {
        List<VocabularyItem> results = new ArrayList<>();
        for (VocabularyItem vi : this.getVocabulary()) {
            if (vi.getInstances().contains(attr))
                results.add(vi);
        }

        return results;
    }
}
