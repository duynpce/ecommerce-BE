package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.application.mapper.TransactionMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.TransactionRepository;
import org.example.productservice.application.usecase.TransactionUseCase;
import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements TransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public Transaction create(CreateTransactionCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + command.productId()));

        if (product.getQuantity() < command.quantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + command.productId());
        }

        Transaction transaction = transactionMapper.toDomain(command);
        transaction.setContributorId(product.getContributorId());
        transaction.setCustomerId(command.customerId());
        transaction.setStatus(TransactionStatus.PENDING);

        product.setQuantity(product.getQuantity() - command.quantity());
        productRepository.save(product);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    }

    @Override
    @Transactional
    public Transaction update(UpdateTransactionCommand command) {
        Transaction transaction = transactionRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + command.id()));

        transactionMapper.updateFromCommand(command, transaction);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!transactionRepository.existsById(id)) {
            throw new NotFoundException("Transaction not found: " + id);
        }
        transactionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageCommand<Transaction> search(TransactionSearchCriteria criteria) {
        return transactionRepository.search(criteria);
    }

    @Override
    @Transactional
    public Transaction updateStatus(UUID id, TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
        transaction.setStatus(status);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction returnTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));

        if (transaction.getStatus() != TransactionStatus.RETURNED) {
            transaction.setStatus(TransactionStatus.RETURNED);
            transactionRepository.save(transaction);

            Product product = productRepository.findById(transaction.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + transaction.getProductId()));
            product.setQuantity(product.getQuantity() + transaction.getQuantity());
            productRepository.save(product);
        }

        return transaction;
    }
}


