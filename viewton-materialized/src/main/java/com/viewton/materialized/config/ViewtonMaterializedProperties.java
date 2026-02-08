package com.viewton.materialized.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the materialized Viewton module.
 */
@ConfigurationProperties(prefix = "viewton")
public class ViewtonMaterializedProperties {
    /**
     * Allowed database schemas for materialized endpoints. Empty means allow all.
     */
    private List<String> allowedSchemas = new ArrayList<>();

    public List<String> getAllowedSchemas() {
        return allowedSchemas;
    }

    public void setAllowedSchemas(List<String> allowedSchemas) {
        this.allowedSchemas = allowedSchemas == null ? new ArrayList<>() : new ArrayList<>(allowedSchemas);
    }
}
