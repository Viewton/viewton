package com.viewton.api.input;

import java.util.Map;

/**
 * REST-style query input backed by flat key/value parameters.
 */
public final class RestQueryInput implements QueryInput {
    private final Map<String, String> parameters;

    public RestQueryInput(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
