package com.viewton.model.graph.value;

import com.viewton.model.graph.GraphValue;
import com.viewton.model.graph.GraphValueType;

/**
 * Boolean value for the Graph-like DSL.
 */
public final class GraphBooleanValue implements GraphValue {
    private final boolean value;

    public GraphBooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public GraphValueType getType() {
        return GraphValueType.BOOLEAN;
    }

    @Override
    public Object getRawValue() {
        return value;
    }

    @Override
    public Boolean asBoolean() {
        return value;
    }

    @Override
    public String toSqlLiteral() {
        return Boolean.toString(value);
    }
}
