package com.viewton.model.graph;

import java.util.Objects;

/**
 * Represents a single filter predicate in the Graph-like query DSL.
 */
public final class GraphFilterCriterion {
    private final String field;
    private final GraphOperator operator;
    private final GraphValue value;

    public GraphFilterCriterion(String field, GraphOperator operator, GraphValue value) {
        this.field = Objects.requireNonNull(field, "field");
        this.operator = Objects.requireNonNull(operator, "operator");
        this.value = Objects.requireNonNull(value, "value");
    }

    public String getField() {
        return field;
    }

    public GraphOperator getOperator() {
        return operator;
    }

    public GraphValue getValue() {
        return value;
    }
}
