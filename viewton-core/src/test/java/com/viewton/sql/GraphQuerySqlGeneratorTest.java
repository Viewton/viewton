package com.viewton.sql;

import com.viewton.dsl.GraphQueryParser;
import com.viewton.model.graph.GraphQueryModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphQuerySqlGeneratorTest {

    @Test
    void buildsSqlFromGraphQueryModel() {
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
        GraphQuerySqlGenerator generator = new GraphQuerySqlGenerator();
        String sql = generator.generate(model, null).getSql();

        String expected = "SELECT DISTINCT currencyCode, paymentSum, rate, status, "
                + "SUM(paymentSum) AS sum_paymentSum, SUM(rate) AS sum_rate, COUNT(*) AS total_count "
                + "FROM payments WHERE userId = 111 AND userEmail = 'someEmail@gmail.com' "
                + "AND paymentSum >= 1000 AND userName LIKE 'Some%' "
                + "AND LOWER(authorEmail) = LOWER('ignoreCaseEmail@email.com') "
                + "ORDER BY conclusionDate DESC, id ASC LIMIT 50 OFFSET 0";

        assertEquals(expected, sql);
    }
}
