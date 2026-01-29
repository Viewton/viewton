package com.viewton.plan;

import java.util.List;
import java.util.Objects;

/**
 * Normalized filter predicate.
 */
public final class FilterCriterion {
    private final String field;
    private final QueryOperator operator;
    private final List<QueryValue> values;
    private final boolean ignoreCase;

    public FilterCriterion(String field, QueryOperator operator, List<QueryValue> values, boolean ignoreCase) {
        this.field = Objects.requireNonNull(field, "field");
        this.operator = Objects.requireNonNull(operator, "operator");
        this.values = List.copyOf(Objects.requireNonNull(values, "values"));
        this.ignoreCase = ignoreCase;
    }

    public String getField() {
        return field;
    }

    public QueryOperator getOperator() {
        return operator;
    }

    public List<QueryValue> getValues() {
        return values;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }
}
