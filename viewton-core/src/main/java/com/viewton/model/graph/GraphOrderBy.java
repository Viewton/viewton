package com.viewton.model.graph;

import java.util.Objects;

/**
 * Ordering instruction for the Graph-like query DSL.
 */
public final class GraphOrderBy {
    public enum Direction {
        ASC,
        DESC
    }

    private final String field;
    private final Direction direction;

    public GraphOrderBy(String field, Direction direction) {
        this.field = Objects.requireNonNull(field, "field");
        this.direction = Objects.requireNonNull(direction, "direction");
    }

    public String getField() {
        return field;
    }

    public Direction getDirection() {
        return direction;
    }
}
