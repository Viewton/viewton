package com.viewton.jooq;

import com.viewton.execution.QueryExecutor;
import org.jooq.DSLContext;

/**
 * Base class for jOOQ-backed SQL execution adapters.
 */
public abstract class JooqQueryExecutor implements QueryExecutor<DSLContext> {
}
