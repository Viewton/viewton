package com.viewton.jooq;

import com.viewton.api.input.RestQueryInput;
import com.viewton.jooq.schema.JooqSchema;
import com.viewton.jooq.util.ViewtonRepository;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        ViewtonJooqIntegrationTest.TestApplication.class,
        ViewtonJooqIntegrationTest.TestSchemaConfiguration.class
})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:viewton;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jooq.sql-dialect=H2",
        "spring.flyway.enabled=true"
})
class ViewtonJooqIntegrationTest {

    @Autowired
    private ViewtonRepository viewtonRepository;

    @Test
    void listReturnsFilteredResults() {
        Map<String, String> params = new HashMap<>();
        params.put("attributes", "id,amount,status,created_at");
        params.put("sorting", "-amount");
        params.put("pageSize", "2");
        params.put("page", "1");
        params.put("status", "PAID");
        params.put("amount", ">=100");

        List<PaymentDto> result = viewtonRepository.list(new RestQueryInput(params), PaymentDto.class);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(result.get(1).getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @SpringBootApplication
    static class TestApplication {
    }

    @TestConfiguration
    static class TestSchemaConfiguration {
        @Bean
        @Primary
        JooqSchema jooqSchema(DSLContext dslContext) {
            Table<?> payments = dslContext.meta().getTables().stream()
                    .filter(table -> table.getName().equalsIgnoreCase("payments"))
                    .findFirst()
                    .orElseThrow();
            return JooqSchema.builder()
                    .registerTable("payments", payments)
                    .mapDto(PaymentDto.class, "payments")
                    .build();
        }
    }

    static class PaymentDto {
        private Long id;
        private BigDecimal amount;
        private String status;
        private LocalDate created_at;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDate getCreated_at() {
            return created_at;
        }

        public void setCreated_at(LocalDate created_at) {
            this.created_at = created_at;
        }
    }
}
