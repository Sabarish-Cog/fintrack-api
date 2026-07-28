package com.fintrack.fintrack_api.expenses.service;

import com.fintrack.fintrack_api.expenses.dto.BalanceResult;
import com.fintrack.fintrack_api.expenses.model.SharedExpense;
import com.fintrack.fintrack_api.expenses.repository.SharedExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceCalculationService {

    private final SharedExpenseRepository sharedExpenseRepository;
    private final ExpenseShareCalculator expenseShareCalculator;

    @Transactional(readOnly = true)
    public List<BalanceResult> calculateNetBalances(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        List<SharedExpense> expenses = sharedExpenseRepository.findAllByCreatorIdOrParticipantId(userId);
        Map<UserPair, BigDecimal> pairBalances = new HashMap<>();

        for (SharedExpense expense : expenses) {
            Map<Long, BigDecimal> shares = expenseShareCalculator.buildShareMap(expense);
            Long creatorId = expense.getCreatorId();

            for (Map.Entry<Long, BigDecimal> entry : shares.entrySet()) {
                Long participantId = entry.getKey();
                if (participantId.equals(creatorId)) {
                    continue;
                }
                BigDecimal amount = entry.getValue();
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                UserPair pair = new UserPair(participantId, creatorId);
                BigDecimal signedAmount = pair.isFirstUser(participantId) ? amount : amount.negate();
                pairBalances.merge(pair, signedAmount, BigDecimal::add);
            }
        }

        return pairBalances.entrySet().stream()
                .map(entry -> createBalanceResult(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .filter(result -> result.getOwingUserId().equals(userId) || result.getOwedUserId().equals(userId))
                .collect(Collectors.toList());
    }

    private BalanceResult createBalanceResult(UserPair pair, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return BalanceResult.builder()
                    .owingUserId(pair.getFirstUserId())
                    .owedUserId(pair.getSecondUserId())
                    .amount(amount)
                    .build();
        }

        return BalanceResult.builder()
                .owingUserId(pair.getSecondUserId())
                .owedUserId(pair.getFirstUserId())
                .amount(amount.abs())
                .build();
    }

    private static class UserPair {
        private final Long firstUserId;
        private final Long secondUserId;

        private UserPair(Long userIdA, Long userIdB) {
            if (userIdA < userIdB) {
                this.firstUserId = userIdA;
                this.secondUserId = userIdB;
            } else {
                this.firstUserId = userIdB;
                this.secondUserId = userIdA;
            }
        }

        public boolean isFirstUser(Long userId) {
            return firstUserId.equals(userId);
        }

        public Long getFirstUserId() {
            return firstUserId;
        }

        public Long getSecondUserId() {
            return secondUserId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserPair userPair = (UserPair) o;
            return firstUserId.equals(userPair.firstUserId) && secondUserId.equals(userPair.secondUserId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstUserId, secondUserId);
        }
    }
}
