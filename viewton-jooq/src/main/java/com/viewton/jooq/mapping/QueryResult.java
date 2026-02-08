package com.viewton.jooq.mapping;

import com.viewton.execution.ExecutionResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Container for rows returned by jOOQ execution.
 */
public final class QueryResult implements ExecutionResult {
    private final List<JooqRow> rows;
    private final Map<String, Object> aggregations;

    public QueryResult(List<JooqRow> rows, Map<String, Object> aggregations) {
        this.rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
        this.aggregations = Map.copyOf(Objects.requireNonNull(aggregations, "aggregations"));
    }

    public List<JooqRow> getRows() {
        return rows;
    }

    public Map<String, Object> getAggregations() {
        return aggregations;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
