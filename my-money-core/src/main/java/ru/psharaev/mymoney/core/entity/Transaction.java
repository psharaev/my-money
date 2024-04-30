package ru.psharaev.mymoney.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mymoney.transactions_seq")
    private Long transactionId;


    @JoinColumn(name = "from_account_id")
    private long fromAccountId;

    @JoinColumn(name = "to_account_id")
    private long toAccountId;


    private BigDecimal fromAmount;
    private BigDecimal toAmount;

    @NotNull
    private Instant time;

    @NotNull
    private long categoryId;

    @NotNull
    @Column(length = 200)
    private String description;
}
