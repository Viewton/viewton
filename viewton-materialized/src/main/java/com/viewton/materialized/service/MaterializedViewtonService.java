package com.viewton.materialized.service;

import com.viewton.api.input.RestQueryInput;
import com.viewton.api.parser.RestQueryInputParser;
import com.viewton.jooq.executor.JooqQueryExecutor;
import com.viewton.jooq.mapping.QueryResult;
import com.viewton.materialized.api.MaterializedQueryResponse;
import com.viewton.jooq.schema.JooqSchema;
import com.viewton.model.QueryModel;
import com.viewton.model.RestQueryModel;
import com.viewton.plan.QueryPlan;
import com.viewton.plan.RestQueryPlanNormalizer;
import org.jooq.DSLContext;
import org.jooq.Table;

import java.util.Map;
import java.util.Objects;

/**
 * Service that builds dynamic Viewton queries from the database meta-model.
 */
public final class MaterializedViewtonService {
    private final DSLContext dslContext;
    private final RestQueryInputParser restQueryInputParser;
    private final RestQueryPlanNormalizer restQueryPlanNormalizer;

    public MaterializedViewtonService(
            DSLContext dslContext,
            RestQueryInputParser restQueryInputParser,
            RestQueryPlanNormalizer restQueryPlanNormalizer
    ) {
        this.dslContext = Objects.requireNonNull(dslContext, "dslContext");
        this.restQueryInputParser = Objects.requireNonNull(restQueryInputParser, "restQueryInputParser");
        this.restQueryPlanNormalizer = Objects.requireNonNull(restQueryPlanNormalizer, "restQueryPlanNormalizer");
    }

    public MaterializedQueryResponse list(String schemaName, String tableName, Map<String, String> parameters) {
        Objects.requireNonNull(schemaName, "schemaName");
        Objects.requireNonNull(tableName, "tableName");
        Table<?> table = resolveTable(schemaName, tableName);
        JooqSchema schema = JooqSchema.builder()
                .registerTable(tableName, table)
                .build();

        QueryModel model = restQueryInputParser.parse(new RestQueryInput(parameters));
        QueryPlan plan = restQueryPlanNormalizer.normalize(asRestModel(model), tableName, schema);

        JooqQueryExecutor executor = new JooqQueryExecutor(dslContext, schema);
        QueryResult result = executor.execute(plan);
        return new MaterializedQueryResponse(
                result.getRows().stream().map(row -> row.asMap()).toList(),
                result.getAggregations()
        );
    }

    private Table<?> resolveTable(String schemaName, String tableName) {
        return dslContext.meta()
                .getTables()
                .stream()
                .filter(table -> matchesSchema(table, schemaName))
                .filter(table -> table.getName().equalsIgnoreCase(tableName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown table " + schemaName + "." + tableName
                ));
    }

    private boolean matchesSchema(Table<?> table, String schemaName) {
        if (table.getSchema() == null || table.getSchema().getName() == null) {
            return false;
        }
        return table.getSchema().getName().equalsIgnoreCase(schemaName);
    }

    private RestQueryModel asRestModel(QueryModel model) {
        if (model instanceof RestQueryModel restModel) {
            return restModel;
        }
        throw new IllegalStateException("Unexpected query model: " + model.getClass().getName());
    }
}
