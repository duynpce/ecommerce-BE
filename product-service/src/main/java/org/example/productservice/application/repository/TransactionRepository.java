package org.example.productservice.application.repository;

import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.domain.model.Transaction;
import org.example.productservice.infrastructure.web.data.entity.TransactionEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    List<Transaction> findByProductId(UUID productId);
    List<Transaction> findByContributorId(UUID contributorId);
    boolean existsById(UUID id);
    void deleteById(UUID id);
    PageCommand<Transaction> search(TransactionSearchCriteria criteria);
    Optional<Transaction> findByIdWithProductAndShop(UUID id);
}
