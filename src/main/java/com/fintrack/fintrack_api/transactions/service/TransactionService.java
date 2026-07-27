package com.fintrack.fintrack_api.transactions.service;

import com.fintrack.fintrack_api.transactions.model.Transaction;
import com.fintrack.fintrack_api.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Create a new transaction
     *
     * @param transaction the transaction to create
     * @return the created transaction with generated ID
     */
    @Transactional
    public Transaction create(Transaction transaction) {
        log.info("Creating transaction for userId: {}", transaction.getUserId());
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with id: {}", savedTransaction.getId());
        return savedTransaction;
    }

    /**
     * Retrieve all transactions for a specific user
     *
     * @param userId the user ID
     * @return list of transactions for the user
     */
    @Transactional(readOnly = true)
    public List<Transaction> getByUser(Long userId) {
        log.debug("Fetching transactions for userId: {}", userId);
        return transactionRepository.findByUserId(userId);
    }

    /**
     * Delete all transactions for a specific user
     *
     * @param userId the user ID
     */
    @Transactional
    public void deleteAll(Long userId) {
        log.warn("Deleting all transactions for userId: {}", userId);
        transactionRepository.deleteByUserId(userId);
        log.info("All transactions deleted for userId: {}", userId);
    }
}
