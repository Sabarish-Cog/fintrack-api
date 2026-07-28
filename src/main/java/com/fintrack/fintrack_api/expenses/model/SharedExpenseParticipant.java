package com.fintrack.fintrack_api.expenses.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "shared_expense_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SharedExpenseParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SharedExpense sharedExpense;

    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal shareAmount;
}
