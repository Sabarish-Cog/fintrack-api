package com.fintrack.fintrack_api.expenses.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedExpenseParticipantRequest {

    @NotNull(message = "Participant id is required")
    private Long participantId;

    @NotNull(message = "Share amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Share amount must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Share amount must be a valid monetary value")
    private BigDecimal shareAmount;
}
