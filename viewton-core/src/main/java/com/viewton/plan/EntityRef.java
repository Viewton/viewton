package com.viewton.plan;

/**
 * Reference to a queryable entity resolved from the schema.
 */
public record EntityRef(String name) {
    public EntityRef {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Entity name is required");
        }
    }
}
