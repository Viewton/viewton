package com.viewton.model.graph.value;

import com.viewton.model.graph.GraphValue;
import com.viewton.model.graph.GraphValueType;

import java.util.List;
import java.util.Objects;

/**
 * List value for the Graph-like DSL.
 */
public final class GraphListValue implements GraphValue {
    private final List<GraphValue> values;

    public GraphListValue(List<GraphValue> values) {
        this.values = List.copyOf(Objects.requireNonNull(values, "values"));
    }

    @Override
    public GraphValueType getType() {
        return GraphValueType.LIST;
    }

    @Override
    public Object getRawValue() {
        return values;
    }

    public List<GraphValue> getValues() {
        return values;
    }
}
