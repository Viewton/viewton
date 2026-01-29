package com.viewton.plan;

import java.util.List;
import java.util.Objects;

/**
 * Selected fields for the query plan.
 */
public final class Projection {
    private final List<String> fields;

    public Projection(List<String> fields) {
        this.fields = List.copyOf(Objects.requireNonNull(fields, "fields"));
    }

    public List<String> getFields() {
        return fields;
    }
}
