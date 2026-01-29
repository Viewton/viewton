package com.viewton.model.graph;

/**
 * Pagination settings for the Graph-like query DSL.
 */
public final class GraphPagination {
    private final Integer page;
    private final Integer pageSize;

    public GraphPagination(Integer page, Integer pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }
}
