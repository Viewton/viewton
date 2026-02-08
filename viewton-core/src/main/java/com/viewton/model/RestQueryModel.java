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
    private final boolean entities;
    private final List<String> attributes;
    private final List<String> sum;
    private final List<String> avg;
    private final List<String> min;
    private final List<String> max;
    private final List<String> sorting;
    private final List<FilterCriterion> filters;

    public RestQueryModel(
            Integer page,
            Integer pageSize,
            boolean count,
            boolean distinct,
            boolean entities,
            List<String> attributes,
            List<String> sum,
            List<String> avg,
            List<String> min,
            List<String> max,
            List<String> sorting,
            List<FilterCriterion> filters
    ) {
        this.page = page;
        this.pageSize = pageSize;
        this.count = count;
        this.distinct = distinct;
        this.entities = entities;
        this.attributes = List.copyOf(Objects.requireNonNull(attributes, "attributes"));
        this.sum = List.copyOf(Objects.requireNonNull(sum, "sum"));
        this.avg = List.copyOf(Objects.requireNonNull(avg, "avg"));
        this.min = List.copyOf(Objects.requireNonNull(min, "min"));
        this.max = List.copyOf(Objects.requireNonNull(max, "max"));
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

    public boolean isEntities() {
        return entities;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public List<String> getSum() {
        return sum;
    }

    public List<String> getAvg() {
        return avg;
    }

    public List<String> getMin() {
        return min;
    }

    public List<String> getMax() {
        return max;
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
