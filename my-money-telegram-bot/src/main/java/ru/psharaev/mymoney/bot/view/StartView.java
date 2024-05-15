package ru.psharaev.mymoney.bot.view;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.psharaev.mymoney.bot.context.StartContext;
import ru.psharaev.mymoney.bot.model.StartModel;
import ru.psharaev.mymoney.bot.util.Formatter;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.CurrencyService;
import ru.psharaev.mymoney.core.UserService;
import ru.psharaev.mymoney.core.entity.Account;
import ru.psharaev.mymoney.core.entity.Currency;
import ru.psharaev.mymoney.core.entity.Money;
import ru.psharaev.mymoney.core.entity.User;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Component
public class StartView extends AbstractView<StartContext> {
    public static final String FAVORITE_ACCOUNT_STAR = "⭐";

    private final UserService userService;
    private final AccountService accountService;
    private final CurrencyService currencyService;

    public StartView(UserService userService, AccountService accountService, CurrencyService currencyService) {
        super(StartContext.CONTEXT_NAME);
        this.userService = userService;
        this.accountService = accountService;
        this.currencyService = currencyService;
    }

    @Override
    ViewResult renderImpl(StartContext context) {
        User user = userService.getUser(context.getUserId());
        Money[] monies = user.getAccounts().stream()
                .map(a -> new Money(accountService.calcBalance(a.getAccountId()), a.getCurrency()))
                .toArray(Money[]::new);

        return new ViewResult(
                renderText(user, monies),
                renderKeyboard()
        );
    }

    private String renderText(User user, Money[] monies) {
        StringBuilder sb = new StringBuilder();

        sb.append("Состояние: ")
                .append(Formatter.formatMoney(currencyService.convert(Currency.RUB, monies)))
                .append("\n");

        sb.append("\n");

        sb.append("Счета:\n");
        renderAccounts(sb, user, monies);

        sb.append("\n");
        sb.append("Введи число для быстрого создания расхода");

        return sb.toString();
    }

    private InlineKeyboardMarkup renderKeyboard() {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(renderKeyboardFlow())
                .keyboardRow(renderKeyboardTransaction())
                .keyboardRow(renderKeyboardAccountsSettings())
                .keyboardRow(renderUnloadData())
                .build();
    }

    private void renderAccounts(StringBuilder sb, User user, Money[] monies) {
        int i = -1;
        for (Account account : user.getAccounts()) {
            i++;
            if (Objects.equals(user.getFavoriteAccountId(), account.getAccountId())) {
                sb.append(FAVORITE_ACCOUNT_STAR);
            }
            sb.append(account.getName());
            sb.append(": ");
            sb.append(Formatter.formatMoney(monies[i]));
            sb.append("\n");
        }
    }

    private InlineKeyboardRow renderKeyboardFlow() {
        return new InlineKeyboardRow(
                InlineKeyboardButton
                        .builder()
                        .text("Добавить расход/доход")
                        .callbackData(StartModel.Callback.CREATE_FLOW.name())
                        .build()
        );
    }

    private InlineKeyboardRow renderKeyboardTransaction() {
        return new InlineKeyboardRow(
                InlineKeyboardButton
                        .builder()
                        .text("Перевод")
                        .callbackData(StartModel.Callback.CREATE_TRANSACTION.name())
                        .build()
        );
    }

    private InlineKeyboardRow renderKeyboardAccountsSettings() {
        return new InlineKeyboardRow(
                InlineKeyboardButton
                        .builder()
                        .text("Управление счетами")
                        .callbackData(StartModel.Callback.ACCOUNT_MANAGEMENT.name())
                        .build()
        );
    }

    private InlineKeyboardRow renderUnloadData() {
        return new InlineKeyboardRow(
                InlineKeyboardButton
                        .builder()
                        .text("Выгрузить таблицу")
                        .callbackData(StartModel.Callback.UNLOAD_TABLE.name())
                        .build()
        );
    }

    @Getter
    @RequiredArgsConstructor
    private static class AccountWithBalance {
        private final long accountId;
        private final long ownerUserId;
        private final String name;
        private final Currency currency;
        private final BigDecimal balance;

        private AccountWithBalance(Account account, BigDecimal balance) {
            this.accountId = account.getAccountId();
            this.ownerUserId = account.getOwnerUserId();
            this.name = account.getName();
            this.currency = account.getCurrency();
            this.balance = balance;
        }
    }
}
