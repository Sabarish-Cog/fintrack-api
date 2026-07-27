package com.fintrack.fintrack_api.transactions.service;

import com.fintrack.fintrack_api.transactions.model.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(Transaction transaction) {
        Transaction toSave = Transaction.builder()
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt() == null ? LocalDateTime.now() : transaction.getCreatedAt())
                .build();
        return transactionRepository.save(toSave);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByUser(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    public void deleteAllTransactions() {
        transactionRepository.deleteAll();
    }
}
