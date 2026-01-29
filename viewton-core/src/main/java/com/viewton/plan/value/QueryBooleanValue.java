package com.viewton.plan.value;

import com.viewton.plan.QueryValue;
import com.viewton.plan.QueryValueType;

/**
 * Boolean value for the query plan.
 */
public final class QueryBooleanValue implements QueryValue {
    private final boolean value;

    public QueryBooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public QueryValueType getType() {
        return QueryValueType.BOOLEAN;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public boolean getBoolean() {
        return value;
    }
}
