package com.viewton.jooq.mapping;

import org.jooq.Field;
import org.jooq.Record;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Wrapper around a jOOQ {@link Record}.
 */
public final class JooqRow {
    private final Record record;

    public JooqRow(Record record) {
        this.record = Objects.requireNonNull(record, "record");
    }

    public Object get(String field) {
        return record.get(field);
    }

    public <T> T get(Field<T> field) {
        return record.get(field);
    }

    public <T> T into(Class<T> type) {
        return record.into(type);
    }

    public Map<String, Object> asMap() {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Field<?> field : record.fields()) {
            values.put(field.getName(), record.get(field));
        }
        return values;
    }
}
