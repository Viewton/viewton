package com.viewton.plan;

/**
 * Flags describing query behavior (e.g. count, distinct).
 */
public final class QueryFlags {
    private final boolean count;
    private final boolean distinct;

    public QueryFlags(boolean count, boolean distinct) {
        this.count = count;
        this.distinct = distinct;
    }

    public boolean isCount() {
        return count;
    }

    public boolean isDistinct() {
        return distinct;
    }
}
