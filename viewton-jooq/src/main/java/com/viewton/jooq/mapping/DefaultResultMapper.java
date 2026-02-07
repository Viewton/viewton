package com.viewton.jooq.mapping;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Default mapper that converts {@link JooqRow} instances into DTOs via jOOQ.
 */
public final class DefaultResultMapper {
    public <T> List<T> map(QueryResult result, Class<T> type) {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(type, "type");
        return result.getRows()
                .stream()
                .map(row -> row.into(type))
                .collect(Collectors.toList());
    }
}
