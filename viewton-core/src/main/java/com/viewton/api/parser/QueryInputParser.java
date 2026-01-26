package com.viewton.api.parser;

import com.viewton.api.input.QueryInput;
import com.viewton.model.QueryModel;

/**
 * Converts external query input into the internal query model.
 */
public interface QueryInputParser<I extends QueryInput> {
    QueryModel parse(I input);
}
