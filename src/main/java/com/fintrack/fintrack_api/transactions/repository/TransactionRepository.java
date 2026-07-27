package com.fintrack.fintrack_api.transactions.repository;

import com.fintrack.fintrack_api.transactions.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
    long deleteByUserId(Long userId);
}
