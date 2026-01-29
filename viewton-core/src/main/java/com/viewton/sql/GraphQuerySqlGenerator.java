package com.viewton.sql;

import com.viewton.model.QueryModel;
import com.viewton.model.graph.GraphFilterCriterion;
import com.viewton.model.graph.GraphOperator;
import com.viewton.model.graph.GraphOrderBy;
import com.viewton.model.graph.GraphPagination;
import com.viewton.model.graph.GraphQueryModel;
import com.viewton.model.graph.GraphValue;
import com.viewton.schema.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * SQL generator for the Graph-like query DSL.
 */
public final class GraphQuerySqlGenerator implements SqlGenerator {
    @Override
    public SqlStatement generate(QueryModel model, Schema schema) {
        if (!(model instanceof GraphQueryModel graphQuery)) {
            throw new IllegalArgumentException("GraphQuerySqlGenerator supports GraphQueryModel only");
        }
        String sql = buildSql(graphQuery);
        return new SqlStatement(sql);
    }

    private String buildSql(GraphQueryModel query) {
        List<String> selections = new ArrayList<>(query.getSelections());
        for (String sumField : query.getSumFields()) {
            selections.add("SUM(" + sumField + ") AS sum_" + sumField);
        }
        if (query.isCount()) {
            selections.add("COUNT(*) AS total_count");
        }
        if (selections.isEmpty()) {
            selections.add("*");
        }

        StringBuilder builder = new StringBuilder("SELECT ");
        if (query.isDistinct()) {
            builder.append("DISTINCT ");
        }
        builder.append(String.join(", ", selections));
        builder.append(" FROM ").append(query.getResource());

        if (!query.getFilters().isEmpty()) {
            builder.append(" WHERE ");
            builder.append(renderFilters(query.getFilters()));
        }

        if (!query.getOrderBy().isEmpty()) {
            builder.append(" ORDER BY ");
            builder.append(renderOrderBy(query.getOrderBy()));
        }

        GraphPagination pagination = query.getPagination();
        if (pagination != null && pagination.getPageSize() != null) {
            builder.append(" LIMIT ").append(pagination.getPageSize());
            if (pagination.getPage() != null) {
                int offset = Math.max(0, pagination.getPage() - 1) * pagination.getPageSize();
                builder.append(" OFFSET ").append(offset);
            }
        }

        return builder.toString();
    }

    private String renderFilters(List<GraphFilterCriterion> filters) {
        StringJoiner joiner = new StringJoiner(" AND ");
        for (GraphFilterCriterion filter : filters) {
            GraphOperator operator = filter.getOperator();
            String valueLiteral = renderValue(filter.getValue());
            if (operator.isIgnoreCase()) {
                joiner.add("LOWER(" + filter.getField() + ") " + operator.getSqlOperator()
                        + " LOWER(" + valueLiteral + ")");
            } else {
                joiner.add(filter.getField() + " " + operator.getSqlOperator() + " " + valueLiteral);
            }
        }
        return joiner.toString();
    }

    private String renderOrderBy(List<GraphOrderBy> orderBy) {
        StringJoiner joiner = new StringJoiner(", ");
        for (GraphOrderBy item : orderBy) {
            joiner.add(item.getField() + " " + item.getDirection());
        }
        return joiner.toString();
    }

    private String renderValue(GraphValue value) {
        return value.toSqlLiteral();
    }
}
