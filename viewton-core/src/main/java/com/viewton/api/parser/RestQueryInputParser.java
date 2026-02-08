package com.viewton.api.parser;

import com.viewton.api.input.RestQueryInput;
import com.viewton.model.FilterCriterion;
import com.viewton.model.FilterOperator;
import com.viewton.model.QueryModel;
import com.viewton.model.RestQueryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Parses REST-style query parameters into a {@link RestQueryModel}.
 */
public final class RestQueryInputParser implements QueryInputParser<RestQueryInput> {
    private static final Set<String> RESERVED_KEYS = Set.of(
            "page",
            "pageSize",
            "page_size",
            "count",
            "distinct",
            "entities",
            "attributes",
            "sum",
            "avg",
            "min",
            "max",
            "sorting"
    );

    @Override
    public QueryModel parse(RestQueryInput input) {
        Objects.requireNonNull(input, "input");
        Map<String, String> params = Objects.requireNonNullElse(input.getParameters(), Map.of());

        Integer page = parseInteger(params.get("page"));
        Integer pageSize = parseInteger(firstNonNull(params.get("pageSize"), params.get("page_size")));
        boolean count = parseBoolean(params.get("count"));
        boolean distinct = parseBoolean(params.get("distinct"));
        boolean entities = parseBoolean(params.get("entities"), true);
        List<String> attributes = parseList(params.get("attributes"));
        List<String> sum = parseList(params.get("sum"));
        List<String> avg = parseList(params.get("avg"));
        List<String> min = parseList(params.get("min"));
        List<String> max = parseList(params.get("max"));
        List<String> sorting = parseList(params.get("sorting"));
        List<FilterCriterion> filters = parseFilters(params);

        return new RestQueryModel(
                page,
                pageSize,
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

    private static List<FilterCriterion> parseFilters(Map<String, String> params) {
        List<FilterCriterion> filters = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (RESERVED_KEYS.contains(entry.getKey())) {
                continue;
            }
            String rawValue = entry.getValue();
            if (rawValue == null) {
                continue;
            }
            filters.add(parseFilter(entry.getKey(), rawValue));
        }
        return filters;
    }

    private static FilterCriterion parseFilter(String field, String rawValue) {
        boolean ignoreCase = rawValue.startsWith("^");
        String value = ignoreCase ? rawValue.substring(1) : rawValue;

        if (value.contains("..")) {
            String[] bounds = value.split("\\.\\.", 2);
            List<String> values = List.of(bounds[0], bounds.length > 1 ? bounds[1] : "");
            return new FilterCriterion(field, FilterOperator.BETWEEN, values, ignoreCase);
        }

        if (value.startsWith(">=")) {
            return new FilterCriterion(field, FilterOperator.GTE, List.of(value.substring(2)), ignoreCase);
        }
        if (value.startsWith("<=")) {
            return new FilterCriterion(field, FilterOperator.LTE, List.of(value.substring(2)), ignoreCase);
        }
        if (value.startsWith("!=")) {
            return new FilterCriterion(field, FilterOperator.NEQ, List.of(value.substring(2)), ignoreCase);
        }
        if (value.startsWith(">")) {
            return new FilterCriterion(field, FilterOperator.GT, List.of(value.substring(1)), ignoreCase);
        }
        if (value.startsWith("<")) {
            return new FilterCriterion(field, FilterOperator.LT, List.of(value.substring(1)), ignoreCase);
        }
        if (value.startsWith("=")) {
            return new FilterCriterion(field, FilterOperator.EQ, List.of(value.substring(1)), ignoreCase);
        }
        if (value.contains("%") || value.contains("_")) {
            return new FilterCriterion(field, FilterOperator.LIKE, List.of(value), ignoreCase);
        }

        return new FilterCriterion(field, FilterOperator.EQ, List.of(value), ignoreCase);
    }

    private static List<String> parseList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        String[] parts = value.split(",");
        List<String> results = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                results.add(trimmed);
            }
        }
        return results;
    }

    private static boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }

    private static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return parseBoolean(value);
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }
}
