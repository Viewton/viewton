package com.viewton.jooq;

import com.viewton.execution.ExecutionContext;
import org.jooq.DSLContext;

/**
 * Execution context wrapper for jOOQ's {@link DSLContext}.
 */
public final class JooqExecutionContext implements ExecutionContext<DSLContext> {
    private final DSLContext dslContext;

    public JooqExecutionContext(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public DSLContext unwrap() {
        return dslContext;
    }
}
