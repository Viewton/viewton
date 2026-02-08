package com.viewton.plan;

import java.util.List;
import java.util.Objects;

/**
 * Aggregations requested by the query.
 */
public final class Aggregations {
    private final List<String> sumFields;
    private final List<String> avgFields;
    private final List<String> minFields;
    private final List<String> maxFields;

    public Aggregations(
            List<String> sumFields,
            List<String> avgFields,
            List<String> minFields,
            List<String> maxFields
    ) {
        this.sumFields = List.copyOf(Objects.requireNonNull(sumFields, "sumFields"));
        this.avgFields = List.copyOf(Objects.requireNonNull(avgFields, "avgFields"));
        this.minFields = List.copyOf(Objects.requireNonNull(minFields, "minFields"));
        this.maxFields = List.copyOf(Objects.requireNonNull(maxFields, "maxFields"));
    }

    public List<String> getSumFields() {
        return sumFields;
    }

    public List<String> getAvgFields() {
        return avgFields;
    }

    public List<String> getMinFields() {
        return minFields;
    }

    public List<String> getMaxFields() {
        return maxFields;
    }
}
