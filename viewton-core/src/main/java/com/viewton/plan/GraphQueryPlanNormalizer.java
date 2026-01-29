package com.viewton.plan;

import com.viewton.model.graph.GraphFilterCriterion;
import com.viewton.model.graph.GraphOperator;
import com.viewton.model.graph.GraphOrderBy;
import com.viewton.model.graph.GraphPagination;
import com.viewton.model.graph.GraphQueryModel;
import com.viewton.model.graph.GraphValue;
import com.viewton.model.graph.GraphValueType;
import com.viewton.model.graph.value.GraphBooleanValue;
import com.viewton.model.graph.value.GraphEnumValue;
import com.viewton.model.graph.value.GraphNumberValue;
import com.viewton.model.graph.value.GraphStringValue;
import com.viewton.schema.Schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Normalizes Graph-like queries into query plans.
 */
public final class GraphQueryPlanNormalizer {

    public QueryPlan normalize(GraphQueryModel model, Schema schema) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(schema, "schema");
        String entityName = model.getResource();
        validateEntity(schema, entityName);

        Projection projection = new Projection(model.getSelections());
        Aggregations aggregations = new Aggregations(model.getSumFields());
        Sorting sorting = new Sorting(toSortFields(model.getOrderBy()));
        Pagination pagination = toPagination(model.getPagination());
        Filters filters = new Filters(toFilterCriteria(model.getFilters()));
        QueryFlags flags = new QueryFlags(model.isCount(), model.isDistinct());

        return new QueryPlan(new EntityRef(entityName), projection, filters, sorting, pagination, aggregations, flags);
    }

    private void validateEntity(Schema schema, String entityName) {
        if (schema.table(entityName) == null) {
            throw new IllegalArgumentException("Unknown entity: " + entityName);
        }
    }

    private Pagination toPagination(GraphPagination pagination) {
        if (pagination == null) {
            return new Pagination(null, null);
        }
        return new Pagination(pagination.getPage(), pagination.getPageSize());
    }

    private List<SortField> toSortFields(List<GraphOrderBy> orderBy) {
        List<SortField> fields = new ArrayList<>();
        for (GraphOrderBy entry : orderBy) {
            fields.add(new SortField(entry.getField(), SortDirection.valueOf(entry.getDirection().name())));
        }
        return fields;
    }

    private List<FilterCriterion> toFilterCriteria(List<GraphFilterCriterion> filters) {
        List<FilterCriterion> results = new ArrayList<>();
        for (GraphFilterCriterion filter : filters) {
            QueryOperator operator = toOperator(filter.getOperator());
            boolean ignoreCase = filter.getOperator() == GraphOperator.EQ_IGNORE_CASE;
            List<QueryValue> values = List.of(toValue(filter.getValue()));
            results.add(new FilterCriterion(filter.getField(), operator, values, ignoreCase));
        }
        return results;
    }

    private QueryOperator toOperator(GraphOperator operator) {
        return switch (operator) {
            case EQ, EQ_IGNORE_CASE -> QueryOperator.EQ;
            case NEQ -> QueryOperator.NEQ;
            case GT -> QueryOperator.GT;
            case GTE -> QueryOperator.GTE;
            case LT -> QueryOperator.LT;
            case LTE -> QueryOperator.LTE;
            case LIKE -> QueryOperator.LIKE;
        };
    }

    private QueryValue toValue(GraphValue value) {
        if (value.getType() == GraphValueType.NUMBER) {
            BigDecimal number = (BigDecimal) ((GraphNumberValue) value).getRawValue();
            return new com.viewton.plan.value.QueryNumberValue(number);
        }
        if (value.getType() == GraphValueType.BOOLEAN) {
            boolean bool = (Boolean) ((GraphBooleanValue) value).getRawValue();
            return new com.viewton.plan.value.QueryBooleanValue(bool);
        }
        if (value.getType() == GraphValueType.STRING) {
            String text = (String) ((GraphStringValue) value).getRawValue();
            return new com.viewton.plan.value.QueryStringValue(text);
        }
        if (value.getType() == GraphValueType.ENUM) {
            String text = (String) ((GraphEnumValue) value).getRawValue();
            return new com.viewton.plan.value.QueryStringValue(text);
        }
        throw new IllegalArgumentException("Unsupported value type: " + value.getType());
    }
}
