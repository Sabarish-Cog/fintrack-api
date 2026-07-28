package com.fintrack.fintrack_api.expenses.controller;

import com.fintrack.fintrack_api.common.exception.AuthorizationException;
import com.fintrack.fintrack_api.expenses.dto.BalanceResult;
import com.fintrack.fintrack_api.expenses.dto.SharedExpenseCreateRequest;
import com.fintrack.fintrack_api.expenses.dto.SharedExpenseParticipantResponse;
import com.fintrack.fintrack_api.expenses.dto.SharedExpenseResponse;
import com.fintrack.fintrack_api.expenses.model.SharedExpense;
import com.fintrack.fintrack_api.expenses.model.SharedExpenseParticipant;
import com.fintrack.fintrack_api.expenses.service.BalanceCalculationService;
import com.fintrack.fintrack_api.expenses.service.SharedExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users/{userId}/shared-expenses")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SharedExpenseController {

    private final SharedExpenseService sharedExpenseService;
    private final BalanceCalculationService balanceCalculationService;

    @PostMapping
    public ResponseEntity<SharedExpenseResponse> createSharedExpense(
            @PathVariable Long userId,
            @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId,
            @Valid @RequestBody SharedExpenseCreateRequest request) {

        authorize(userId, authenticatedUserId);
        SharedExpense sharedExpense = sharedExpenseService.create(userId, request);
        SharedExpenseResponse response = toResponse(sharedExpense);

        return ResponseEntity.created(URI.create("/api/v1/users/" + userId + "/shared-expenses/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SharedExpenseResponse>> listSharedExpenses(
            @PathVariable Long userId,
            @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId,
            @PageableDefault(size = 20) Pageable pageable) {

        authorize(userId, authenticatedUserId);
        Page<SharedExpenseResponse> page = sharedExpenseService.getByUser(userId, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/balances")
    public ResponseEntity<List<BalanceResult>> getPendingBalances(
            @PathVariable Long userId,
            @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId) {

        authorize(userId, authenticatedUserId);
        List<BalanceResult> balances = balanceCalculationService.calculateNetBalances(userId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<SharedExpenseResponse> getSharedExpenseById(
            @PathVariable Long userId,
            @PathVariable Long expenseId,
            @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId) {

        authorize(userId, authenticatedUserId);
        return ResponseEntity.ok(toResponse(sharedExpenseService.getById(userId, expenseId)));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllSharedExpenses(
            @PathVariable Long userId,
            @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId) {

        authorize(userId, authenticatedUserId);
        long deletedCount = sharedExpenseService.deleteAll(userId);
        log.info("Deleted {} shared expenses for userId={}", deletedCount, userId);
        return ResponseEntity.noContent().build();
    }

    private void authorize(Long pathUserId, Long authenticatedUserId) {
        if (authenticatedUserId == null || !authenticatedUserId.equals(pathUserId)) {
            log.warn("Authorization failure for authenticatedUserId={} pathUserId={}", authenticatedUserId, pathUserId);
            throw new AuthorizationException("User is not authorized to access this resource.");
        }
    }

    private SharedExpenseResponse toResponse(SharedExpense sharedExpense) {
        return SharedExpenseResponse.builder()
                .id(sharedExpense.getId())
                .creatorId(sharedExpense.getCreatorId())
                .description(sharedExpense.getDescription())
                .totalAmount(sharedExpense.getTotalAmount())
                .splitType(sharedExpense.getSplitType())
                .participants(toParticipantResponses(sharedExpense.getParticipants()))
                .createdAt(sharedExpense.getCreatedAt())
                .updatedAt(sharedExpense.getUpdatedAt())
                .build();
    }

    private List<SharedExpenseParticipantResponse> toParticipantResponses(List<SharedExpenseParticipant> participants) {
        return participants.stream()
                .map(participant -> SharedExpenseParticipantResponse.builder()
                        .participantId(participant.getParticipantId())
                        .shareAmount(participant.getShareAmount())
                        .build())
                .collect(Collectors.toList());
    }
}
