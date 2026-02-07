package com.viewton.materialized.config;

import com.viewton.api.parser.RestQueryInputParser;
import com.viewton.jooq.executor.JooqQueryExecutor;
import com.viewton.materialized.api.MaterializedOpenApiController;
import com.viewton.materialized.api.MaterializedViewtonController;
import com.viewton.materialized.service.MaterializedViewtonService;
import com.viewton.plan.RestQueryPlanNormalizer;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the materialized Viewton module.
 */
@AutoConfiguration
@ConditionalOnClass({ DSLContext.class, JooqQueryExecutor.class })
public class ViewtonMaterializedAutoConfiguration {

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
    @ConditionalOnBean(DSLContext.class)
    public MaterializedViewtonService materializedViewtonService(
            DSLContext dslContext,
            RestQueryInputParser restQueryInputParser,
            RestQueryPlanNormalizer restQueryPlanNormalizer
    ) {
        return new MaterializedViewtonService(dslContext, restQueryInputParser, restQueryPlanNormalizer);
    }

    @Bean
    @ConditionalOnMissingBean
    public MaterializedViewtonController materializedViewtonController(
            MaterializedViewtonService materializedViewtonService
    ) {
        return new MaterializedViewtonController(materializedViewtonService);
    }

    @Bean
    @ConditionalOnMissingBean
    public MaterializedOpenApiController materializedOpenApiController(DSLContext dslContext) {
        return new MaterializedOpenApiController(dslContext);
    }
}
