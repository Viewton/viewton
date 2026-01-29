package com.viewton.plan;

import java.util.Objects;

/**
 * Semantic query representation independent of input language and execution engine.
 */
public final class QueryPlan {
    private final EntityRef entity;
    private final Projection projection;
    private final Filters filters;
    private final Sorting sorting;
    private final Pagination pagination;
    private final Aggregations aggregations;
    private final QueryFlags flags;

    public QueryPlan(
            EntityRef entity,
            Projection projection,
            Filters filters,
            Sorting sorting,
            Pagination pagination,
            Aggregations aggregations,
            QueryFlags flags
    ) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.projection = Objects.requireNonNull(projection, "projection");
        this.filters = Objects.requireNonNull(filters, "filters");
        this.sorting = Objects.requireNonNull(sorting, "sorting");
        this.pagination = Objects.requireNonNull(pagination, "pagination");
        this.aggregations = Objects.requireNonNull(aggregations, "aggregations");
        this.flags = Objects.requireNonNull(flags, "flags");
    }

    public EntityRef getEntity() {
        return entity;
    }

    public Projection getProjection() {
        return projection;
    }

    public Filters getFilters() {
        return filters;
    }

    public Sorting getSorting() {
        return sorting;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public Aggregations getAggregations() {
        return aggregations;
    }

    public QueryFlags getFlags() {
        return flags;
    }
}
