package com.fintrack.fintrack_api.transactions.dto;

import com.fintrack.fintrack_api.transactions.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private Long userId;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private Instant createdAt;
    private Instant updatedAt;
}
