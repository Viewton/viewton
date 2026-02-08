package com.viewton.materialized.service;

import com.viewton.api.input.RestQueryInput;
import com.viewton.api.parser.RestQueryInputParser;
import com.viewton.jooq.executor.JooqQueryExecutor;
import com.viewton.jooq.mapping.QueryResult;
import com.viewton.materialized.api.MaterializedDeclarativeQueryRequest;
import com.viewton.materialized.api.MaterializedQueryResponse;
import com.viewton.jooq.schema.JooqSchema;
import com.viewton.model.FilterCriterion;
import com.viewton.model.FilterOperator;
import com.viewton.model.QueryModel;
import com.viewton.model.RestQueryModel;
import com.viewton.plan.QueryPlan;
import com.viewton.plan.RestQueryPlanNormalizer;
import org.jooq.DSLContext;
import org.jooq.Table;

import java.util.Collection;
import java.util.List;
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

    public MaterializedQueryResponse query(MaterializedDeclarativeQueryRequest request) {
        Objects.requireNonNull(request, "request");
        String schemaName = Objects.requireNonNull(request.getSchema(), "schema");
        String tableName = Objects.requireNonNull(request.getTable(), "table");
        Table<?> table = resolveTable(schemaName, tableName);
        JooqSchema schema = JooqSchema.builder()
                .registerTable(tableName, table)
                .build();

        RestQueryModel model = toRestModel(request);
        QueryPlan plan = restQueryPlanNormalizer.normalize(model, tableName, schema);

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

    private RestQueryModel toRestModel(MaterializedDeclarativeQueryRequest request) {
        boolean count = Boolean.TRUE.equals(request.getCount());
        boolean distinct = Boolean.TRUE.equals(request.getDistinct());
        boolean entities = request.getEntities() == null || Boolean.TRUE.equals(request.getEntities());

        List<String> attributes = defaultList(request.getAttributes());
        List<String> sum = defaultList(request.getSum());
        List<String> avg = defaultList(request.getAvg());
        List<String> min = defaultList(request.getMin());
        List<String> max = defaultList(request.getMax());
        List<String> sorting = defaultList(request.getSorting());
        List<FilterCriterion> filters = toFilters(request.getFilters());

        return new RestQueryModel(
                request.getPage(),
                request.getPageSize(),
                count,
                distinct,
                entities,
                attributes,
                sum,
                avg,
                min,
                max,
                sorting,
                filters
        );
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private List<FilterCriterion> toFilters(List<MaterializedDeclarativeQueryRequest.FilterInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return List.of();
        }
        return inputs.stream()
                .map(this::toFilter)
                .toList();
    }

    private FilterCriterion toFilter(MaterializedDeclarativeQueryRequest.FilterInput input) {
        Objects.requireNonNull(input, "input");
        String field = Objects.requireNonNull(input.getField(), "field");
        String op = Objects.requireNonNullElse(input.getOp(), "eq");
        FilterOperator operator = parseOperator(op);
        boolean ignoreCase = Boolean.TRUE.equals(input.getIgnoreCase());
        List<String> values = toStringList(input.getValue());
        if (values.isEmpty()) {
            values = List.of("");
        }
        return new FilterCriterion(field, operator, values, ignoreCase);
    }

    private FilterOperator parseOperator(String op) {
        return switch (op.toLowerCase()) {
            case "eq", "=" -> FilterOperator.EQ;
            case "neq", "!=" -> FilterOperator.NEQ;
            case "gt", ">" -> FilterOperator.GT;
            case "gte", ">=" -> FilterOperator.GTE;
            case "lt", "<" -> FilterOperator.LT;
            case "lte", "<=" -> FilterOperator.LTE;
            case "between" -> FilterOperator.BETWEEN;
            case "like" -> FilterOperator.LIKE;
            default -> throw new IllegalArgumentException("Unsupported operator: " + op);
        };
    }

    private List<String> toStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::stringify)
                    .toList();
        }
        return List.of(stringify(value));
    }

    private String stringify(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
