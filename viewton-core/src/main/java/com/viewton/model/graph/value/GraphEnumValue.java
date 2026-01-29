package com.viewton.model.graph.value;

import com.viewton.model.graph.GraphValue;
import com.viewton.model.graph.GraphValueType;

import java.util.Objects;

/**
 * Enum (identifier) value for the Graph-like DSL.
 */
public final class GraphEnumValue implements GraphValue {
    private final String value;

    public GraphEnumValue(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public GraphValueType getType() {
        return GraphValueType.ENUM;
    }

    @Override
    public Object getRawValue() {
        return value;
    }

    @Override
    public String asIdentifier() {
        return value;
    }

    @Override
    public String toSqlLiteral() {
        return "'" + value.replace("'", "''") + "'";
    }
}
