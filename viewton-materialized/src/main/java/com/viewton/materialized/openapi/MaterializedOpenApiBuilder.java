package com.viewton.materialized.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.Components;
import org.jooq.DataType;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;

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
import java.util.Objects;

/**
 * Builds an OpenAPI specification from database metadata.
 */
public final class MaterializedOpenApiBuilder {
    private final DSLContext dslContext;

    public MaterializedOpenApiBuilder(DSLContext dslContext) {
        this.dslContext = Objects.requireNonNull(dslContext, "dslContext");
    }

    public OpenAPI buildAllTables() {
        OpenAPI openAPI = baseOpenApi();
        Components components = new Components();
        Paths paths = new Paths();

        for (Table<?> table : dslContext.meta().getTables()) {
            String schemaName = table.getSchema() != null ? table.getSchema().getName() : "public";
            String tableName = table.getName();
            String schemaKey = schemaKey(schemaName, tableName);
            components.addSchemas(schemaKey, buildTableSchema(table));
            components.addSchemas(schemaKey + "Response", buildResponseSchema(table));
            String path = "/materialized-viewton/" + schemaName + "/" + tableName;
            paths.addPathItem(path, buildPathItem(schemaName, tableName, schemaKey, table));
        }

        openAPI.setComponents(components);
        openAPI.setPaths(paths);
        return openAPI;
    }

    public OpenAPI buildForTable(String schemaName, String tableName) {
        Table<?> table = resolveTable(schemaName, tableName);
        OpenAPI openAPI = baseOpenApi();
        Components components = new Components();
        String schemaKey = schemaKey(schemaName, tableName);
        components.addSchemas(schemaKey, buildTableSchema(table));
        components.addSchemas(schemaKey + "Response", buildResponseSchema(table));
        openAPI.setComponents(components);
        Paths paths = new Paths();
        String path = "/materialized-viewton/" + schemaName + "/" + tableName;
        paths.addPathItem(path, buildPathItem(schemaName, tableName, schemaKey, table));
        openAPI.setPaths(paths);
        return openAPI;
    }

    private OpenAPI baseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Viewton Materialized API")
                        .version("1.0.0")
                        .description("Dynamic query endpoint for Viewton materialized views"));
    }

    private PathItem buildPathItem(String schemaName, String tableName, String schemaKey, Table<?> table) {
        Operation operation = new Operation()
                .summary("Query " + schemaName + "." + tableName)
                .description(buildQueryDescription())
                .parameters(buildParameters(table))
                .responses(buildResponses(schemaKey));
        return new PathItem().get(operation);
    }

    private ApiResponses buildResponses(String schemaKey) {
        Schema<?> arraySchema = new ArraySchema()
                .items(new Schema<>().$ref("#/components/schemas/" + schemaKey + "Response"));
        ApiResponse response = new ApiResponse()
                .description("Query results")
                .content(new io.swagger.v3.oas.models.media.Content()
                        .addMediaType("application/json",
                                new io.swagger.v3.oas.models.media.MediaType().schema(arraySchema)));
        return new ApiResponses().addApiResponse("200", response);
    }

    private String buildQueryDescription() {
        return String.join(" ",
                "Query parameters follow Viewton REST syntax.",
                "Filters can use operators: =, !=, >, >=, <, <=, between (..), like (%/_),",
                "and case-insensitive prefix (^).",
                "Aggregations use sum=<field1,field2> and return <field>_sum fields."
        );
    }

    private List<Parameter> buildParameters(Table<?> table) {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(queryParameter("page", "Page number (1-based)."));
        parameters.add(queryParameter("pageSize", "Page size (alias: page_size)."));
        parameters.add(queryParameter("page_size", "Page size (alias: pageSize)."));
        parameters.add(queryParameter("count", "Return count only."));
        parameters.add(queryParameter("distinct", "Return distinct rows."));
        parameters.add(queryParameter("attributes", "Comma-separated list of fields to select."));
        parameters.add(queryParameter("sum", "Comma-separated list of numeric fields to sum."));
        parameters.add(queryParameter("sorting", "Comma-separated list of fields to sort, prefix with '-' for DESC."));

        for (Field<?> field : table.fields()) {
            Parameter parameter = new Parameter()
                    .name(field.getName())
                    .in("query")
                    .description(fieldDescription(field))
                    .schema(buildSchema(field.getDataType()));
            parameters.add(parameter);
        }
        return parameters;
    }

    private Parameter queryParameter(String name, String description) {
        return new Parameter()
                .name(name)
                .in("query")
                .description(description)
                .schema(new Schema<>().type("string"));
    }

    private Schema<?> buildTableSchema(Table<?> table) {
        ObjectSchema schema = new ObjectSchema();
        Map<String, Schema<?>> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (Field<?> field : table.fields()) {
            properties.put(field.getName(), buildSchema(field.getDataType()));
            if (!field.getDataType().nullable()) {
                required.add(field.getName());
            }
        }
        schema.setProperties(properties);
        if (!required.isEmpty()) {
            schema.setRequired(required);
        }
        return schema;
    }

    private Schema<?> buildResponseSchema(Table<?> table) {
        ObjectSchema schema = new ObjectSchema();
        Map<String, Schema<?>> properties = new LinkedHashMap<>();
        for (Field<?> field : table.fields()) {
            Schema<?> base = buildSchema(field.getDataType());
            properties.put(field.getName(), base);
            if (field.getDataType().isNumeric()) {
                properties.put(field.getName() + "_sum", buildSchema(field.getDataType()));
            }
        }
        properties.put("count", new Schema<>().type("integer").format("int64"));
        schema.setProperties(properties);
        return schema;
    }

    private Schema<?> buildSchema(DataType<?> dataType) {
        Schema<?> schema = new Schema<>();
        Class<?> type = dataType.getType();
        if (type == null) {
            schema.setType("string");
            return schema;
        }
        if (type == Integer.class || type == Short.class || type == Byte.class) {
            schema.setType("integer");
            schema.setFormat("int32");
        } else if (type == Long.class) {
            schema.setType("integer");
            schema.setFormat("int64");
        } else if (type == Double.class || type == Float.class) {
            schema.setType("number");
            schema.setFormat("double");
        } else if (type == BigDecimal.class) {
            schema.setType("number");
            schema.setFormat("decimal");
        } else if (type == Boolean.class) {
            schema.setType("boolean");
        } else if (type == LocalDate.class) {
            schema.setType("string");
            schema.setFormat("date");
        } else if (type == LocalDateTime.class || type == OffsetDateTime.class || type == Instant.class) {
            schema.setType("string");
            schema.setFormat("date-time");
        } else {
            schema.setType("string");
        }

        if (dataType.length() != null && dataType.length() > 0) {
            schema.setMaxLength(dataType.length());
        }
        Map<String, Object> extensions = new LinkedHashMap<>();
        if (dataType.precision() != null && dataType.precision() > 0) {
            extensions.put("x-precision", dataType.precision());
        }
        if (dataType.scale() != null && dataType.scale() > 0) {
            extensions.put("x-scale", dataType.scale());
        }
        if (!extensions.isEmpty()) {
            schema.setExtensions(extensions);
        }
        if (dataType.defaultValue() != null) {
            schema.setDefault(dataType.defaultValue());
        }
        schema.setNullable(dataType.nullable());
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

    private String schemaKey(String schemaName, String tableName) {
        return schemaName + "_" + tableName;
    }
}
