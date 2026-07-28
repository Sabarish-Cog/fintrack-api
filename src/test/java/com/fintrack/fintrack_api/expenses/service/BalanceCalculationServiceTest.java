package com.fintrack.fintrack_api.expenses.service;

import com.fintrack.fintrack_api.expenses.model.SharedExpense;
import com.fintrack.fintrack_api.expenses.model.SharedExpenseParticipant;
import com.fintrack.fintrack_api.expenses.repository.SharedExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceCalculationServiceTest {

        @Mock
        private SharedExpenseRepository sharedExpenseRepository;

        @Mock
        private ExpenseShareCalculator expenseShareCalculator;

        @InjectMocks
        private BalanceCalculationService balanceCalculationService;

    @Test
    void calculateNetBalancesCombinesOwedAmountsForUser() {
        SharedExpense expense1 = SharedExpense.builder()
                .creatorId(1L)
                .totalAmount(new BigDecimal("50.00"))
                .splitType(com.fintrack.fintrack_api.expenses.model.SplitType.CUSTOM)
                .participants(List.of(
                        SharedExpenseParticipant.builder().participantId(2L).shareAmount(new BigDecimal("50.00")).build()))
                .build();

        SharedExpense expense2 = SharedExpense.builder()
                .creatorId(2L)
                .totalAmount(new BigDecimal("20.00"))
                .splitType(com.fintrack.fintrack_api.expenses.model.SplitType.CUSTOM)
                .participants(List.of(
                        SharedExpenseParticipant.builder().participantId(1L).shareAmount(new BigDecimal("20.00")).build()))
                .build();

        SharedExpense expense3 = SharedExpense.builder()
                .creatorId(1L)
                .totalAmount(new BigDecimal("30.00"))
                .splitType(com.fintrack.fintrack_api.expenses.model.SplitType.CUSTOM)
                .participants(List.of(
                        SharedExpenseParticipant.builder().participantId(3L).shareAmount(new BigDecimal("30.00")).build()))
                .build();

        when(sharedExpenseRepository.findAllByCreatorIdOrParticipantId(1L))
                .thenReturn(List.of(expense1, expense2, expense3));

        when(expenseShareCalculator.buildShareMap(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    com.fintrack.fintrack_api.expenses.model.SharedExpense e = invocation.getArgument(0);
                    java.util.Map<Long, java.math.BigDecimal> map = new java.util.LinkedHashMap<>();
                    for (com.fintrack.fintrack_api.expenses.model.SharedExpenseParticipant p : e.getParticipants()) {
                        map.put(p.getParticipantId(), p.getShareAmount());
                    }
                    return map;
                });

        List<?> results = balanceCalculationService.calculateNetBalances(1L);
        assertTrue(results.stream().anyMatch(result -> result instanceof com.fintrack.fintrack_api.expenses.dto.BalanceResult &&
                ((com.fintrack.fintrack_api.expenses.dto.BalanceResult) result).getOwingUserId().equals(2L) &&
                ((com.fintrack.fintrack_api.expenses.dto.BalanceResult) result).getOwedUserId().equals(1L) &&
                ((com.fintrack.fintrack_api.expenses.dto.BalanceResult) result).getAmount().equals(new BigDecimal("30.00"))));
        assertTrue(results.stream().anyMatch(result -> result instanceof com.fintrack.fintrack_api.expenses.dto.BalanceResult &&
                ((com.fintrack.fintrack_api.expenses.dto.BalanceResult) result).getOwingUserId().equals(3L) &&
                ((com.fintrack.fintrack_api.expenses.dto.BalanceResult) result).getOwedUserId().equals(1L) &&
                ((com.fintrack.fintrack_api.expenses.dto.BalanceResult) result).getAmount().equals(new BigDecimal("30.00"))));
    }
}
