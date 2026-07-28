package com.fintrack.fintrack_api.expenses.service;

import com.fintrack.fintrack_api.common.exception.ResourceNotFoundException;
import com.fintrack.fintrack_api.expenses.dto.SharedExpenseCreateRequest;
import com.fintrack.fintrack_api.expenses.model.SharedExpense;
import com.fintrack.fintrack_api.expenses.model.SharedExpenseParticipant;
import com.fintrack.fintrack_api.expenses.repository.SharedExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedExpenseService {

    private final SharedExpenseRepository sharedExpenseRepository;
    private final ExpenseShareCalculator expenseShareCalculator;

    @Transactional
    public SharedExpense create(Long creatorId, SharedExpenseCreateRequest request) {
        Objects.requireNonNull(creatorId, "creatorId must not be null");
        Objects.requireNonNull(request, "request must not be null");

        expenseShareCalculator.validateParticipants(request);

        log.info("Creating shared expense for creatorId={}", creatorId);

        SharedExpense sharedExpense = SharedExpense.builder()
                .creatorId(creatorId)
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .splitType(request.getSplitType())
                .build();

        List<SharedExpenseParticipant> participants = expenseShareCalculator.buildParticipants(sharedExpense, request);
        sharedExpense.setParticipants(participants);
        SharedExpense savedSharedExpense = sharedExpenseRepository.save(sharedExpense);

        log.info("Shared expense created for creatorId={} id={} totalAmount={}", creatorId, savedSharedExpense.getId(), savedSharedExpense.getTotalAmount());
        return savedSharedExpense;
    }

    @Transactional(readOnly = true)
    public Page<SharedExpense> getByUser(Long creatorId, Pageable pageable) {
        Objects.requireNonNull(creatorId, "creatorId must not be null");
        Objects.requireNonNull(pageable, "pageable must not be null");

        log.debug("Fetching shared expenses for creatorId={} page={} size={}", creatorId, pageable.getPageNumber(), pageable.getPageSize());
        return sharedExpenseRepository.findByCreatorId(creatorId, pageable);
    }

    @Transactional(readOnly = true)
    public SharedExpense getById(Long creatorId, Long expenseId) {
        Objects.requireNonNull(creatorId, "creatorId must not be null");
        Objects.requireNonNull(expenseId, "expenseId must not be null");

        return sharedExpenseRepository.findByIdAndCreatorId(expenseId, creatorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shared expense not found for creatorId=" + creatorId + " and expenseId=" + expenseId));
    }

    @Transactional
    public long deleteAll(Long creatorId) {
        Objects.requireNonNull(creatorId, "creatorId must not be null");

        log.warn("Deleting all shared expenses for creatorId={}", creatorId);
        long deletedCount = sharedExpenseRepository.deleteByCreatorId(creatorId);
        log.info("Deleted {} shared expenses for creatorId={}", deletedCount, creatorId);
        return deletedCount;
    }
}
