package de.uol.dsass.model.vocabulary;

import de.uol.dsass.model.attribute.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class for a single vocabulary (item)
 */
public class VocabularyItem {
    /**
     * The name of the vocabulary
     */
    private String name;
    /**
     * The description of the vocabulary.
     */
    private String description;
    /**
     * A list of instances that all belong this vocabulary item
     */
    private List<Attribute> instances;
    /**
     * A list of samples for this vocabulary
     */
    private List samples;
    /**
     * The reference attribute for this vocabulary (only for non weak vocabularies)
     */
    private Attribute groundTruth;

    public VocabularyItem() {
        this.instances = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Attribute> getInstances() {
        return instances;
    }

    public void addInstance(Attribute instance) {
        this.instances.add(instance);
    }

    public Attribute getGroundTruth() {
        return groundTruth;
    }

    public void setGroundTruth(Attribute groundTruth) {
        this.groundTruth = groundTruth;
    }

    public List<String> getAvailableNames() {
        return this.instances.stream().map(a -> a.getName()).collect(Collectors.toList());
    }

    public void setSamples(List<Attribute> samples) {
        this.samples = samples;
    }

    public List getSamples() {
        return samples;
    }

    public boolean isWeak() {
        return this.groundTruth == null;
    }

}
