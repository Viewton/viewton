package com.viewton.materialized.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

/**
 * Declarative request payload that mimics GraphQL-style selection.
 */
public final class MaterializedDeclarativeQueryRequest {
    private String schema;
    private String table;
    private Integer page;
    private Integer pageSize;
    private Boolean count;
    private Boolean distinct;
    private Boolean entities;
    private List<String> attributes;
    private List<String> sum;
    private List<String> avg;
    private List<String> min;
    private List<String> max;
    private List<String> sorting;
    private List<FilterInput> filters;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    @JsonAlias("page_size")
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getCount() {
        return count;
    }

    public void setCount(Boolean count) {
        this.count = count;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    public Boolean getEntities() {
        return entities;
    }

    public void setEntities(Boolean entities) {
        this.entities = entities;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    @JsonAlias("fields")
    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public List<String> getSum() {
        return sum;
    }

    public void setSum(List<String> sum) {
        this.sum = sum;
    }

    public List<String> getAvg() {
        return avg;
    }

    public void setAvg(List<String> avg) {
        this.avg = avg;
    }

    public List<String> getMin() {
        return min;
    }

    public void setMin(List<String> min) {
        this.min = min;
    }

    public List<String> getMax() {
        return max;
    }

    public void setMax(List<String> max) {
        this.max = max;
    }

    public List<String> getSorting() {
        return sorting;
    }

    @JsonAlias({"orderBy", "order_by"})
    public void setSorting(List<String> sorting) {
        this.sorting = sorting;
    }

    public List<FilterInput> getFilters() {
        return filters;
    }

    @JsonAlias("where")
    public void setFilters(List<FilterInput> filters) {
        this.filters = filters;
    }

    public static final class FilterInput {
        private String field;
        private String op;
        private Object value;
        private Boolean ignoreCase;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOp() {
            return op;
        }

        @JsonAlias("operator")
        public void setOp(String op) {
            this.op = op;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Boolean getIgnoreCase() {
            return ignoreCase;
        }

        @JsonAlias("ignore_case")
        public void setIgnoreCase(Boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
        }
    }
}
