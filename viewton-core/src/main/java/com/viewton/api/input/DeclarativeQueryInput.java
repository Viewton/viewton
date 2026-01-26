package com.viewton.api.input;

/**
 * Placeholder for GraphQL-like declarative query input.
 * The concrete DSL contract will be defined in a future iteration.
 */
public interface DeclarativeQueryInput extends QueryInput {
    /**
     * Returns the raw declarative representation (opaque for now).
     */
    Object definition();
}
