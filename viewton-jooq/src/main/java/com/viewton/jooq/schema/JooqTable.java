package com.viewton.jooq.schema;

import com.viewton.schema.Table;

import java.util.Objects;

final class JooqTable implements Table {
    private final org.jooq.Table<?> table;

    JooqTable(org.jooq.Table<?> table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    org.jooq.Table<?> unwrap() {
        return table;
    }
}
