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
@Table(name = "flows")
public class Flow {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mymoney.flows_seq")
    private Long flowId;

    @JoinColumn(name = "account_id")
    private long accountId;

    private BigDecimal amount;

    @NotNull
    private Instant time;

    @NotNull
    private long categoryId;

    @NotNull
    @Column(length = 200)
    private String description;
}
