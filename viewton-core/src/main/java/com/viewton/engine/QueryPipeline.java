package com.viewton.engine;

import com.viewton.api.input.QueryInput;
import com.viewton.execution.ExecutionContext;
import com.viewton.execution.ExecutionResult;
import com.viewton.model.QueryModel;
import com.viewton.schema.Schema;
import com.viewton.sql.SqlStatement;

/**
 * Orchestrates the query lifecycle: parsing, modeling, SQL generation, and execution.
 */
public interface QueryPipeline<I extends QueryInput, C> {
    QueryModel parse(I input);

    SqlStatement generate(QueryModel model, Schema schema);

    ExecutionResult execute(SqlStatement statement, ExecutionContext<C> context);
}
