package com.viewton.materialized.api;

import com.viewton.materialized.service.MaterializedViewtonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST endpoint for dynamic Viewton reads from Postgres via jOOQ.
 */
@RestController
@RequestMapping("/materialized-viewton")
public class MaterializedViewtonController {
    private final MaterializedViewtonService service;

    public MaterializedViewtonController(MaterializedViewtonService service) {
        this.service = service;
    }

    @GetMapping("/{schema}/{table}")
    public List<Map<String, Object>> list(
            @PathVariable("schema") String schema,
            @PathVariable("table") String table,
            @RequestParam Map<String, String> parameters
    ) {
        return service.list(schema, table, parameters);
    }
}
