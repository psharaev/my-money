package ru.psharaev.mymoney.bot.view;

import lombok.extern.slf4j.Slf4j;
import ru.psharaev.mymoney.bot.context.StartContext;
import ru.psharaev.mymoney.bot.model.StartModel;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.UserService;
import ru.psharaev.mymoney.core.entity.Account;
import ru.psharaev.mymoney.core.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Component
public class StartView extends AbstractView<StartContext> {
    public static final String FAVORITE_ACCOUNT_STAR = "⭐";

    private final UserService userService;
    private final AccountService accountService;

    public StartView(UserService userService, AccountService accountService) {
        super(StartContext.CONTEXT_NAME);
        this.userService = userService;
        this.accountService = accountService;
    }

    @Override
    ViewResult renderImpl(StartContext context) {
        User user = userService.getUser(context.getUserId());
        return new ViewResult(
                renderText(user),
                renderKeyboard()
        );
    }

    private String renderText(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Счета:\n");
        renderAccounts(sb, user);

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
                .build();
    }

    private void renderAccounts(StringBuilder sb, User user) {
        for (Account account : user.getAccounts()) {
            if (Objects.equals(user.getFavoriteAccountId(), account.getAccountId())) {
                sb.append(FAVORITE_ACCOUNT_STAR);
            }
            sb.append(account.getName());
            sb.append(": ");
            BigDecimal bigDecimal = accountService.calcBalance(account.getAccountId());
            sb.append(bigDecimal.toPlainString());
            sb.append(account.getCurrency().getCurrencySymbol());
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
}
