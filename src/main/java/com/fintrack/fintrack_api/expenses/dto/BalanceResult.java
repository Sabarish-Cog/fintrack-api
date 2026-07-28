package com.fintrack.fintrack_api.expenses.dto;

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
public class BalanceResult {

    private Long owingUserId;
    private Long owedUserId;
    private BigDecimal amount;
}
