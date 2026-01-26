package com.viewton.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Basic query model parsed from REST parameters.
 */
public final class RestQueryModel implements QueryModel {
    private final Integer page;
    private final Integer pageSize;
    private final boolean count;
    private final boolean distinct;
    private final List<String> attributes;
    private final List<String> sum;
    private final List<String> sorting;
    private final List<FilterCriterion> filters;

    public RestQueryModel(
            Integer page,
            Integer pageSize,
            boolean count,
            boolean distinct,
            List<String> attributes,
            List<String> sum,
            List<String> sorting,
            List<FilterCriterion> filters
    ) {
        this.page = page;
        this.pageSize = pageSize;
        this.count = count;
        this.distinct = distinct;
        this.attributes = List.copyOf(Objects.requireNonNull(attributes, "attributes"));
        this.sum = List.copyOf(Objects.requireNonNull(sum, "sum"));
        this.sorting = List.copyOf(Objects.requireNonNull(sorting, "sorting"));
        this.filters = List.copyOf(Objects.requireNonNull(filters, "filters"));
    }

    public Optional<Integer> getPage() {
        return Optional.ofNullable(page);
    }

    public Optional<Integer> getPageSize() {
        return Optional.ofNullable(pageSize);
    }

    public boolean isCount() {
        return count;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public List<String> getSum() {
        return sum;
    }

    public List<String> getSorting() {
        return sorting;
    }

    public List<FilterCriterion> getFilters() {
        return filters;
    }

    public Set<String> getFilteredFields() {
        return filters.stream().map(FilterCriterion::getField).collect(java.util.stream.Collectors.toSet());
    }
}
