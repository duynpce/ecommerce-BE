package org.example.productservice.application.mapper;

import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.domain.model.Transaction;
import org.example.productservice.infrastructure.web.data.entity.TransactionEntity;
import org.example.productservice.infrastructure.web.dto.transaction.CreateTransactionRequest;
import org.example.productservice.infrastructure.web.dto.transaction.TransactionFilter;
import org.example.productservice.infrastructure.web.dto.transaction.TransactionResponse;
import org.example.productservice.infrastructure.web.dto.transaction.UpdateTransactionRequest;

import java.util.UUID;

public interface TransactionMapper {
    Transaction toDomain(TransactionEntity entity);
    Transaction toDomain(CreateTransactionCommand command);
    TransactionEntity toEntity(Transaction transaction);
    void updateFromCommand(UpdateTransactionCommand command, Transaction transaction);

    CreateTransactionCommand toCommand(CreateTransactionRequest request, UUID contributorId);
    UpdateTransactionCommand toCommand(UpdateTransactionRequest request, UUID id);

    // userId null → READ_ALL (no user restriction); userId set → READ_SELF
    TransactionSearchCriteria toCriteria(TransactionFilter filter, UUID userId);

    TransactionResponse toResponse(Transaction transaction);
}
