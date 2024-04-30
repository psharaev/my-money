package ru.psharaev.mymoney.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mymoney.users_seq")
    private Long userId;

    private Instant createdAt;
    private Instant lastOperationAt;

    private ZoneOffset timezone;
    private String languageCode;

    @JoinColumn(name = "favorite_account_id")
    private Long favoriteAccountId;
    @JoinColumn(name = "favorite_category_flow_id")
    private Long favoriteCategoryFlowId;
    @JoinColumn(name = "favorite_category_transaction_id")
    private Long favoriteCategoryTransactionId;

    private Long telegramChatId;

    @Builder.Default
    @OneToMany(mappedBy = "ownerUserId", fetch = FetchType.EAGER)
    private List<Account> accounts = new ArrayList<>();
}
