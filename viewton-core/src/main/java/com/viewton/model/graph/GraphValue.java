package com.viewton.model.graph;

/**
 * Value representation for the Graph-like DSL.
 */
public interface GraphValue {
    GraphValueType getType();

    Object getRawValue();

    default String asIdentifier() {
        throw new IllegalStateException("Value is not an identifier: " + getType());
    }

    default Integer asInteger() {
        throw new IllegalStateException("Value is not a number: " + getType());
    }

    default Boolean asBoolean() {
        throw new IllegalStateException("Value is not a boolean: " + getType());
    }

    default String toSqlLiteral() {
        throw new IllegalStateException("Value cannot be rendered as SQL literal: " + getType());
    }
}
