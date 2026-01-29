package com.viewton.model.graph.value;

import com.viewton.model.graph.GraphValue;
import com.viewton.model.graph.GraphValueType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Numeric value for the Graph-like DSL.
 */
public final class GraphNumberValue implements GraphValue {
    private final BigDecimal value;

    public GraphNumberValue(BigDecimal value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public GraphValueType getType() {
        return GraphValueType.NUMBER;
    }

    @Override
    public Object getRawValue() {
        return value;
    }

    @Override
    public Integer asInteger() {
        return value.intValueExact();
    }

    @Override
    public String toSqlLiteral() {
        return value.toPlainString();
    }
}
