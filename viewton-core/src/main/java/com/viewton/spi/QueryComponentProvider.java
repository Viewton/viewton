package com.viewton.spi;

import com.viewton.api.input.QueryInput;
import com.viewton.api.parser.QueryInputParser;
import com.viewton.execution.QueryExecutor;
import com.viewton.sql.SqlGenerator;

import java.util.Optional;

/**
 * SPI entry point for supplying parsers, generators, and executors.
 * Implementations can be registered via {@link java.util.ServiceLoader}.
 */
public interface QueryComponentProvider {
    Optional<QueryInputParser<? extends QueryInput>> parser();

    Optional<SqlGenerator> sqlGenerator();

    Optional<QueryExecutor<?>> executor();
}
