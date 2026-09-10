package org.example.productservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.application.mapper.TransactionMapper;
import org.example.productservice.application.repository.TransactionRepository;
import org.example.productservice.domain.model.Transaction;
import org.example.productservice.infrastructure.web.data.entity.TransactionEntity;
import org.example.productservice.infrastructure.web.data.specification.TransactionSpecification;
import org.example.productservice.infrastructure.web.data.springdata.SpringDataTransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final SpringDataTransactionRepository springDataRepo;
    private final TransactionMapper transactionMapper;
    // MongoTemplate replaces JpaSpecificationExecutor for dynamic queries
    private final MongoTemplate mongoTemplate;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = transactionMapper.toEntity(transaction);
        // MongoDB has no @GeneratedValue — assign a UUID for new documents
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return transactionMapper.toDomain(springDataRepo.save(entity));
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return springDataRepo.findById(id).map(transactionMapper::toDomain);
    }



    @Override
    public List<Transaction> findByProductId(UUID productId) {
        return springDataRepo.findByProductId(productId).stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepo.deleteById(id);
    }

    @Override
    public PageCommand<Transaction> search(TransactionSearchCriteria criteria) {
        Query query = TransactionSpecification.fromCriteria(criteria);

        // Count before applying pagination so the total reflects all matching documents
        long totalCount = mongoTemplate.count(query, TransactionEntity.class);

        query.with(PageRequest.of(criteria.page(), criteria.limit()));
        List<Transaction> transactions = mongoTemplate.find(query, TransactionEntity.class).stream()
                .map(transactionMapper::toDomain)
                .toList();

        return PageCommand.of(transactions, totalCount, criteria.page(), criteria.limit());
    }

    @Override
    public Optional<Transaction> findByIdWithProductAndShop(UUID id) {
        return springDataRepo.findByIdWithProductAndShop(id).map(transactionMapper::toDomain);
    }
}
