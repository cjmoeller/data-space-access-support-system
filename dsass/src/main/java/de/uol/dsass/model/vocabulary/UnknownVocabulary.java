package de.uol.dsass.model.vocabulary;

import de.uol.dsass.model.attribute.Attribute;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the list of candidate "vocabularies", i.e. attributes that could not be matched to an existing vocabulary
 * in the weak vocabulary list. This list will be checked for matches any time another attribute could not be matched.
 */
@Component
public class UnknownVocabulary {

    private List<VocabularyItem> vocabulary;

    public UnknownVocabulary() {
        vocabulary = new ArrayList<>();
    }

    /**
     * Adds an attribute the the candidate list of 'unknown' vocabularies.
     * @param attr the attribute
     * @param name the name of the candidate vocabulary
     * @param description the description of the candidate vocabulary
     * @param samples samples of the candidate vocabulary
     */
    public void addVocabularyItem(Attribute attr, String name, String description, List samples) {
        VocabularyItem newItem = new VocabularyItem();
        newItem.setDescription(description);
        newItem.setName(name);
        newItem.addInstance(attr);
        newItem.setSamples(samples);
        this.vocabulary.add(newItem);
    }

    public void removeVocabularyItem(int index) {
        vocabulary.remove(index);
    }

    public List<VocabularyItem> getVocabulary() {
        return vocabulary;
    }

}
