package org.example.productservice.infrastructure.config;

import org.bson.UuidRepresentation;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.Index;
import org.example.productservice.infrastructure.web.data.entity.VoucherEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MongoConfig{
    @Bean
    public MongoClientSettingsBuilderCustomizer uuidCustomizer() {
        return builder -> builder.uuidRepresentation(UuidRepresentation.STANDARD);
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    public ApplicationRunner ensureVoucherIndexes(MongoTemplate mongoTemplate) {
        return args -> mongoTemplate.indexOps(VoucherEntity.class)
                .ensureIndex(new Index().on("code", Sort.Direction.ASC).unique());
    }
}
