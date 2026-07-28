package com.fintrack.fintrack_api.expenses.service;

import com.fintrack.fintrack_api.expenses.dto.SharedExpenseCreateRequest;
import com.fintrack.fintrack_api.expenses.dto.SharedExpenseParticipantRequest;
import com.fintrack.fintrack_api.expenses.model.SharedExpense;
import com.fintrack.fintrack_api.expenses.model.SharedExpenseParticipant;
import com.fintrack.fintrack_api.expenses.model.SplitType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ExpenseShareCalculator {

    private static final int SCALE = 4;

    public void validateParticipants(SharedExpenseCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        List<SharedExpenseParticipantRequest> participants = request.getParticipants();
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("At least one participant is required");
        }

        if (request.getSplitType() == null) {
            throw new IllegalArgumentException("Split type is required");
        }

        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than zero");
        }

        validateParticipantIds(participants);

        if (request.getSplitType().equals(SplitType.CUSTOM)) {
            validateCustomAmounts(request);
        } else {
            validateEqualSplitAmounts(participants);
        }
    }

    public List<SharedExpenseParticipant> buildParticipants(SharedExpense sharedExpense,
                                                            SharedExpenseCreateRequest request) {
        if (request.getSplitType().equals(SplitType.EQUAL)) {
            return buildEqualSplitParticipants(sharedExpense, request);
        }
        return buildCustomSplitParticipants(sharedExpense, request);
    }

    public Map<Long, BigDecimal> buildShareMap(SharedExpense expense) {
        if (expense.getSplitType().equals(SplitType.EQUAL)) {
            return calculateEqualShareMap(expense);
        }

        return expense.getParticipants().stream()
                .collect(Collectors.toMap(SharedExpenseParticipant::getParticipantId,
                        SharedExpenseParticipant::getShareAmount, BigDecimal::add, LinkedHashMap::new));
    }

    private void validateParticipantIds(List<SharedExpenseParticipantRequest> participants) {
        Set<Long> participantIds = participants.stream()
                .map(SharedExpenseParticipantRequest::getParticipantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (participantIds.size() != participants.size()) {
            throw new IllegalArgumentException("Participant ids must be unique and not null");
        }
    }

    private void validateCustomAmounts(SharedExpenseCreateRequest request) {
        BigDecimal totalShareAmount = BigDecimal.ZERO;

        for (SharedExpenseParticipantRequest participant : request.getParticipants()) {
            if (participant.getShareAmount() == null) {
                throw new IllegalArgumentException("Share amount is required for custom split");
            }
            if (participant.getShareAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Share amount must be greater than zero");
            }
            totalShareAmount = totalShareAmount.add(participant.getShareAmount());
        }

        if (totalShareAmount.compareTo(request.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("Custom shares must add up to the total amount");
        }
    }

    private void validateEqualSplitAmounts(List<SharedExpenseParticipantRequest> participants) {
        for (SharedExpenseParticipantRequest participant : participants) {
            if (participant.getParticipantId() == null) {
                throw new IllegalArgumentException("Participant id must not be null");
            }
            if (participant.getShareAmount() != null && participant.getShareAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Share amount must be greater than zero when provided");
            }
        }
    }

    private List<SharedExpenseParticipant> buildEqualSplitParticipants(SharedExpense sharedExpense,
                                                                      SharedExpenseCreateRequest request) {
        int count = request.getParticipants().size();
        BigDecimal equalShare = sharedExpense.getTotalAmount()
                .divide(BigDecimal.valueOf(count), SCALE, RoundingMode.HALF_UP);

        List<SharedExpenseParticipant> participants = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;

        for (int index = 0; index < request.getParticipants().size(); index++) {
            SharedExpenseParticipantRequest participantRequest = request.getParticipants().get(index);
            BigDecimal shareAmount = (index == request.getParticipants().size() - 1)
                    ? sharedExpense.getTotalAmount().subtract(allocated)
                    : equalShare;

            allocated = allocated.add(shareAmount);
            participants.add(SharedExpenseParticipant.builder()
                    .sharedExpense(sharedExpense)
                    .participantId(participantRequest.getParticipantId())
                    .shareAmount(shareAmount)
                    .build());
        }

        return participants;
    }

    private List<SharedExpenseParticipant> buildCustomSplitParticipants(SharedExpense sharedExpense,
                                                                       SharedExpenseCreateRequest request) {
        return request.getParticipants().stream()
                .map(participantRequest -> SharedExpenseParticipant.builder()
                        .sharedExpense(sharedExpense)
                        .participantId(participantRequest.getParticipantId())
                        .shareAmount(participantRequest.getShareAmount())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<Long, BigDecimal> calculateEqualShareMap(SharedExpense expense) {
        List<SharedExpenseParticipant> participants = expense.getParticipants();
        int count = participants.size();
        BigDecimal equalShare = expense.getTotalAmount()
                .divide(BigDecimal.valueOf(count), SCALE, RoundingMode.HALF_UP);

        Map<Long, BigDecimal> shareMap = new LinkedHashMap<>();
        BigDecimal allocated = BigDecimal.ZERO;

        for (int index = 0; index < participants.size(); index++) {
            SharedExpenseParticipant participant = participants.get(index);
            BigDecimal shareAmount = (index == participants.size() - 1)
                    ? expense.getTotalAmount().subtract(allocated)
                    : equalShare;
            allocated = allocated.add(shareAmount);
            shareMap.put(participant.getParticipantId(), shareAmount);
        }

        return shareMap;
    }
}
