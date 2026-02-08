package com.viewton.materialized.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Response wrapper for materialized queries with entities and aggregations.
 */
public final class MaterializedQueryResponse {
    private final List<Map<String, Object>> entities;
    private final Map<String, Object> aggregations;

    public MaterializedQueryResponse(List<Map<String, Object>> entities, Map<String, Object> aggregations) {
        this.entities = List.copyOf(Objects.requireNonNull(entities, "entities"));
        this.aggregations = Map.copyOf(Objects.requireNonNull(aggregations, "aggregations"));
    }

    public List<Map<String, Object>> getEntities() {
        return entities;
    }

    public Map<String, Object> getAggregations() {
        return aggregations;
    }
}
