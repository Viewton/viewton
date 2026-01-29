package com.viewton.plan;

/**
 * Typed value used in query plans.
 */
public interface QueryValue {
    QueryValueType getType();

    Object getValue();
}
