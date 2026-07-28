package com.fintrack.fintrack_api.expenses.service;

import com.fintrack.fintrack_api.expenses.dto.SharedExpenseCreateRequest;
import com.fintrack.fintrack_api.expenses.dto.SharedExpenseParticipantRequest;
import com.fintrack.fintrack_api.expenses.model.SharedExpense;
import com.fintrack.fintrack_api.expenses.model.SharedExpenseParticipant;
import com.fintrack.fintrack_api.expenses.model.SplitType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpenseShareCalculatorTest {

    private final ExpenseShareCalculator calculator = new ExpenseShareCalculator();

    @Test
    void buildParticipants_equalSplitAllocatesSharesAndRemainder() {
        SharedExpenseCreateRequest request = SharedExpenseCreateRequest.builder()
                .description("Lunch")
                .totalAmount(new BigDecimal("100.00"))
                .splitType(SplitType.EQUAL)
                .participants(List.of(
                        SharedExpenseParticipantRequest.builder().participantId(2L).build(),
                        SharedExpenseParticipantRequest.builder().participantId(3L).build(),
                        SharedExpenseParticipantRequest.builder().participantId(4L).build()))
                .build();

        SharedExpense expense = SharedExpense.builder()
                .creatorId(1L)
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .splitType(request.getSplitType())
                .build();

        List<SharedExpenseParticipant> participants = calculator.buildParticipants(expense, request);

        assertEquals(3, participants.size());
        assertEquals(new BigDecimal("33.3333"), participants.get(0).getShareAmount());
        assertEquals(new BigDecimal("33.3333"), participants.get(1).getShareAmount());
        assertEquals(new BigDecimal("33.3334"), participants.get(2).getShareAmount());
        assertEquals(new BigDecimal("100.0000"), participants.stream()
                .map(SharedExpenseParticipant::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test
    void buildParticipants_customSplitPreservesProvidedAmounts() {
        SharedExpenseCreateRequest request = SharedExpenseCreateRequest.builder()
                .description("Dinner")
                .totalAmount(new BigDecimal("100.00"))
                .splitType(SplitType.CUSTOM)
                .participants(List.of(
                        SharedExpenseParticipantRequest.builder().participantId(2L).shareAmount(new BigDecimal("30.00")).build(),
                        SharedExpenseParticipantRequest.builder().participantId(3L).shareAmount(new BigDecimal("70.00")).build()))
                .build();

        SharedExpense expense = SharedExpense.builder()
                .creatorId(1L)
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .splitType(request.getSplitType())
                .build();

        List<SharedExpenseParticipant> participants = calculator.buildParticipants(expense, request);

        assertEquals(2, participants.size());
        assertEquals(new BigDecimal("30.00"), participants.get(0).getShareAmount());
        assertEquals(new BigDecimal("70.00"), participants.get(1).getShareAmount());
    }

    @Test
    void validateParticipants_customSplitThrowsWhenAmountsDoNotSumToTotal() {
        SharedExpenseCreateRequest request = SharedExpenseCreateRequest.builder()
                .description("Dinner")
                .totalAmount(new BigDecimal("100.00"))
                .splitType(SplitType.CUSTOM)
                .participants(List.of(
                        SharedExpenseParticipantRequest.builder().participantId(2L).shareAmount(new BigDecimal("30.00")).build(),
                        SharedExpenseParticipantRequest.builder().participantId(3L).shareAmount(new BigDecimal("60.00")).build()))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> calculator.validateParticipants(request));

        assertEquals("Custom shares must add up to the total amount", exception.getMessage());
    }
}
