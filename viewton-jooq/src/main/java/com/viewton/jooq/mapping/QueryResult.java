package com.viewton.jooq.mapping;

import com.viewton.execution.ExecutionResult;

import java.util.List;
import java.util.Objects;

/**
 * Container for rows returned by jOOQ execution.
 */
public final class QueryResult implements ExecutionResult {
    private final List<JooqRow> rows;

    public QueryResult(List<JooqRow> rows) {
        this.rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
    }

    public List<JooqRow> getRows() {
        return rows;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
