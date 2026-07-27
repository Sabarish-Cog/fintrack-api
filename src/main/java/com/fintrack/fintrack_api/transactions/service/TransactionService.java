package com.fintrack.fintrack_api.transactions.service;

import com.fintrack.fintrack_api.common.exception.ResourceNotFoundException;
import com.fintrack.fintrack_api.transactions.dto.TransactionCreateRequest;
import com.fintrack.fintrack_api.transactions.model.Transaction;
import com.fintrack.fintrack_api.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Service layer for transaction business logic.
 * <p>
 * Encapsulates transaction creation, retrieval, listing and deletion operations.
 * Transactions are persisted using JPA repository abstractions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Create a transaction for a user.
     *
     * @param userId  user identifier
     * @param request transaction create request
     * @return created transaction entity
     */
    @Transactional
    public Transaction create(Long userId, TransactionCreateRequest request) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(request, "request must not be null");

        log.info("Creating transaction for userId={}", userId);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .description(request.getDescription())
                .amount(request.getAmount())
                .type(request.getType())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully for userId={} id={}", userId, savedTransaction.getId());
        return savedTransaction;
    }

    /**
     * List transactions for a user using pagination.
     *
     * @param userId   user identifier
     * @param pageable pageable parameters
     * @return paged transaction entities
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getByUser(Long userId, Pageable pageable) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(pageable, "pageable must not be null");

        log.debug("Fetching transactions for userId={} page={} size={}", userId, pageable.getPageNumber(), pageable.getPageSize());
        return transactionRepository.findByUserId(userId, pageable);
    }

    /**
     * Retrieve a transaction by id scoped to a user.
     *
     * @param userId        user identifier
     * @param transactionId transaction identifier
     * @return transaction entity
     */
    @Transactional(readOnly = true)
    public Transaction getById(Long userId, Long transactionId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");

        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found for userId=" + userId + " and transactionId=" + transactionId));
    }

    /**
     * Delete all transactions for a user.
     *
     * @param userId user identifier
     * @return number of deleted transactions
     */
    @Transactional
    public long deleteAll(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        log.warn("Deleting all transactions for userId={}", userId);
        long deletedCount = transactionRepository.deleteByUserId(userId);
        log.info("Deleted {} transactions for userId={}", deletedCount, userId);
        return deletedCount;
    }
}
