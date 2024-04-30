package ru.psharaev.mymoney.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "account_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mymoney.accounts_seq")
    private Long accountId;

    @JoinColumn(name = "owner_user_id")
    private Long ownerUserId;

    @NotBlank
    @Column(length = 60)
    private String name;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Currency currency;
}
