package com.viewton.plan;

/**
 * Flags describing query behavior (e.g. count, distinct).
 */
public final class QueryFlags {
    private final boolean count;
    private final boolean distinct;
    private final boolean entities;

    public QueryFlags(boolean count, boolean distinct, boolean entities) {
        this.count = count;
        this.distinct = distinct;
        this.entities = entities;
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
}
