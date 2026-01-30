package com.viewton.jooq.config;

import com.viewton.api.parser.RestQueryInputParser;
import com.viewton.jooq.executor.JooqQueryExecutor;
import com.viewton.jooq.mapping.DefaultResultMapper;
import com.viewton.jooq.schema.JooqSchema;
import com.viewton.jooq.util.ViewtonRepository;
import com.viewton.plan.RestQueryPlanNormalizer;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Viewton's jOOQ integration.
 */
@AutoConfiguration
@ConditionalOnClass(DSLContext.class)
@ConditionalOnBean(DSLContext.class)
public class ViewtonJooqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestQueryInputParser restQueryInputParser() {
        return new RestQueryInputParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestQueryPlanNormalizer restQueryPlanNormalizer() {
        return new RestQueryPlanNormalizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public JooqSchema jooqSchema(DSLContext dslContext) {
        return JooqSchema.fromMeta(dslContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultResultMapper defaultResultMapper() {
        return new DefaultResultMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public JooqQueryExecutor jooqQueryExecutor(DSLContext dslContext, JooqSchema jooqSchema) {
        return new JooqQueryExecutor(dslContext, jooqSchema);
    }

    @Bean
    @ConditionalOnMissingBean
    public ViewtonRepository viewtonRepository(
            JooqSchema jooqSchema,
            RestQueryInputParser restQueryInputParser,
            RestQueryPlanNormalizer restQueryPlanNormalizer,
            JooqQueryExecutor jooqQueryExecutor,
            DefaultResultMapper defaultResultMapper
    ) {
        return new ViewtonRepository(
                jooqSchema,
                restQueryInputParser,
                restQueryPlanNormalizer,
                jooqQueryExecutor,
                defaultResultMapper
        );
    }
}
