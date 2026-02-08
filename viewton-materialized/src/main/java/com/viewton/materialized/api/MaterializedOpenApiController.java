package com.viewton.materialized.api;

import com.viewton.materialized.openapi.MaterializedOpenApiBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint that returns an OpenAPI specification for a specific table.
 */
@RestController
@RequestMapping("/openapi")
public class MaterializedOpenApiController {
    private final MaterializedOpenApiBuilder openApiBuilder;

    public MaterializedOpenApiController(MaterializedOpenApiBuilder openApiBuilder) {
        this.openApiBuilder = openApiBuilder;
    }

    @GetMapping("/{schema}/{table}")
    public OpenAPI describe(
            @PathVariable("schema") String schema,
            @PathVariable("table") String table
    ) {
        return openApiBuilder.buildForTable(schema, table);
    }
}
