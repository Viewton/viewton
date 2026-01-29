package com.viewton.plan;

import com.viewton.api.input.RestQueryInput;
import com.viewton.api.parser.RestQueryInputParser;
import com.viewton.model.RestQueryModel;
import com.viewton.schema.Schema;
import com.viewton.schema.Table;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestQueryPlanNormalizerTest {

    @Test
    void normalizesRestQueryToPlan() {
        Map<String, String> params = new HashMap<>();
        params.put("pageSize", "50");
        params.put("page", "1");
        params.put("count", "true");
        params.put("distinct", "true");
        params.put("attributes", "currencyCode,paymentSum,rate,status");
        params.put("sum", "paymentSum,rate");
        params.put("sorting", "-conclusionDate,id");
        params.put("userId", "111");
        params.put("userEmail", "someEmail@gmail.com");
        params.put("paymentSum", ">=1000");
        params.put("userName", "Some%");
        params.put("authorEmail", "^ignoreCaseEmail@email.com");

        RestQueryModel model = (RestQueryModel) new RestQueryInputParser()
                .parse(new RestQueryInput(params));

        RestQueryPlanNormalizer normalizer = new RestQueryPlanNormalizer();
        QueryPlan plan = normalizer.normalize(model, "payments", new TestSchema());

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
        RestQueryModel model = (RestQueryModel) new RestQueryInputParser()
                .parse(new RestQueryInput(Map.of()));
        RestQueryPlanNormalizer normalizer = new RestQueryPlanNormalizer();

        assertThrows(IllegalArgumentException.class, () -> normalizer.normalize(model, "payments", new EmptySchema()));
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
