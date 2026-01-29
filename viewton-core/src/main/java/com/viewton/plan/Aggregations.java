package com.viewton.plan;

import java.util.List;
import java.util.Objects;

/**
 * Aggregations requested by the query.
 */
public final class Aggregations {
    private final List<String> sumFields;

    public Aggregations(List<String> sumFields) {
        this.sumFields = List.copyOf(Objects.requireNonNull(sumFields, "sumFields"));
    }

    public List<String> getSumFields() {
        return sumFields;
    }
}
