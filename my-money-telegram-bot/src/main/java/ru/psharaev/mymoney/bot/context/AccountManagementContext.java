package ru.psharaev.mymoney.bot.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.psharaev.mymoney.core.entity.Currency;

@Getter
@Setter
@AllArgsConstructor
public class AccountManagementContext implements Context {
    public static final String CONTEXT_NAME = "account_management";

    private final long userId;
    private long chatId;
    private int messageId;

    private String languageCode;

    private String enterData;

    private String createAccountName;
    private Currency createAccountCurrency;

    @Override
    @JsonIgnore
    public String getContextName() {
        return CONTEXT_NAME;
    }
}
