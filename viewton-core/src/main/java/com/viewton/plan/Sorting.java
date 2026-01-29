package com.viewton.plan;

import java.util.List;
import java.util.Objects;

/**
 * Sorting instructions for a query plan.
 */
public final class Sorting {
    private final List<SortField> fields;

    public Sorting(List<SortField> fields) {
        this.fields = List.copyOf(Objects.requireNonNull(fields, "fields"));
    }

    public List<SortField> getFields() {
        return fields;
    }
}
