package com.viewton.plan.value;

import com.viewton.plan.QueryValue;
import com.viewton.plan.QueryValueType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Numeric value for the query plan.
 */
public final class QueryNumberValue implements QueryValue {
    private final BigDecimal value;

    public QueryNumberValue(BigDecimal value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public QueryValueType getType() {
        return QueryValueType.NUMBER;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public BigDecimal getNumber() {
        return value;
    }
}
