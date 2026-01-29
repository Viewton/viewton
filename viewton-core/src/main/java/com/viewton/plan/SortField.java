package com.viewton.plan;

import java.util.Objects;

/**
 * Sorting field directive.
 */
public final class SortField {
    private final String field;
    private final SortDirection direction;

    public SortField(String field, SortDirection direction) {
        this.field = Objects.requireNonNull(field, "field");
        this.direction = Objects.requireNonNull(direction, "direction");
    }

    public String getField() {
        return field;
    }

    public SortDirection getDirection() {
        return direction;
    }
}
