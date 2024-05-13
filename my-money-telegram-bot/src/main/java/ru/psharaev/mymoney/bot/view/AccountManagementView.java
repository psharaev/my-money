package ru.psharaev.mymoney.bot.view;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.psharaev.mymoney.bot.context.AccountManagementContext;
import ru.psharaev.mymoney.bot.model.AccountManagementModel;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.entity.Account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.psharaev.mymoney.bot.view.StartView.FAVORITE_ACCOUNT_STAR;

@Slf4j
@Component
public class AccountManagementView extends AbstractView<AccountManagementContext> {

    private static final InlineKeyboardButton CREATE_NEW_ACCOUNT = InlineKeyboardButton
            .builder()
            .text("Создать новый счёт")
            .callbackData(AccountManagementModel.Callback.CREATE_ACCOUNT.name())
            .build();

    private static final InlineKeyboardButton BACK = InlineKeyboardButton
            .builder()
            .text("« Назад")
            .callbackData(AccountManagementModel.Callback.BACK.name())
            .build();

    private final AccountService accountService;


    public AccountManagementView(AccountService accountService) {
        super(AccountManagementContext.CONTEXT_NAME);
        this.accountService = accountService;
    }

    @Override
    ViewResult renderImpl(AccountManagementContext context) {

        List<Account> allAccounts = accountService.getAllAccounts(context.getUserId());
        Optional<Long> favoriteAccountId = accountService.getFavoriteAccountId(context.getUserId());

        Cache cache = new Cache(allAccounts, favoriteAccountId);

        return new ViewResult(
                renderText(context, cache),
                renderKeyboard(context, cache)
        );
    }

    private String renderText(AccountManagementContext context, Cache cache) {
        StringBuilder sb = new StringBuilder(100);

        sb.append("💸 Твои счета\n\n");

        int number = 1;
        for (Account account : cache.getAccounts()) {
            sb.append(number)
                    .append(". ");
            if (cache.isFavoriteAccountId(account.getAccountId())) {
                sb.append(FAVORITE_ACCOUNT_STAR);
            }
            sb.append(account.getName());
            sb.append(": ");
            BigDecimal bigDecimal = accountService.calcBalance(account.getAccountId());
            sb.append(bigDecimal.toPlainString());
            sb.append(account.getCurrency().getCurrencySymbol());
            sb.append("\n");
            number++;
        }

        return sb.toString();
    }

    private InlineKeyboardMarkup renderKeyboard(AccountManagementContext context, Cache cache) {
        ArrayList<InlineKeyboardRow> keyboard = new ArrayList<>(cache.getAccounts().size());
        int number = 1;
        for (Account account : cache.getAccounts()) {
            keyboard.add(new InlineKeyboardRow(
                            InlineKeyboardButton
                                    .builder()
                                    .text(number + ". " + account.getName())
                                    .callbackData(AccountManagementModel.Callback.IGNORE_BUCKET_NAME.name())
                                    .build()
                    )
            );

            InlineKeyboardRow row = new InlineKeyboardRow();

            if (!cache.isFavoriteAccountId(account.getAccountId())) {
                row.add(
                        InlineKeyboardButton
                                .builder()
                                .text("☆")
                                .callbackData("%s:0x%x".formatted(AccountManagementModel.Callback.SET_FAVORITE_ACCOUNT.name(),
                                        account.getAccountId()))
                                .build()
                );
            }

            row.add(InlineKeyboardButton
                    .builder()
                    .text("✍️")
                    .callbackData("%s:0x%x".formatted(AccountManagementModel.Callback.RENAME_ACCOUNT.name(),
                            account.getAccountId()))
                    .build());
            row.add(InlineKeyboardButton
                    .builder()
                    .text("🗑")
                    .callbackData("%s:0x%x".formatted(AccountManagementModel.Callback.DELETE_ACCOUNT.name(),
                            account.getAccountId()))
                    .build());

            keyboard.add(row);
            number++;
        }

        keyboard.add(new InlineKeyboardRow(
                CREATE_NEW_ACCOUNT
        ));

        keyboard.add(new InlineKeyboardRow(
                BACK
        ));
        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }

    @Getter
    @RequiredArgsConstructor
    private static final class Cache {
        private final List<Account> accounts;
        private final Optional<Long> favoriteAccountId;

        private boolean isFavoriteAccountId(long accountId) {
            return favoriteAccountId.isPresent() && favoriteAccountId.get() == accountId;
        }
    }
}
