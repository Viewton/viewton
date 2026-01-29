package com.viewton.api.parser;

import com.viewton.api.input.RestQueryInput;
import com.viewton.model.FilterCriterion;
import com.viewton.model.FilterOperator;
import com.viewton.model.RestQueryModel;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestQueryInputParserTest {

    @Test
    void parsesReservedParametersAndFilters() {
        Map<String, String> params = new HashMap<>();
        params.put("page", "2");
        params.put("pageSize", "25");
        params.put("count", "true");
        params.put("distinct", "1");
        params.put("attributes", "id, name , ,email");
        params.put("sum", "total, amount");
        params.put("sorting", "name,-createdAt");
        params.put("status", "active");
        params.put("ignored", null);

        RestQueryInputParser parser = new RestQueryInputParser();
        RestQueryModel model = (RestQueryModel) parser.parse(new RestQueryInput(params));

        assertAll(
                () -> assertEquals(2, model.getPage().orElseThrow()),
                () -> assertEquals(25, model.getPageSize().orElseThrow()),
                () -> assertTrue(model.isCount()),
                () -> assertTrue(model.isDistinct()),
                () -> assertEquals(List.of("id", "name", "email"), model.getAttributes()),
                () -> assertEquals(List.of("total", "amount"), model.getSum()),
                () -> assertEquals(List.of("name", "-createdAt"), model.getSorting()),
                () -> assertEquals(1, model.getFilters().size())
        );

        FilterCriterion filter = model.getFilters().get(0);
        assertAll(
                () -> assertEquals("status", filter.getField()),
                () -> assertEquals(FilterOperator.EQ, filter.getOperator()),
                () -> assertEquals(List.of("active"), filter.getValues()),
                () -> assertFalse(filter.isIgnoreCase())
        );
    }

    @Test
    void parsesFilterOperatorsAndIgnoreCase() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("age", ">=18");
        params.put("price", "5..10");
        params.put("code", "^ABC");
        params.put("note", "%foo");

        RestQueryInputParser parser = new RestQueryInputParser();
        RestQueryModel model = (RestQueryModel) parser.parse(new RestQueryInput(params));

        List<FilterCriterion> filters = model.getFilters();
        assertAll(
                () -> assertEquals(4, filters.size()),
                () -> assertEquals(FilterOperator.GTE, filters.get(0).getOperator()),
                () -> assertEquals(List.of("18"), filters.get(0).getValues()),
                () -> assertFalse(filters.get(0).isIgnoreCase()),
                () -> assertEquals(FilterOperator.BETWEEN, filters.get(1).getOperator()),
                () -> assertEquals(List.of("5", "10"), filters.get(1).getValues()),
                () -> assertFalse(filters.get(1).isIgnoreCase()),
                () -> assertEquals(FilterOperator.EQ, filters.get(2).getOperator()),
                () -> assertEquals(List.of("ABC"), filters.get(2).getValues()),
                () -> assertTrue(filters.get(2).isIgnoreCase()),
                () -> assertEquals(FilterOperator.LIKE, filters.get(3).getOperator()),
                () -> assertEquals(List.of("%foo"), filters.get(3).getValues())
        );
    }
}
