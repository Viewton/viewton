package com.viewton.jooq.schema;

import com.viewton.schema.Schema;
import com.viewton.schema.Table;
import org.jooq.DSLContext;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * jOOQ-backed schema adapter for Viewton.
 */
public final class JooqSchema implements Schema {
    private final Map<String, org.jooq.Table<?>> tables;
    private final Map<Class<?>, String> dtoMappings;

    public JooqSchema(Map<String, org.jooq.Table<?>> tables, Map<Class<?>, String> dtoMappings) {
        this.tables = Map.copyOf(Objects.requireNonNull(tables, "tables"));
        this.dtoMappings = Map.copyOf(Objects.requireNonNull(dtoMappings, "dtoMappings"));
    }

    public static JooqSchema fromMeta(DSLContext dslContext) {
        Objects.requireNonNull(dslContext, "dslContext");
        Map<String, org.jooq.Table<?>> tables = new LinkedHashMap<>();
        for (org.jooq.Table<?> table : dslContext.meta().getTables()) {
            tables.put(table.getName(), table);
        }
        return new JooqSchema(tables, Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Table table(String name) {
        org.jooq.Table<?> table = tables.get(name);
        return table == null ? null : new JooqTable(table);
    }

    public org.jooq.Table<?> jooqTable(String name) {
        return tables.get(name);
    }

    public String resolveEntityName(Class<?> dtoType) {
        Objects.requireNonNull(dtoType, "dtoType");
        String mapped = dtoMappings.get(dtoType);
        if (mapped != null) {
            return mapped;
        }
        String simpleName = dtoType.getSimpleName();
        if (tables.containsKey(simpleName)) {
            return simpleName;
        }
        String lower = simpleName.toLowerCase(Locale.ROOT);
        if (tables.containsKey(lower)) {
            return lower;
        }
        return null;
    }

    public static final class Builder {
        private final Map<String, org.jooq.Table<?>> tables = new LinkedHashMap<>();
        private final Map<Class<?>, String> dtoMappings = new LinkedHashMap<>();

        public Builder registerTable(String entityName, org.jooq.Table<?> table) {
            Objects.requireNonNull(entityName, "entityName");
            Objects.requireNonNull(table, "table");
            tables.put(entityName, table);
            return this;
        }

        public Builder mapDto(Class<?> dtoType, String entityName) {
            Objects.requireNonNull(dtoType, "dtoType");
            Objects.requireNonNull(entityName, "entityName");
            dtoMappings.put(dtoType, entityName);
            return this;
        }

        public JooqSchema build() {
            return new JooqSchema(tables, dtoMappings);
        }
    }
}
