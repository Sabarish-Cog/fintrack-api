package com.fintrack.fintrack_api.transactions.repository;

import com.fintrack.fintrack_api.transactions.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for transaction persistence operations.
 * <p>
 * Uses Spring Data JPA to manage transaction entities and custom user-scoped queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transactions for the given user with pagination.
     *
     * @param userId   user identifier
     * @param pageable pageable parameters
     * @return page of transactions
     */
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    /**
     * Find a single transaction by id and user.
     *
     * @param id     transaction identifier
     * @param userId user identifier
     * @return optional transaction
     */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /**
     * Delete all transactions for the specified user.
     *
     * @param userId user identifier
     * @return number of deleted records
     */
    long deleteByUserId(Long userId);
}
