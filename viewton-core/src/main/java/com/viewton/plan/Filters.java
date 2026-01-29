package com.viewton.plan;

import java.util.List;
import java.util.Objects;

/**
 * Collection of filter predicates.
 */
public final class Filters {
    private final List<FilterCriterion> criteria;

    public Filters(List<FilterCriterion> criteria) {
        this.criteria = List.copyOf(Objects.requireNonNull(criteria, "criteria"));
    }

    public List<FilterCriterion> getCriteria() {
        return criteria;
    }
}
