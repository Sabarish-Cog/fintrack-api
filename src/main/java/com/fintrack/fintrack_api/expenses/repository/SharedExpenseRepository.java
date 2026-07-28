package com.fintrack.fintrack_api.expenses.repository;

import com.fintrack.fintrack_api.expenses.model.SharedExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedExpenseRepository extends JpaRepository<SharedExpense, Long> {

    Page<SharedExpense> findByCreatorId(Long creatorId, Pageable pageable);

    Optional<SharedExpense> findByIdAndCreatorId(Long id, Long creatorId);

    long deleteByCreatorId(Long creatorId);

    @Query("select distinct e from SharedExpense e left join fetch e.participants p " +
            "where e.creatorId = :userId or p.participantId = :userId")
    List<SharedExpense> findAllByCreatorIdOrParticipantId(Long userId);
}
