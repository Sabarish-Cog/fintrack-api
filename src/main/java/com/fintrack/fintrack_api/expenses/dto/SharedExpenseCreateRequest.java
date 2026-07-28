package com.fintrack.fintrack_api.expenses.dto;

import com.fintrack.fintrack_api.expenses.model.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedExpenseCreateRequest {

    @NotBlank(message = "Description must not be blank")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Total amount must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Total amount must be a valid monetary value")
    private BigDecimal totalAmount;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    @NotEmpty(message = "At least one participant is required")
    @Valid
    private List<SharedExpenseParticipantRequest> participants;
}
