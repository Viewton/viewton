package com.viewton.materialized.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "viewton")
public class ViewtonProperties {

	private Set<String> allowedSchemas;

	public Set<String> getAllowedSchemas() {
		return allowedSchemas;
	}

	public void setAllowedSchemas(Set<String> allowedSchemas) {
		this.allowedSchemas = allowedSchemas;
	}
}