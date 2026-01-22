package com.viewton.execution;

/**
 * Wraps a user-provided execution context (e.g., jOOQ DSLContext).
 */
public interface ExecutionContext<C> {
    C unwrap();
}
