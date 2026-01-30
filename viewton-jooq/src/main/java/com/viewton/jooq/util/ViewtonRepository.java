package com.viewton.jooq.util;

import com.viewton.api.input.QueryInput;
import com.viewton.api.input.RestQueryInput;
import com.viewton.api.parser.RestQueryInputParser;
import com.viewton.jooq.executor.JooqQueryExecutor;
import com.viewton.jooq.mapping.DefaultResultMapper;
import com.viewton.jooq.mapping.QueryResult;
import com.viewton.jooq.schema.JooqSchema;
import com.viewton.model.QueryModel;
import com.viewton.model.RestQueryModel;
import com.viewton.plan.QueryPlan;
import com.viewton.plan.RestQueryPlanNormalizer;

import java.util.List;
import java.util.Objects;

/**
 * Convenience repository that parses query input and executes it via jOOQ.
 */
public final class ViewtonRepository {
    private final JooqSchema schema;
    private final RestQueryInputParser restQueryInputParser;
    private final RestQueryPlanNormalizer restQueryPlanNormalizer;
    private final JooqQueryExecutor executor;
    private final DefaultResultMapper resultMapper;

    public ViewtonRepository(
            JooqSchema schema,
            RestQueryInputParser restQueryInputParser,
            RestQueryPlanNormalizer restQueryPlanNormalizer,
            JooqQueryExecutor executor,
            DefaultResultMapper resultMapper
    ) {
        this.schema = Objects.requireNonNull(schema, "schema");
        this.restQueryInputParser = Objects.requireNonNull(restQueryInputParser, "restQueryInputParser");
        this.restQueryPlanNormalizer = Objects.requireNonNull(restQueryPlanNormalizer, "restQueryPlanNormalizer");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.resultMapper = Objects.requireNonNull(resultMapper, "resultMapper");
    }

    public <T> List<T> list(QueryInput input, Class<T> resultType) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(resultType, "resultType");
        QueryPlan plan = buildPlan(input, resultType);
        QueryResult result = executor.execute(plan);
        return resultMapper.map(result, resultType);
    }

    private QueryPlan buildPlan(QueryInput input, Class<?> resultType) {
        if (input instanceof RestQueryInput restQueryInput) {
            QueryModel model = restQueryInputParser.parse(restQueryInput);
            return restQueryPlanNormalizer.normalize(asRestModel(model), resolveEntity(resultType), schema);
        }
        throw new IllegalArgumentException("Unsupported query input type: " + input.getClass().getName());
    }

    private RestQueryModel asRestModel(QueryModel model) {
        if (model instanceof RestQueryModel restModel) {
            return restModel;
        }
        throw new IllegalStateException("Unexpected query model: " + model.getClass().getName());
    }

    private String resolveEntity(Class<?> resultType) {
        String entityName = schema.resolveEntityName(resultType);
        if (entityName == null) {
            throw new IllegalArgumentException("No entity mapping registered for " + resultType.getName());
        }
        return entityName;
    }
}
