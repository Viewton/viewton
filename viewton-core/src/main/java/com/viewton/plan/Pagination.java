package com.viewton.plan;

/**
 * Pagination settings for a query plan.
 */
public final class Pagination {
    private final Integer page;
    private final Integer pageSize;

    public Pagination(Integer page, Integer pageSize) {
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
