package com.fintrack.fintrack_api.expenses.controller;

import com.fintrack.fintrack_api.common.exception.AuthorizationException;
import com.fintrack.fintrack_api.expenses.dto.BalanceResult;
import com.fintrack.fintrack_api.expenses.dto.SharedExpenseCreateRequest;
import com.fintrack.fintrack_api.expenses.dto.SharedExpenseParticipantRequest;
import com.fintrack.fintrack_api.expenses.model.SharedExpense;
import com.fintrack.fintrack_api.expenses.model.SharedExpenseParticipant;
import com.fintrack.fintrack_api.expenses.model.SplitType;
import com.fintrack.fintrack_api.expenses.service.BalanceCalculationService;
import com.fintrack.fintrack_api.expenses.service.SharedExpenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SharedExpenseControllerTest {

    @Mock
    private SharedExpenseService sharedExpenseService;

    @Mock
    private BalanceCalculationService balanceCalculationService;

    @InjectMocks
    private SharedExpenseController controller;

    @Test
    void createSharedExpenseReturnsCreatedResponse() {
        SharedExpenseCreateRequest request = SharedExpenseCreateRequest.builder()
                .description("Dinner")
                .totalAmount(new BigDecimal("100.00"))
                .splitType(SplitType.CUSTOM)
                .participants(List.of(
                        SharedExpenseParticipantRequest.builder().participantId(2L).shareAmount(new BigDecimal("40.00")).build(),
                        SharedExpenseParticipantRequest.builder().participantId(3L).shareAmount(new BigDecimal("60.00")).build()))
                .build();

        SharedExpense savedExpense = SharedExpense.builder()
                .id(1L)
                .creatorId(1L)
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .splitType(request.getSplitType())
                .participants(List.of(
                        SharedExpenseParticipant.builder().participantId(2L).shareAmount(new BigDecimal("40.00")).build(),
                        SharedExpenseParticipant.builder().participantId(3L).shareAmount(new BigDecimal("60.00")).build()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(sharedExpenseService.create(1L, request)).thenReturn(savedExpense);

        var response = controller.createSharedExpense(1L, 1L, request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("/api/v1/users/1/shared-expenses/1", response.getHeaders().getLocation().toString());
        assertEquals(1L, response.getBody().getId());
        assertEquals(1L, response.getBody().getCreatorId());
        assertEquals("Dinner", response.getBody().getDescription());
    }

    @Test
    void getPendingBalancesThrowsWhenUnauthorized() {
        assertThrows(AuthorizationException.class, () -> controller.getPendingBalances(1L, 2L));
    }

    @Test
    void getPendingBalancesReturnsListWhenAuthorized() {
        BalanceResult balanceResult = BalanceResult.builder()
                .owingUserId(2L)
                .owedUserId(1L)
                .amount(new BigDecimal("30.00"))
                .build();

        when(balanceCalculationService.calculateNetBalances(1L)).thenReturn(List.of(balanceResult));

        var response = controller.getPendingBalances(1L, 1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(balanceResult, response.getBody().get(0));
    }
}
