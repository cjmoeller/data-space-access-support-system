package de.uol.dsass.matching;

import de.uol.dsass.model.DataSource;
import de.uol.dsass.model.attribute.*;
import de.uol.dsass.model.vocabulary.Vocabulary;
import de.uol.dsass.model.vocabulary.UnknownVocabulary;
import de.uol.dsass.model.vocabulary.VocabularyItem;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.util.List;

public class VocabularyMatcher {
    /**
     * Matches a datasource with a predefined vocabulary
     * @param ds                    the data source
     * @param voc                   the vocabulary
     * @param unknownVocabulary     temporal list for unknown vocabularies
     * @param threshold             sensitivity of the VocabularyMatcher [0,1], where 0 = very strict and 1 = very loose
     */
    public static void matchDataSource(DataSource ds, Vocabulary voc, UnknownVocabulary unknownVocabulary, double threshold) {
        for (int i = 0; i < ds.getDataInterface().getAttributes().size(); i++) {
            boolean matchedVoc = false;
            for (int j = 0; j < voc.getVocabulary().size(); j++) {
                //for the numerical attributes: prepare the samples
                if (ds.getDataInterface().getAttributes().get(i) instanceof FloatAttribute &&
                        voc.getVocabulary().get(j).getGroundTruth() instanceof FloatAttribute) {
                    DescriptiveStatistics attributeStatistics = new DescriptiveStatistics();
                    DescriptiveStatistics vocStatistics = new DescriptiveStatistics();
                    List<Float> attributeSamples = ds.getDataInterface().getAttributes().get(i).getSamples();
                    List<Float> vocSamples = voc.getVocabulary().get(j).getSamples();
                    for (Float sample : attributeSamples)
                        attributeStatistics.addValue(sample);
                    for (Float sample : vocSamples)
                        vocStatistics.addValue(sample);
                    matchedVoc = isMatchedNumericVoc(ds, voc, threshold, i, matchedVoc, j, attributeStatistics, vocStatistics);
                    //for the numerical attributes: prepare the samples
                } else if (ds.getDataInterface().getAttributes().get(i) instanceof IntegerAttribute &&
                        voc.getVocabulary().get(j).getGroundTruth() instanceof IntegerAttribute) {
                    DescriptiveStatistics attributeStatistics = new DescriptiveStatistics();
                    DescriptiveStatistics vocStatistics = new DescriptiveStatistics();
                    List<Integer> attributeSamples = ds.getDataInterface().getAttributes().get(i).getSamples();
                    List<Integer> vocSamples = voc.getVocabulary().get(j).getSamples();
                    for (Integer sample : attributeSamples)
                        attributeStatistics.addValue(sample);
                    for (Integer sample : vocSamples)
                        vocStatistics.addValue(sample);
                    matchedVoc = isMatchedNumericVoc(ds, voc, threshold, i, matchedVoc, j, attributeStatistics, vocStatistics);
                    //for string attributes: simple name based matching
                } else if (ds.getDataInterface().getAttributes().get(i) instanceof StringAttribute &&
                        voc.getVocabulary().get(j).getGroundTruth() instanceof StringAttribute) {
                    matchedVoc = isMatchedStringVoc(ds, voc, threshold, i, matchedVoc, j);

                } else if (ds.getDataInterface().getAttributes().get(i) instanceof UnknownAttribute &&
                        voc.getVocabulary().get(j).getGroundTruth() instanceof UnknownAttribute) {
                }
            }
            matchWeakVoc(ds, voc, unknownVocabulary, threshold, i, matchedVoc);
        }
    }

    /**
     * Tries to match an attribute from the data source to the list of vocabulary candidates. If the attribute can be assigned to
     * one of these, it is removed from the 'unknown' (or candidate) vocabulary list and added to the real weak vocabulary. If the attribute
     * can not be matched to any candidate vocabulary then the attribute will be added to the 'unknown' vocabulary itself
     *
     * @param ds                the data source
     * @param voc               the vocabulary
     * @param unknownVocabulary the candidate vocabulary list
     * @param threshold         the threshold to classify values as matches [0,1]
     * @param i                 the attribute index
     * @param matchedVoc        true if a match with an existing vocabulary was detected earlier
     */
    private static void matchWeakVoc(DataSource ds, Vocabulary voc, UnknownVocabulary unknownVocabulary, double threshold, int i, boolean matchedVoc) {
        if (!matchedVoc) {
            DescriptiveStatistics attributeStatistics = new DescriptiveStatistics();
            List attributeSamples = ds.getDataInterface().getAttributes().get(i).getSamples();
            boolean matchedUnknownVoc = false;

            for (int j = 0; j < unknownVocabulary.getVocabulary().size(); j++) {
                if (ds.getDataInterface().getAttributes().get(i) instanceof FloatAttribute &&
                        unknownVocabulary.getVocabulary().get(j).getInstances().get(0) instanceof FloatAttribute) {
                    for (Object sample : attributeSamples)
                        attributeStatistics.addValue((float) sample);

                    DescriptiveStatistics unknownVocStatistics = new DescriptiveStatistics();
                    List<Float> unknownVocSamples = unknownVocabulary.getVocabulary().get(j).getSamples();
                    for (Float sample : unknownVocSamples)
                        unknownVocStatistics.addValue(sample);
                    double weakMatchFactor = calcMatchFactor(attributeStatistics, unknownVocStatistics,
                            ds.getDataInterface().getAttributes().get(i).getName().toLowerCase(),
                            unknownVocabulary.getVocabulary().get(j).getName().toLowerCase());
                    // attribute matches with unknown vocabulary
                    if (weakMatchFactor <= threshold) {
                        matchedUnknownVoc = addWeakVoc2Voc(ds, voc, unknownVocabulary, i, j);
                        break;
                    }
                } else if (ds.getDataInterface().getAttributes().get(i) instanceof IntegerAttribute &&
                        unknownVocabulary.getVocabulary().get(j).getInstances().get(0) instanceof IntegerAttribute) {
                    for (Object sample : attributeSamples)
                        attributeStatistics.addValue((int) sample);

                    DescriptiveStatistics unknownVocStatistics = new DescriptiveStatistics();
                    List<Integer> unknownVocSamples = unknownVocabulary.getVocabulary().get(j).getSamples();
                    for (Integer sample : unknownVocSamples)
                        unknownVocStatistics.addValue(sample);
                    double weakMatchFactor = calcMatchFactor(attributeStatistics, unknownVocStatistics,
                            ds.getDataInterface().getAttributes().get(i).getName().toLowerCase(),
                            unknownVocabulary.getVocabulary().get(j).getName().toLowerCase());

                    // attribute matches with weak vocabulary
                    if (weakMatchFactor <= threshold) {
                        matchedUnknownVoc = addWeakVoc2Voc(ds, voc, unknownVocabulary, i, j);
                        break;
                    }
                } else if (ds.getDataInterface().getAttributes().get(i) instanceof StringAttribute &&
                        unknownVocabulary.getVocabulary().get(j).getInstances().get(0) instanceof StringAttribute) {
                    JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
                    double matchFactor = 1 - jaroWinklerDistance.apply(
                            ds.getDataInterface().getAttributes().get(i).getName().toLowerCase(),
                            unknownVocabulary.getVocabulary().get(j).getName().toLowerCase().toLowerCase());
                    if (matchFactor < threshold) {
                        matchedUnknownVoc = addWeakVoc2Voc(ds, voc, unknownVocabulary, i, j);
                        break;
                    }

                }
            }
            if (!matchedUnknownVoc) {
                unknownVocabulary.addVocabularyItem(ds.getDataInterface().getAttributes().get(i),
                        ds.getDataInterface().getAttributes().get(i).getName(), "default description",
                        ds.getDataInterface().getAttributes().get(i).getSamples());
            } else {
            }
        }
    }

    /**
     * Adds an unknown vocabulary to the weak vocabulary
     *
     * @param ds                the data source of the respective attribute
     * @param voc               the vocabulary
     * @param unknownVocabulary the candidate vocabulary list
     * @param i                 the index of the desired vocabulary item
     * @param j                 the index of the candidate vocabulary item
     * @return
     */
    private static boolean addWeakVoc2Voc(DataSource ds, Vocabulary voc, UnknownVocabulary unknownVocabulary, int i, int j) {
        VocabularyItem theItem = unknownVocabulary.getVocabulary().get(j);
        theItem.getSamples().addAll(ds.getDataInterface().getAttributes().get(i).getSamples());
        theItem.addInstance(ds.getDataInterface().getAttributes().get(i));
        voc.getVocabulary().add(unknownVocabulary.getVocabulary().get(j));
        voc.getVocabulary().get(voc.getVocabulary().size() - 1).addInstance(unknownVocabulary.getVocabulary().get(j).getInstances().get(0));
        unknownVocabulary.removeVocabularyItem(j);
        return true;
    }

    /**
     * Calculates a value between 0 and 1 that indicates how similar the data source attribute is
     * to the vocabulary attribute. 0 means no similarity, 1 means very strong similarity.
     *
     * @param attributeStatistics Samples of the attribute
     * @param vocStatistics       Samples of an instance of the vocabulary to compare
     * @param attributeName       name of the attribute
     * @param vocName             name of the vocabulary
     * @return a value between 0 and 1, where 0 is the maximum possible similarity.
     */
    private static double calcMatchFactor(DescriptiveStatistics attributeStatistics,
                                          DescriptiveStatistics vocStatistics,
                                          String attributeName, String vocName) {
        // calculate normalized median, mean and standard deviation of an attribute
        double medianNormalized = Math.abs(attributeStatistics.getPercentile(50) -
                vocStatistics.getPercentile(50)) / (vocStatistics.getPercentile(90) -
                vocStatistics.getPercentile(10));
        double meanNormalized = Math.abs(attributeStatistics.getMean() -
                vocStatistics.getMean()) / (vocStatistics.getPercentile(90) -
                vocStatistics.getPercentile(10));
        double sdtNormalized = Math.abs(attributeStatistics.getStandardDeviation() -
                vocStatistics.getStandardDeviation()) / (vocStatistics.getPercentile(90) -
                vocStatistics.getPercentile(10));
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
        double attributeNameDistance = jaroWinklerDistance.apply(
                attributeName.toLowerCase(),
                vocName.toLowerCase());

        // calculated distance measurements will be normalized into one value
        double matchFactor = (1 * medianNormalized + 1 * meanNormalized + 1 * sdtNormalized +
                80 * (1 - attributeNameDistance)) / 83;
        return matchFactor;
    }

    /**
     * checks if a numeric (float/int/double) data source attribute matches a vocabulary attribute.
     * Returns true if the attribute i could be matched with vocabulary j.
     * The matched vocabulary will be added to the vocabulary. The sensitivity of the matching can
     * be adjusted by the threshold parameter. The similarity is measured based on the median, mean,
     * standard deviation, and Jaro-Winkler-Distance
     *
     * @param ds                  the data source
     * @param voc                 the vocabulary
     * @param threshold           the threshold to classify matches
     * @param i                   the index of the attribute
     * @param matchedVoc          if a match was detected earlier
     * @param j                   the index of the vocabulary item
     * @param attributeStatistics the samples of the attribute
     * @param vocStatistics       the samples of an instance of the vocabulary item
     * @return                    true if the attribute i could be matched to an vocabulary item, false if not
     */
    private static boolean isMatchedNumericVoc(DataSource ds, Vocabulary voc, double threshold,
                                               int i, boolean matchedVoc, int j, DescriptiveStatistics attributeStatistics,
                                               DescriptiveStatistics vocStatistics) {
        double matchFactor = calcMatchFactor(attributeStatistics, vocStatistics,
                ds.getDataInterface().getAttributes().get(i).getName(),
                voc.getVocabulary().get(j).getName().toLowerCase());

        System.out.println("Matching [" + ds.getName() + "]/" + ds.getDataInterface().getAttributes().get(i).getName() + " with "
                + voc.getVocabulary().get(j).getName() + ". Score:" + matchFactor);
        if (matchFactor <= threshold && matchFactor >= 0) {
            voc.getVocabulary().get(j).addInstance(ds.getDataInterface().getAttributes().get(i));
            matchedVoc = true;
        }
        return matchedVoc;
    }

    /**
     * Checks if a string data source attribute matches a vocabulary attribute.
     * Returns true if the attribute i could be matched with vocabulary j.
     * The matched vocabulary will be added to the vocabulary. The sensitivity of the matching can
     * be adjusted by the threshold parameter. The similarity is measured with the Jaro-Winkler-Distance.
     *
     * @param ds         the data source
     * @param voc        the vocabulary
     * @param threshold  the threshold to classify matches
     * @param i          the index of the attribute
     * @param matchedVoc if a match was detected earlier
     * @param j          the index of the vocabulary item
     * @return
     */
    private static boolean isMatchedStringVoc(DataSource ds, Vocabulary voc, double threshold,
                                              int i, boolean matchedVoc, int j) {
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
        double matchFactor = 1 - jaroWinklerDistance.apply(
                ds.getDataInterface().getAttributes().get(i).getName().toLowerCase(),
                voc.getVocabulary().get(j).getName().toLowerCase().toLowerCase());
        System.out.println("Matching [" + ds.getName() + "]/" + ds.getDataInterface().getAttributes().get(i).getName() + " with "
                + voc.getVocabulary().get(j).getName() + ". Score:" + matchFactor);
        if (matchFactor < threshold && matchFactor >= 0) {
            voc.getVocabulary().get(j).addInstance(ds.getDataInterface().getAttributes().get(i));
            matchedVoc = true;
        }
        return matchedVoc;
    }

}
