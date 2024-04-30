package ru.psharaev.mymoney.bot.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class FlowContext implements Context {
    public static final String CONTEXT_NAME = "flow";

    private final long userId;
    private long chatId;
    private int messageId;

    private long accountId;
    @JsonProperty("isFavoriteAccount")
    private boolean isFavoriteAccount;
    private BigDecimal amount;
    private String currencySymbol;
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
