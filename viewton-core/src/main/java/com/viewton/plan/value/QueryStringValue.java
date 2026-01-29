package com.viewton.plan.value;

import com.viewton.plan.QueryValue;
import com.viewton.plan.QueryValueType;

import java.util.Objects;

/**
 * String value for the query plan.
 */
public final class QueryStringValue implements QueryValue {
    private final String value;

    public QueryStringValue(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public QueryValueType getType() {
        return QueryValueType.STRING;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public String getString() {
        return value;
    }
}
