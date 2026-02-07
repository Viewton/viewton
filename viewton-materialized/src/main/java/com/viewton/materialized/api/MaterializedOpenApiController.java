package com.viewton.materialized.api;

import org.jooq.DataType;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generates an OpenAPI-like description based on database metadata.
 */
@RestController
@RequestMapping("/materialized-viewton/openapi")
public class MaterializedOpenApiController {
    private final DSLContext dslContext;

    public MaterializedOpenApiController(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @GetMapping("/{schema}/{table}")
    public Map<String, Object> describe(
            @PathVariable("schema") String schema,
            @PathVariable("table") String tableName
    ) {
        Table<?> table = resolveTable(schema, tableName);
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("openapi", "3.0.3");
        spec.put("info", Map.of(
                "title", "Viewton Materialized API",
                "version", "1.0.0",
                "description", "Dynamic query endpoint for " + schema + "." + tableName
        ));
        spec.put("paths", Map.of(
                "/materialized-viewton/{schema}/{table}", buildPathItem(schema, tableName, table)
        ));
        spec.put("components", Map.of(
                "schemas", Map.of(
                        tableName, buildTableSchema(table),
                        tableName + "Response", buildResponseSchema(table)
                )
        ));
        return spec;
    }

    private Map<String, Object> buildPathItem(String schema, String tableName, Table<?> table) {
        Map<String, Object> get = new LinkedHashMap<>();
        get.put("summary", "Query " + schema + "." + tableName);
        get.put("description", buildQueryDescription());
        get.put("parameters", buildParameters(table));
        get.put("responses", Map.of(
                "200", Map.of(
                        "description", "Query results",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of(
                                                "type", "array",
                                                "items", Map.of(
                                                        "$ref", "#/components/schemas/" + tableName + "Response"
                                                )
                                        )
                                )
                        )
                )
        ));
        return Map.of("get", get);
    }

    private String buildQueryDescription() {
        return String.join(" ",
                "Query parameters follow Viewton REST syntax.",
                "Filters can use operators: =, !=, >, >=, <, <=, between (..), like (%/_),",
                "and case-insensitive prefix (^).",
                "Aggregations use sum=<field1,field2> and return <field>_sum fields."
        );
    }

    private List<Map<String, Object>> buildParameters(Table<?> table) {
        List<Map<String, Object>> parameters = new ArrayList<>();
        parameters.add(pathParameter("schema", "Schema name"));
        parameters.add(pathParameter("table", "Table name"));

        parameters.add(queryParameter("page", "Page number (1-based)."));
        parameters.add(queryParameter("pageSize", "Page size (alias: page_size)."));
        parameters.add(queryParameter("page_size", "Page size (alias: pageSize)."));
        parameters.add(queryParameter("count", "Return count only."));
        parameters.add(queryParameter("distinct", "Return distinct rows."));
        parameters.add(queryParameter("attributes", "Comma-separated list of fields to select."));
        parameters.add(queryParameter("sum", "Comma-separated list of numeric fields to sum."));
        parameters.add(queryParameter("sorting", "Comma-separated list of fields to sort, prefix with '-' for DESC."));

        for (Field<?> field : table.fields()) {
            Map<String, Object> parameter = new LinkedHashMap<>();
            parameter.put("name", field.getName());
            parameter.put("in", "query");
            parameter.put("required", false);
            parameter.put("description", fieldDescription(field));
            parameter.put("schema", buildSchema(field.getDataType()));
            parameters.add(parameter);
        }
        return parameters;
    }

    private Map<String, Object> pathParameter(String name, String description) {
        Map<String, Object> parameter = new LinkedHashMap<>();
        parameter.put("name", name);
        parameter.put("in", "path");
        parameter.put("required", true);
        parameter.put("description", description);
        parameter.put("schema", Map.of("type", "string"));
        return parameter;
    }

    private Map<String, Object> queryParameter(String name, String description) {
        Map<String, Object> parameter = new LinkedHashMap<>();
        parameter.put("name", name);
        parameter.put("in", "query");
        parameter.put("required", false);
        parameter.put("description", description);
        parameter.put("schema", Map.of("type", "string"));
        return parameter;
    }

    private Map<String, Object> buildTableSchema(Table<?> table) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (Field<?> field : table.fields()) {
            properties.put(field.getName(), buildSchema(field.getDataType()));
            if (!field.getDataType().nullable()) {
                required.add(field.getName());
            }
        }
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }

    private Map<String, Object> buildResponseSchema(Table<?> table) {
        Map<String, Object> schema = buildTableSchema(table);
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) schema.get("properties"));
        for (Field<?> field : table.fields()) {
            if (field.getDataType().isNumeric()) {
                properties.put(field.getName() + "_sum", buildSchema(field.getDataType()));
            }
        }
        properties.put("count", Map.of("type", "integer", "format", "int64"));
        schema.put("properties", properties);
        return schema;
    }

    private Map<String, Object> buildSchema(DataType<?> dataType) {
        Map<String, Object> schema = new LinkedHashMap<>();
        Class<?> type = dataType.getType();
        if (type == null) {
            schema.put("type", "string");
            return schema;
        }
        if (type == Integer.class || type == Short.class || type == Byte.class) {
            schema.put("type", "integer");
            schema.put("format", "int32");
        } else if (type == Long.class) {
            schema.put("type", "integer");
            schema.put("format", "int64");
        } else if (type == Double.class || type == Float.class) {
            schema.put("type", "number");
            schema.put("format", "double");
        } else if (type == BigDecimal.class) {
            schema.put("type", "number");
            schema.put("format", "decimal");
        } else if (type == Boolean.class) {
            schema.put("type", "boolean");
        } else if (type == LocalDate.class) {
            schema.put("type", "string");
            schema.put("format", "date");
        } else if (type == LocalDateTime.class || type == OffsetDateTime.class || type == Instant.class) {
            schema.put("type", "string");
            schema.put("format", "date-time");
        } else {
            schema.put("type", "string");
        }

        if (dataType.length() != null && dataType.length() > 0) {
            schema.put("maxLength", dataType.length());
        }
        if (dataType.precision() != null && dataType.precision() > 0) {
            schema.put("x-precision", dataType.precision());
        }
        if (dataType.scale() != null && dataType.scale() > 0) {
            schema.put("x-scale", dataType.scale());
        }
        if (dataType.defaultValue() != null) {
            schema.put("default", dataType.defaultValue());
        }
        schema.put("nullable", dataType.nullable());
        return schema;
    }

    private String fieldDescription(Field<?> field) {
        StringBuilder builder = new StringBuilder();
        DataType<?> dataType = field.getDataType();
        builder.append("Type: ").append(dataType.getTypeName().toLowerCase(Locale.ROOT)).append(".");
        builder.append(" Nullable: ").append(dataType.nullable()).append(".");
        if (dataType.length() != null && dataType.length() > 0) {
            builder.append(" Max length: ").append(dataType.length()).append(".");
        }
        if (dataType.precision() != null && dataType.precision() > 0) {
            builder.append(" Precision: ").append(dataType.precision()).append(".");
        }
        if (dataType.scale() != null && dataType.scale() > 0) {
            builder.append(" Scale: ").append(dataType.scale()).append(".");
        }
        if (dataType.defaultValue() != null) {
            builder.append(" Default: ").append(dataType.defaultValue()).append(".");
        }
        return builder.toString();
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
}
