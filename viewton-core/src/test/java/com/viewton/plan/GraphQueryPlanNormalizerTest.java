package com.viewton.plan;

import com.viewton.dsl.GraphQueryParser;
import com.viewton.model.graph.GraphQueryModel;
import com.viewton.schema.Schema;
import com.viewton.schema.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraphQueryPlanNormalizerTest {

    @Test
    void normalizesGraphQueryToPlan() {
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
        GraphQueryPlanNormalizer normalizer = new GraphQueryPlanNormalizer();
        QueryPlan plan = normalizer.normalize(model, new TestSchema());

        assertAll(
                () -> assertEquals("payments", plan.getEntity().name()),
                () -> assertEquals(List.of("currencyCode", "paymentSum", "rate", "status"), plan.getProjection().getFields()),
                () -> assertEquals(List.of("paymentSum", "rate"), plan.getAggregations().getSumFields()),
                () -> assertEquals(2, plan.getSorting().getFields().size()),
                () -> assertEquals(5, plan.getFilters().getCriteria().size()),
                () -> assertEquals(true, plan.getFlags().isDistinct()),
                () -> assertEquals(true, plan.getFlags().isCount())
        );

        FilterCriterion ignoreCase = plan.getFilters().getCriteria().get(4);
        assertAll(
                () -> assertEquals("authorEmail", ignoreCase.getField()),
                () -> assertEquals(QueryOperator.EQ, ignoreCase.getOperator()),
                () -> assertEquals(true, ignoreCase.isIgnoreCase())
        );
    }

    @Test
    void throwsWhenEntityIsUnknown() {
        GraphQueryModel model = GraphQueryParser.parse("query { payments { id } }");
        GraphQueryPlanNormalizer normalizer = new GraphQueryPlanNormalizer();

        assertThrows(IllegalArgumentException.class, () -> normalizer.normalize(model, new EmptySchema()));
    }

    private static final class TestSchema implements Schema {
        @Override
        public Table table(String name) {
            return "payments".equals(name) ? () -> {
            } : null;
        }
    }

    private static final class EmptySchema implements Schema {
        @Override
        public Table table(String name) {
            return null;
        }
    }
}
