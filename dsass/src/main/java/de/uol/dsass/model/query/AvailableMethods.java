package de.uol.dsass.model.query;

/**
 * Typical filter methods for gql-queries: equal to, range query, lower than equal, categories,
 * first n elements, first n elements after element m. Currently, we only implement minmax.
 */
public enum AvailableMethods {
    equal, minmax, gte, lte, category, first, first_after
}
