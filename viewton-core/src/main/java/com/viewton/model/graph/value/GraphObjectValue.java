package com.viewton.model.graph.value;

import com.viewton.model.graph.GraphValue;
import com.viewton.model.graph.GraphValueType;

import java.util.Map;
import java.util.Objects;

/**
 * Object value for the Graph-like DSL.
 */
public final class GraphObjectValue implements GraphValue {
    private final Map<String, GraphValue> values;

    public GraphObjectValue(Map<String, GraphValue> values) {
        this.values = java.util.Collections.unmodifiableMap(
                new java.util.LinkedHashMap<>(Objects.requireNonNull(values, "values"))
        );
    }

    @Override
    public GraphValueType getType() {
        return GraphValueType.OBJECT;
    }

    @Override
    public Object getRawValue() {
        return values;
    }

    public Map<String, GraphValue> getValues() {
        return values;
    }
}
