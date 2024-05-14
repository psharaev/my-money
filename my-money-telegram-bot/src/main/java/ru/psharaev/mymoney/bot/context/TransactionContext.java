package ru.psharaev.mymoney.bot.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TransactionContext implements Context {
    public static final String CONTEXT_NAME = "transaction";

    private final long userId;
    private long chatId;
    private int messageId;

    private long fromAccountId;
    private long toAccountId;

    private BigDecimal fromAccountAmount;
    private BigDecimal toAccountAmount;

    private OffsetDateTime time;
    private String category;
    private String description;

    private String enterData;

    @Override
    @JsonIgnore
    public String getContextName() {
        return CONTEXT_NAME;
    }
}
