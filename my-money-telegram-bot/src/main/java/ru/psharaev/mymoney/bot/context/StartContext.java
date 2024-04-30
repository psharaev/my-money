package ru.psharaev.mymoney.bot.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.ZoneOffset;

@Getter
@Setter
@AllArgsConstructor
public class StartContext implements Context {
    public static final String CONTEXT_NAME = "start";

    private final long userId;
    private long chatId;
    private int messageId;

    private ZoneOffset timezone;
    private String languageCode;

    private Long favoriteAccountId;
    private Long favoriteCategoryFlowId;
    private Long favoriteCategoryTransactionId;

    @Override
    @JsonIgnore
    public String getContextName() {
        return CONTEXT_NAME;
    }
}
