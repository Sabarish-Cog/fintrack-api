package com.fintrack.fintrack_api.transactions.repository;

import com.fintrack.fintrack_api.transactions.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
