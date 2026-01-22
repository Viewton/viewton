package com.viewton.execution;

import com.viewton.sql.SqlStatement;

/**
 * Executes generated SQL using a specific execution context.
 */
public interface QueryExecutor<C> {
    ExecutionResult execute(SqlStatement statement, ExecutionContext<C> context);
}
