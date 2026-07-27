package com.fintrack.fintrack_api.transactions.controller;

import com.fintrack.fintrack_api.common.exception.AuthorizationException;
import com.fintrack.fintrack_api.transactions.dto.TransactionCreateRequest;
import com.fintrack.fintrack_api.transactions.dto.TransactionResponse;
import com.fintrack.fintrack_api.transactions.model.Transaction;
import com.fintrack.fintrack_api.transactions.service.TransactionService;
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

/**
 * REST controller for transaction operations.
 * <p>
 * Supports transaction creation, retrieval, paginated listing, and bulk deletion
 * for the authenticated user. This controller uses a header-based authorization
 * guard to validate that the path user id matches the authenticated user id.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/transactions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Create a new transaction for the given user.
     *
     * @param userId              the user identifier from the path
     * @param authenticatedUserId the authenticated user identifier from the request header
     * @param request             validated transaction creation payload
     * @return 201 Created with transaction response body
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @PathVariable Long userId,
            @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId,
            @Valid @RequestBody TransactionCreateRequest request) {

        authorize(userId, authenticatedUserId);
        Transaction transaction = transactionService.create(userId, request);
        TransactionResponse response = toResponse(transaction);

        return ResponseEntity.created(URI.create("/api/v1/users/" + userId + "/transactions/" + response.getId()))
                .body(response);
    }

    /**
     * List transactions for the given user with pageable results.
     *
     * @param userId              the user identifier from the path
     * @param authenticatedUserId the authenticated user identifier from the request header
     * @param pageable            pageable request parameters
     * @return paged list of transaction responses
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> listTransactions(
            @PathVariable Long userId,
            @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId,
            @PageableDefault(size = 20) Pageable pageable) {

        authorize(userId, authenticatedUserId);
        Page<TransactionResponse> page = transactionService.getByUser(userId, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(page);
    }

    /**
     * Retrieve a single transaction by id for the given user.
     *
     * @param userId              the user identifier from the path
     * @param transactionId       transaction id from the path
     * @param authenticatedUserId the authenticated user identifier from the request header
     * @return transaction response
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long userId,
            @PathVariable Long transactionId,
            @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId) {

        authorize(userId, authenticatedUserId);
        return ResponseEntity.ok(toResponse(transactionService.getById(userId, transactionId)));
    }

    /**
     * Delete all transactions for the given user.
     *
     * @param userId              the user identifier from the path
     * @param authenticatedUserId the authenticated user identifier from the request header
     * @return no content response on successful deletion
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllTransactions(
            @PathVariable Long userId,
            @RequestHeader("X-Authenticated-User-Id") Long authenticatedUserId) {

        authorize(userId, authenticatedUserId);
        long deletedCount = transactionService.deleteAll(userId);
        log.info("Deleted {} transactions for userId={}", deletedCount, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ensure the authenticated user matches the path user before granting access.
     *
     * @param pathUserId          user id from the path
     * @param authenticatedUserId user id from the authenticated request
     */
    private void authorize(Long pathUserId, Long authenticatedUserId) {
        if (authenticatedUserId == null || !authenticatedUserId.equals(pathUserId)) {
            log.warn("Authorization failure for authenticatedUserId={} pathUserId={}", authenticatedUserId, pathUserId);
            throw new AuthorizationException("User is not authorized to access this resource.");
        }
    }

    /**
     * Map a domain transaction entity to a DTO response.
     *
     * @param transaction the domain entity
     * @return response DTO
     */
    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
