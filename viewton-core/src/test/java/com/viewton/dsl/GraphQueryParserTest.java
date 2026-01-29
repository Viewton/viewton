package com.viewton.dsl;

import com.viewton.model.graph.GraphFilterCriterion;
import com.viewton.model.graph.GraphOperator;
import com.viewton.model.graph.GraphOrderBy;
import com.viewton.model.graph.GraphPagination;
import com.viewton.model.graph.GraphQueryModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GraphQueryParserTest {

    @Test
    void parsesGraphQueryIntoTypedModel() {
        String query = """
                query {
                  payments(
                    pagination: { page: 1, pageSize: 50 }
                    distinct: true
                    count: true
                    sum: [paymentSum, rate]
                    orderBy: [
                      { field: conclusionDate, direction: DESC }
                      { field: id, direction: ASC }
                    ]
                    where: {
                      userId: { eq: 111 }
                      userEmail: { eq: "someEmail@gmail.com" }
                      paymentSum: { gte: 1000 }
                      userName: { like: "Some%" }
                      authorEmail: { eqIgnoreCase: "ignoreCaseEmail@email.com" }
                    }
                  ) {
                    currencyCode
                    paymentSum
                    rate
                    status
                  }
                }
                """;

        GraphQueryModel model = GraphQueryParser.parse(query);

        assertAll(
                () -> assertEquals("payments", model.getResource()),
                () -> assertEquals(List.of("currencyCode", "paymentSum", "rate", "status"), model.getSelections()),
                () -> assertEquals(List.of("paymentSum", "rate"), model.getSumFields()),
                () -> assertEquals(2, model.getOrderBy().size()),
                () -> assertEquals(5, model.getFilters().size()),
                () -> assertEquals(true, model.isDistinct()),
                () -> assertEquals(true, model.isCount())
        );

        GraphPagination pagination = model.getPagination();
        assertNotNull(pagination);
        assertAll(
                () -> assertEquals(1, pagination.getPage()),
                () -> assertEquals(50, pagination.getPageSize())
        );

        GraphOrderBy firstOrder = model.getOrderBy().get(0);
        GraphOrderBy secondOrder = model.getOrderBy().get(1);
        assertAll(
                () -> assertEquals("conclusionDate", firstOrder.getField()),
                () -> assertEquals(GraphOrderBy.Direction.DESC, firstOrder.getDirection()),
                () -> assertEquals("id", secondOrder.getField()),
                () -> assertEquals(GraphOrderBy.Direction.ASC, secondOrder.getDirection())
        );

        GraphFilterCriterion ignoreCaseFilter = model.getFilters().get(4);
        assertAll(
                () -> assertEquals("authorEmail", ignoreCaseFilter.getField()),
                () -> assertEquals(GraphOperator.EQ_IGNORE_CASE, ignoreCaseFilter.getOperator()),
                () -> assertEquals("ignoreCaseEmail@email.com", ignoreCaseFilter.getValue().asIdentifier())
        );
    }
}
