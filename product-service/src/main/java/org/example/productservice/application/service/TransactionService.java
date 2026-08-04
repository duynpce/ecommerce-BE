package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.client.TicketClient;
import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.application.mapper.TransactionMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.TransactionRepository;
import org.example.productservice.application.usecase.TransactionUseCase;
import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.domain.exception.InvalidStateException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.Transaction;
import org.example.productservice.infrastructure.ticket.dto.StartBuyingProcedureRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements TransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final TransactionMapper transactionMapper;
    private final TicketClient ticketClient;

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
        transaction.setTotalAmount(transaction.calculateTotal());
        transaction.setCreatedAt(Instant.now());

        product.setQuantity(product.getQuantity() - command.quantity());
        productRepository.save(product);

        Transaction saved = transactionRepository.save(transaction);

        ticketClient.startBuyingProcedure(new StartBuyingProcedureRequest(
                saved.getId(),
                saved.getContributorId(),
                saved.getCustomerId()
        ));

        return saved;
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

    /**
     * Step 2a — PENDING → APPROVED
     */
    @Override
    @Transactional
    public Transaction approve(UUID id) {
        Transaction transaction = requireTransaction(id);
        requireStatus(transaction, TransactionStatus.PENDING, "approve");
        transaction.setStatus(TransactionStatus.PACKING);

        return transactionRepository.save(transaction);
    }

    /**
     * Step 2b — PENDING → REJECTED
     * Restores stock since the reservation made at creation is canceled.
     */
    @Override
    @Transactional
    public Transaction reject(UUID id) {
        Transaction transaction = requireTransaction(id);
        requireStatus(transaction, TransactionStatus.PENDING, "reject");
        transaction.setStatus(TransactionStatus.REJECTED);
        transactionRepository.save(transaction);

        Product product = productRepository.findById(transaction.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + transaction.getProductId()));
        product.setQuantity(product.getQuantity() + transaction.getQuantity());
        productRepository.save(product);

        return transaction;
    }

    /**
     * Step 3 — APPROVED → SHIPPED
     */
    @Override
    @Transactional
    public Transaction markShipped(UUID id) {
        Transaction transaction = requireTransaction(id);
        requireStatus(transaction, TransactionStatus.PACKING, "mark as shipped");
        transaction.setStatus(TransactionStatus.DELIVERING);

        return transactionRepository.save(transaction);
    }

    /**
     * Step 4 — SHIPPED → COMPLETED
     */
    @Override
    @Transactional
    public Transaction complete(UUID id) {
        Transaction transaction = requireTransaction(id);
        requireStatus(transaction, TransactionStatus.DELIVERING, "complete");
        transaction.setStatus(TransactionStatus.COMPLETED);

        return transactionRepository.save(transaction);
    }

    /**
     * Step 5 — SHIPPED → RETURNED
     * Restores product stock when the return procedure succeeds.
     */
    @Override
    @Transactional
    public Transaction returnTransaction(UUID id) {
        Transaction transaction = requireTransaction(id);

        if (transaction.getStatus() == TransactionStatus.RETURNED) {
            return transaction;
        }

        transaction.setStatus(TransactionStatus.RETURNED);
        transactionRepository.save(transaction);

        Product product = productRepository.findById(transaction.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + transaction.getProductId()));
        product.setQuantity(product.getQuantity() + transaction.getQuantity());
        productRepository.save(product);

        return transaction;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Transaction requireTransaction(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    }

    private void requireStatus(Transaction transaction, TransactionStatus expected, String action) {
        if (transaction.getStatus() != expected) {
            throw new InvalidStateException(String.format(
                    "Cannot %s transaction %s: expected status %s but was %s",
                    action, transaction.getId(), expected, transaction.getStatus()
            ));
        }
    }
}
