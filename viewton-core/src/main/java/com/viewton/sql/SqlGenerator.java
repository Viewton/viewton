package com.viewton.sql;

import com.viewton.model.QueryModel;
import com.viewton.schema.Schema;

/**
 * Produces SQL statements from the internal query model and schema metadata.
 */
public interface SqlGenerator {
    SqlStatement generate(QueryModel model, Schema schema);
}
