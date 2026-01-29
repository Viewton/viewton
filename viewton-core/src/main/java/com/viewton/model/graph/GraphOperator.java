package com.viewton.model.graph;

import java.util.Locale;

/**
 * Operators supported by the Graph-like query DSL.
 */
public enum GraphOperator {
    EQ("=", false),
    NEQ("!=", false),
    GT(">", false),
    GTE(">=", false),
    LT("<", false),
    LTE("<=", false),
    LIKE("LIKE", false),
    EQ_IGNORE_CASE("=", true);

    private final String sqlOperator;
    private final boolean ignoreCase;

    GraphOperator(String sqlOperator, boolean ignoreCase) {
        this.sqlOperator = sqlOperator;
        this.ignoreCase = ignoreCase;
    }

    public String getSqlOperator() {
        return sqlOperator;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public static GraphOperator fromKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "eq" -> EQ;
            case "neq" -> NEQ;
            case "gt" -> GT;
            case "gte" -> GTE;
            case "lt" -> LT;
            case "lte" -> LTE;
            case "like" -> LIKE;
            case "eqignorecase" -> EQ_IGNORE_CASE;
            default -> throw new IllegalArgumentException("Unsupported operator: " + key);
        };
    }
}
