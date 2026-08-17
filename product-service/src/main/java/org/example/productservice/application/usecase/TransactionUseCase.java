package org.example.productservice.application.usecase;

import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.domain.model.Transaction;

import java.util.UUID;

public interface TransactionUseCase {
    Transaction create(CreateTransactionCommand command);
    Transaction findById(UUID id);
    Transaction update(UpdateTransactionCommand command);
    void delete(UUID id);
    PageCommand<Transaction> search(TransactionSearchCriteria criteria);
    Transaction complete(UUID id, TransactionStatus status);

}
