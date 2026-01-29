package com.viewton.model.graph;

import com.viewton.model.QueryModel;

import java.util.List;
import java.util.Objects;

/**
 * Typed AST for the Graph-like query DSL.
 */
public final class GraphQueryModel implements QueryModel {
    private final String resource;
    private final List<String> selections;
    private final GraphPagination pagination;
    private final boolean distinct;
    private final boolean count;
    private final List<String> sumFields;
    private final List<GraphOrderBy> orderBy;
    private final List<GraphFilterCriterion> filters;

    public GraphQueryModel(
            String resource,
            List<String> selections,
            GraphPagination pagination,
            boolean distinct,
            boolean count,
            List<String> sumFields,
            List<GraphOrderBy> orderBy,
            List<GraphFilterCriterion> filters
    ) {
        this.resource = Objects.requireNonNull(resource, "resource");
        this.selections = List.copyOf(Objects.requireNonNull(selections, "selections"));
        this.pagination = pagination;
        this.distinct = distinct;
        this.count = count;
        this.sumFields = List.copyOf(Objects.requireNonNull(sumFields, "sumFields"));
        this.orderBy = List.copyOf(Objects.requireNonNull(orderBy, "orderBy"));
        this.filters = List.copyOf(Objects.requireNonNull(filters, "filters"));
    }

    public String getResource() {
        return resource;
    }

    public List<String> getSelections() {
        return selections;
    }

    public GraphPagination getPagination() {
        return pagination;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public boolean isCount() {
        return count;
    }

    public List<String> getSumFields() {
        return sumFields;
    }

    public List<GraphOrderBy> getOrderBy() {
        return orderBy;
    }

    public List<GraphFilterCriterion> getFilters() {
        return filters;
    }
}
