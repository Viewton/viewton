package com.viewton.jooq.executor;

import com.viewton.jooq.mapping.JooqRow;
import com.viewton.jooq.mapping.QueryResult;
import com.viewton.jooq.schema.JooqSchema;
import com.viewton.plan.FilterCriterion;
import com.viewton.plan.QueryPlan;
import com.viewton.plan.QueryValue;
import com.viewton.plan.SortDirection;
import com.viewton.plan.SortField;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectSeekStepN;
import org.jooq.Select;
import org.jooq.SelectLimitStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Executes {@link QueryPlan} instances using jOOQ.
 */
public final class JooqQueryExecutor {
    private final org.jooq.DSLContext dslContext;
    private final JooqSchema schema;

    public JooqQueryExecutor(org.jooq.DSLContext dslContext, JooqSchema schema) {
        this.dslContext = Objects.requireNonNull(dslContext, "dslContext");
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    public QueryResult execute(QueryPlan plan) {
        Objects.requireNonNull(plan, "plan");
        Table<?> table = resolveTable(plan.getEntity().name());

        List<JooqRow> rows = List.of();
        if (plan.getFlags().isEntities()) {
            List<SelectFieldOrAsterisk> selectFields = buildEntitySelectFields(plan, table);
            SelectConditionStep<Record> select = (plan.getFlags().isDistinct()
                    ? dslContext.selectDistinct(selectFields).from(table)
                    : dslContext.select(selectFields).from(table))
                    .where(buildCondition(plan, table));

            SelectLimitStep<Record> sorted = applySorting(select, plan, table);
            Select<Record> limited = applyPagination(sorted, plan);

            Result<Record> result = limited.fetch();
            rows = result.stream().map(JooqRow::new).toList();
        }

        Map<String, Object> aggregations = Map.of();
        if (requiresAggregations(plan)) {
            aggregations = runAggregations(plan, table);
        }
        return new QueryResult(rows, aggregations);
    }

    private Table<?> resolveTable(String entityName) {
        Table<?> table = schema.jooqTable(entityName);
        if (table == null) {
            throw new IllegalArgumentException("Unknown entity: " + entityName);
        }
        return table;
    }

    private List<SelectFieldOrAsterisk> buildEntitySelectFields(QueryPlan plan, Table<?> table) {
        List<SelectFieldOrAsterisk> fields = new ArrayList<>();
        if (!plan.getProjection().getFields().isEmpty()) {
            for (String fieldName : plan.getProjection().getFields()) {
                fields.add(resolveField(table, fieldName, Object.class));
            }
        }
        if (fields.isEmpty()) {
            fields.add(DSL.asterisk());
        }
        return fields;
    }

    private List<SelectFieldOrAsterisk> buildAggregationFields(QueryPlan plan, Table<?> table) {
        List<SelectFieldOrAsterisk> fields = new ArrayList<>();
        if (plan.getFlags().isCount()) {
            fields.add(DSL.count().as("count"));
        }
        for (String fieldName : plan.getAggregations().getSumFields()) {
            Field<Double> field = resolveField(table, fieldName, Double.class);
            fields.add(DSL.sum(field).as(fieldName + "_sum"));
        }
        return fields;
    }

    private boolean requiresAggregations(QueryPlan plan) {
        return plan.getFlags().isCount() || !plan.getAggregations().getSumFields().isEmpty();
    }

    private Map<String, Object> runAggregations(QueryPlan plan, Table<?> table) {
        List<SelectFieldOrAsterisk> aggregationFields = buildAggregationFields(plan, table);
        if (aggregationFields.isEmpty()) {
            return Map.of();
        }
        SelectConditionStep<Record> select = (plan.getFlags().isDistinct()
                ? dslContext.selectDistinct(aggregationFields).from(table)
                : dslContext.select(aggregationFields).from(table))
                .where(buildCondition(plan, table));
        Result<Record> result = select.fetch();
        if (result.isEmpty()) {
            return Map.of();
        }
        return new JooqRow(result.get(0)).asMap();
    }

    private Condition buildCondition(QueryPlan plan, Table<?> table) {
        Condition condition = DSL.trueCondition();
        for (FilterCriterion criterion : plan.getFilters().getCriteria()) {
            condition = condition.and(toCondition(table, criterion));
        }
        return condition;
    }

    private Condition toCondition(Table<?> table, FilterCriterion criterion) {
        Field<Object> field = resolveField(table, criterion.getField(), Object.class);
        List<QueryValue> values = criterion.getValues();
        boolean ignoreCase = criterion.isIgnoreCase();
        return switch (criterion.getOperator()) {
            case EQ -> compareEq(field, values.get(0), ignoreCase);
            case NEQ -> compareNeq(field, values.get(0), ignoreCase);
            case GT -> field.gt(values.get(0).getValue());
            case GTE -> field.ge(values.get(0).getValue());
            case LT -> field.lt(values.get(0).getValue());
            case LTE -> field.le(values.get(0).getValue());
            case BETWEEN -> between(field, values, ignoreCase);
            case LIKE -> compareLike(field, values.get(0), ignoreCase);
        };
    }

    private Condition between(Field<Object> field, List<QueryValue> values, boolean ignoreCase) {
        if (values.size() < 2) {
            throw new IllegalArgumentException("Between operator requires two values");
        }
        Object start = values.get(0).getValue();
        Object end = values.get(1).getValue();
        if (ignoreCase) {
            Field<String> lowered = lower(field);
            return lowered.between(stringValue(start), stringValue(end));
        }
        return field.between(start, end);
    }

    private Condition compareEq(Field<Object> field, QueryValue value, boolean ignoreCase) {
        if (ignoreCase) {
            Field<String> lowered = lower(field);
            String loweredValue = stringValue(value.getValue()).toLowerCase(Locale.ROOT);
            return lowered.eq(loweredValue);
        }
        return field.eq(value.getValue());
    }

    private Condition compareNeq(Field<Object> field, QueryValue value, boolean ignoreCase) {
        if (ignoreCase) {
            Field<String> lowered = lower(field);
            String loweredValue = stringValue(value.getValue()).toLowerCase(Locale.ROOT);
            return lowered.ne(loweredValue);
        }
        return field.ne(value.getValue());
    }

    private Condition compareLike(Field<Object> field, QueryValue value, boolean ignoreCase) {
        if (ignoreCase) {
            Field<String> lowered = lower(field);
            String loweredValue = stringValue(value.getValue()).toLowerCase(Locale.ROOT);
            return lowered.like(loweredValue);
        }
        return field.like(stringValue(value.getValue()));
    }

    private Field<String> lower(Field<Object> field) {
        return DSL.lower(field.cast(String.class));
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private SelectLimitStep<Record> applySorting(
            SelectConditionStep<Record> select,
            QueryPlan plan,
            Table<?> table
    ) {
        List<SortField> sortFields = plan.getSorting().getFields();
        if (sortFields.isEmpty()) {
            return select;
        }
        List<org.jooq.SortField<?>> orderBy = new ArrayList<>();
        for (SortField sortField : sortFields) {
            Field<Object> field = resolveField(table, sortField.getField(), Object.class);
            orderBy.add(sortField.getDirection() == SortDirection.ASC ? field.asc() : field.desc());
        }
        SelectSeekStepN<Record> ordered = select.orderBy(orderBy);
        return ordered;
    }

    private Select<Record> applyPagination(SelectLimitStep<Record> select, QueryPlan plan) {
        Integer pageSize = plan.getPagination().getPageSize();
        Integer page = plan.getPagination().getPage();
        if (pageSize == null) {
            return select;
        }
        int limit = Math.max(pageSize, 0);
        if (page == null || page <= 1) {
            return select.limit(limit);
        }
        int offset = (page - 1) * limit;
        return select.limit(limit).offset(offset);
    }

    private <T> Field<T> resolveField(Table<?> table, String fieldName, Class<T> type) {
        Field<T> field = table.field(fieldName, type);
        if (field == null) {
            throw new IllegalArgumentException("Unknown field: " + fieldName);
        }
        return field;
    }

}
