package com.viewton.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a single filter predicate from REST input.
 */
public final class FilterCriterion {
    private final String field;
    private final FilterOperator operator;
    private final List<String> values;
    private final boolean ignoreCase;

    public FilterCriterion(String field, FilterOperator operator, List<String> values, boolean ignoreCase) {
        this.field = Objects.requireNonNull(field, "field");
        this.operator = Objects.requireNonNull(operator, "operator");
        this.values = List.copyOf(Objects.requireNonNull(values, "values"));
        this.ignoreCase = ignoreCase;
    }

    public String getField() {
        return field;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public List<String> getValues() {
        return values;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }
}
