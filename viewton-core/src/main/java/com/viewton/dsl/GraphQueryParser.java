package com.viewton.dsl;

import com.viewton.model.graph.GraphFilterCriterion;
import com.viewton.model.graph.GraphOperator;
import com.viewton.model.graph.GraphOrderBy;
import com.viewton.model.graph.GraphPagination;
import com.viewton.model.graph.GraphQueryModel;
import com.viewton.model.graph.GraphValue;
import com.viewton.model.graph.GraphValueType;
import com.viewton.model.graph.value.GraphBooleanValue;
import com.viewton.model.graph.value.GraphEnumValue;
import com.viewton.model.graph.value.GraphListValue;
import com.viewton.model.graph.value.GraphNumberValue;
import com.viewton.model.graph.value.GraphObjectValue;
import com.viewton.model.graph.value.GraphStringValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Parser for the Graph-like query DSL.
 */
public final class GraphQueryParser {
    private final GraphTokenizer tokenizer;
    private GraphToken current;

    public GraphQueryParser(String input) {
        this.tokenizer = new GraphTokenizer(Objects.requireNonNull(input, "input"));
        this.current = tokenizer.nextToken();
    }

    public static GraphQueryModel parse(String input) {
        return new GraphQueryParser(input).parseDocument();
    }

    private GraphQueryModel parseDocument() {
        if (matchIdentifier("query")) {
            consume();
        }
        expect(GraphTokenType.LBRACE);
        String resource = expectIdentifier();
        Map<String, GraphValue> arguments = Map.of();
        if (match(GraphTokenType.LPAREN)) {
            consume();
            arguments = parseArguments();
            expect(GraphTokenType.RPAREN);
        }
        expect(GraphTokenType.LBRACE);
        List<String> selections = parseSelections();
        expect(GraphTokenType.RBRACE);
        expect(GraphTokenType.RBRACE);
        expect(GraphTokenType.EOF);

        return buildModel(resource, selections, arguments);
    }

    private Map<String, GraphValue> parseArguments() {
        Map<String, GraphValue> args = new LinkedHashMap<>();
        while (!match(GraphTokenType.RPAREN)) {
            String name = expectIdentifier();
            expect(GraphTokenType.COLON);
            GraphValue value = parseValue();
            args.put(name, value);
            if (match(GraphTokenType.COMMA)) {
                consume();
            }
        }
        return args;
    }

    private List<String> parseSelections() {
        List<String> selections = new ArrayList<>();
        while (!match(GraphTokenType.RBRACE)) {
            selections.add(expectIdentifier());
            if (match(GraphTokenType.COMMA)) {
                consume();
            }
        }
        return selections;
    }

    private GraphValue parseValue() {
        if (match(GraphTokenType.LBRACE)) {
            consume();
            Map<String, GraphValue> map = new LinkedHashMap<>();
            while (!match(GraphTokenType.RBRACE)) {
                String key = expectIdentifier();
                expect(GraphTokenType.COLON);
                GraphValue value = parseValue();
                map.put(key, value);
                if (match(GraphTokenType.COMMA)) {
                    consume();
                }
            }
            expect(GraphTokenType.RBRACE);
            return new GraphObjectValue(map);
        }
        if (match(GraphTokenType.LBRACKET)) {
            consume();
            List<GraphValue> list = new ArrayList<>();
            while (!match(GraphTokenType.RBRACKET)) {
                list.add(parseValue());
                if (match(GraphTokenType.COMMA)) {
                    consume();
                }
            }
            expect(GraphTokenType.RBRACKET);
            return new GraphListValue(list);
        }
        if (match(GraphTokenType.STRING)) {
            String value = current.getText();
            consume();
            return new GraphStringValue(value);
        }
        if (match(GraphTokenType.NUMBER)) {
            String value = current.getText();
            consume();
            return new GraphNumberValue(new BigDecimal(value));
        }
        if (match(GraphTokenType.IDENTIFIER)) {
            String identifier = current.getText();
            consume();
            if ("true".equalsIgnoreCase(identifier) || "false".equalsIgnoreCase(identifier)) {
                return new GraphBooleanValue(Boolean.parseBoolean(identifier));
            }
            return new GraphEnumValue(identifier);
        }
        throw new IllegalArgumentException("Unexpected token: " + current.getType());
    }

    private GraphQueryModel buildModel(String resource, List<String> selections, Map<String, GraphValue> arguments) {
        GraphPagination pagination = null;
        boolean distinct = false;
        boolean count = false;
        List<String> sumFields = new ArrayList<>();
        List<GraphOrderBy> orderBy = new ArrayList<>();
        List<GraphFilterCriterion> filters = new ArrayList<>();

        for (Map.Entry<String, GraphValue> entry : arguments.entrySet()) {
            String name = entry.getKey();
            GraphValue value = entry.getValue();
            switch (name) {
                case "pagination" -> pagination = parsePagination(value);
                case "distinct" -> distinct = asBoolean(value, "distinct");
                case "count" -> count = asBoolean(value, "count");
                case "sum" -> sumFields = parseIdentifierList(value, "sum");
                case "orderBy" -> orderBy = parseOrderBy(value);
                case "where" -> filters = parseFilters(value);
                default -> throw new IllegalArgumentException("Unsupported argument: " + name);
            }
        }

        return new GraphQueryModel(resource, selections, pagination, distinct, count, sumFields, orderBy, filters);
    }

    private GraphPagination parsePagination(GraphValue value) {
        Map<String, GraphValue> map = asObject(value, "pagination");
        Integer page = null;
        Integer pageSize = null;
        if (map.containsKey("page")) {
            page = asInteger(map.get("page"), "pagination.page");
        }
        if (map.containsKey("pageSize")) {
            pageSize = asInteger(map.get("pageSize"), "pagination.pageSize");
        }
        return new GraphPagination(page, pageSize);
    }

    private List<String> parseIdentifierList(GraphValue value, String field) {
        if (value.getType() != GraphValueType.LIST) {
            throw new IllegalArgumentException(field + " must be a list");
        }
        GraphListValue listValue = (GraphListValue) value;
        List<String> results = new ArrayList<>();
        for (GraphValue item : listValue.getValues()) {
            results.add(asIdentifier(item, field));
        }
        return results;
    }

    private List<GraphOrderBy> parseOrderBy(GraphValue value) {
        if (value.getType() != GraphValueType.LIST) {
            throw new IllegalArgumentException("orderBy must be a list");
        }
        GraphListValue listValue = (GraphListValue) value;
        List<GraphOrderBy> results = new ArrayList<>();
        for (GraphValue item : listValue.getValues()) {
            Map<String, GraphValue> map = asObject(item, "orderBy");
            String field = asIdentifier(require(map, "field", "orderBy.field"), "orderBy.field");
            String directionRaw = asIdentifier(map.getOrDefault("direction", new GraphEnumValue("ASC")), "orderBy.direction");
            GraphOrderBy.Direction direction = GraphOrderBy.Direction.valueOf(directionRaw.toUpperCase(Locale.ROOT));
            results.add(new GraphOrderBy(field, direction));
        }
        return results;
    }

    private List<GraphFilterCriterion> parseFilters(GraphValue value) {
        Map<String, GraphValue> map = asObject(value, "where");
        List<GraphFilterCriterion> results = new ArrayList<>();
        for (Map.Entry<String, GraphValue> entry : map.entrySet()) {
            String field = entry.getKey();
            Map<String, GraphValue> operatorMap = asObject(entry.getValue(), "where." + field);
            for (Map.Entry<String, GraphValue> operatorEntry : operatorMap.entrySet()) {
                GraphOperator operator = GraphOperator.fromKey(operatorEntry.getKey());
                results.add(new GraphFilterCriterion(field, operator, operatorEntry.getValue()));
            }
        }
        return results;
    }

    private Map<String, GraphValue> asObject(GraphValue value, String field) {
        if (value.getType() != GraphValueType.OBJECT) {
            throw new IllegalArgumentException(field + " must be an object");
        }
        return ((GraphObjectValue) value).getValues();
    }

    private boolean asBoolean(GraphValue value, String field) {
        if (value.getType() == GraphValueType.BOOLEAN) {
            return value.asBoolean();
        }
        throw new IllegalArgumentException(field + " must be boolean");
    }

    private Integer asInteger(GraphValue value, String field) {
        if (value.getType() == GraphValueType.NUMBER) {
            return value.asInteger();
        }
        throw new IllegalArgumentException(field + " must be numeric");
    }

    private String asIdentifier(GraphValue value, String field) {
        if (value.getType() == GraphValueType.STRING || value.getType() == GraphValueType.ENUM) {
            return value.asIdentifier();
        }
        throw new IllegalArgumentException(field + " must be identifier or string");
    }

    private GraphValue require(Map<String, GraphValue> map, String key, String field) {
        GraphValue value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private void expect(GraphTokenType type) {
        if (current.getType() != type) {
            throw new IllegalArgumentException("Expected " + type + " but got " + current.getType());
        }
        consume();
    }

    private String expectIdentifier() {
        if (!match(GraphTokenType.IDENTIFIER)) {
            throw new IllegalArgumentException("Expected identifier but got " + current.getType());
        }
        String value = current.getText();
        consume();
        return value;
    }

    private boolean match(GraphTokenType type) {
        return current.getType() == type;
    }

    private boolean matchIdentifier(String identifier) {
        return current.getType() == GraphTokenType.IDENTIFIER && identifier.equals(current.getText());
    }

    private void consume() {
        current = tokenizer.nextToken();
    }
}
