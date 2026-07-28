package com.fintrack.fintrack_api.expenses.dto;

import com.fintrack.fintrack_api.expenses.model.SplitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedExpenseResponse {

    private Long id;
    private Long creatorId;
    private String description;
    private BigDecimal totalAmount;
    private SplitType splitType;
    private List<SharedExpenseParticipantResponse> participants;
    private Instant createdAt;
    private Instant updatedAt;
}
