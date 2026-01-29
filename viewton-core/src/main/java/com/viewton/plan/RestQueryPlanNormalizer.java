package com.viewton.plan;

import com.viewton.model.FilterCriterion;
import com.viewton.model.FilterOperator;
import com.viewton.model.RestQueryModel;
import com.viewton.schema.Schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Normalizes REST query models into query plans.
 */
public final class RestQueryPlanNormalizer {

    public QueryPlan normalize(RestQueryModel model, String entityName, Schema schema) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(entityName, "entityName");
        Objects.requireNonNull(schema, "schema");
        validateEntity(schema, entityName);

        Projection projection = new Projection(model.getAttributes());
        Aggregations aggregations = new Aggregations(model.getSum());
        Sorting sorting = new Sorting(toSortFields(model.getSorting()));
        Pagination pagination = new Pagination(model.getPage().orElse(null), model.getPageSize().orElse(null));
        Filters filters = new Filters(toFilterCriteria(model.getFilters()));
        QueryFlags flags = new QueryFlags(model.isCount(), model.isDistinct());

        return new QueryPlan(new EntityRef(entityName), projection, filters, sorting, pagination, aggregations, flags);
    }

    private void validateEntity(Schema schema, String entityName) {
        if (schema.table(entityName) == null) {
            throw new IllegalArgumentException("Unknown entity: " + entityName);
        }
    }

    private List<SortField> toSortFields(List<String> sorting) {
        List<SortField> results = new ArrayList<>();
        for (String entry : sorting) {
            if (entry.startsWith("-")) {
                results.add(new SortField(entry.substring(1), SortDirection.DESC));
            } else {
                results.add(new SortField(entry, SortDirection.ASC));
            }
        }
        return results;
    }

    private List<com.viewton.plan.FilterCriterion> toFilterCriteria(
            List<com.viewton.model.FilterCriterion> filters
    ) {
        List<com.viewton.plan.FilterCriterion> results = new ArrayList<>();
        for (com.viewton.model.FilterCriterion filter : filters) {
            QueryOperator operator = toOperator(filter.getOperator());
            List<QueryValue> values = new ArrayList<>();
            for (String value : filter.getValues()) {
                values.add(toValue(value));
            }
            results.add(new com.viewton.plan.FilterCriterion(filter.getField(), operator, values, filter.isIgnoreCase()));
        }
        return results;
    }

    private QueryOperator toOperator(FilterOperator operator) {
        return switch (operator) {
            case EQ -> QueryOperator.EQ;
            case NEQ -> QueryOperator.NEQ;
            case GT -> QueryOperator.GT;
            case GTE -> QueryOperator.GTE;
            case LT -> QueryOperator.LT;
            case LTE -> QueryOperator.LTE;
            case BETWEEN -> QueryOperator.BETWEEN;
            case LIKE -> QueryOperator.LIKE;
        };
    }

    private QueryValue toValue(String value) {
        if (value == null) {
            return new com.viewton.plan.value.QueryStringValue("");
        }
        try {
            return new com.viewton.plan.value.QueryNumberValue(new BigDecimal(value));
        } catch (NumberFormatException ignored) {
            return new com.viewton.plan.value.QueryStringValue(value);
        }
    }
}
