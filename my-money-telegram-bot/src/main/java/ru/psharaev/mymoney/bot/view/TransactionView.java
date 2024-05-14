package ru.psharaev.mymoney.bot.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.psharaev.mymoney.bot.context.TransactionContext;
import ru.psharaev.mymoney.bot.model.TransactionModel;
import ru.psharaev.mymoney.bot.util.Formatter;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.CurrencyService;
import ru.psharaev.mymoney.core.entity.Account;
import ru.psharaev.mymoney.core.entity.CurrencyPair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TransactionView extends AbstractView<TransactionContext> {
    private static final InlineKeyboardButton ENTER_TIME = InlineKeyboardButton
            .builder()
            .text("Время")
            .callbackData(TransactionModel.Callback.ENTER_TIME.name())
            .build();

    private static final InlineKeyboardButton ENTER_CATEGORY = InlineKeyboardButton
            .builder()
            .text("Категория")
            .callbackData(TransactionModel.Callback.ENTER_CATEGORY.name())
            .build();

    private static final InlineKeyboardButton ENTER_DESCRIPTION = InlineKeyboardButton
            .builder()
            .text("Описание")
            .callbackData(TransactionModel.Callback.ENTER_DESCRIPTION.name())
            .build();

    private static final InlineKeyboardButton COMPLETE = InlineKeyboardButton
            .builder()
            .text("✅Создать")
            .callbackData(TransactionModel.Callback.COMPLETE.name())
            .build();

    private static final InlineKeyboardButton CANCEL = InlineKeyboardButton
            .builder()
            .text("« Отмена")
            .callbackData(TransactionModel.Callback.BACK.name())
            .build();

    private final AccountService accountService;
    private final CurrencyService currencyService;

    public TransactionView(AccountService accountService, CurrencyService currencyService) {
        super(TransactionContext.CONTEXT_NAME);
        this.accountService = accountService;
        this.currencyService = currencyService;
    }

    @Override
    ViewResult renderImpl(TransactionContext context) {
        Optional<Long> favoriteAccountId = accountService.getFavoriteAccountId(context.getUserId());
        Account fromAccount = accountService.getAccount(context.getFromAccountId());
        Account toAccount = accountService.getAccount(context.getToAccountId());

        return new ViewResult(
                renderText(context, favoriteAccountId, fromAccount, toAccount),
                renderKeyboard(context, favoriteAccountId, fromAccount, toAccount)
        );
    }

    private String renderText(TransactionContext context, Optional<Long> favoriteAccountId, Account fromAccount, Account toAccount) {
        StringBuilder sb = new StringBuilder(100);

        sb.append("💸 Давай создадим перевод между счетами\n\n");

        sb.append("💳Со счёта: ")
                .append(Formatter.formatAccountName(favoriteAccountId, fromAccount))
                .append("\n")
                .append(Formatter.formatAmount(context.getFromAccountAmount(), fromAccount.getCurrency()))
                .append("\n");

        sb.append("\n");

        sb.append("💳На счёт: ")
                .append(Formatter.formatAccountName(favoriteAccountId, toAccount))
                .append("\n")
                .append(Formatter.formatAmount(context.getToAccountAmount(), toAccount.getCurrency()))
                .append("\n");

        sb.append("\n");

        if (fromAccount.getCurrency() == toAccount.getCurrency()) {
            if (!context.getFromAccountAmount().equals(context.getToAccountAmount())) {
                sb.append("⚠️⚠️Отличается сумма списания и зачисления⚠️⚠️\nНе забудь добавить в расход комиссию:\n")
                        .append(Formatter.formatAmount(
                                context.getToAccountAmount().subtract(context.getFromAccountAmount()),
                                fromAccount.getCurrency()
                        ))
                        .append("\n\n");
            }
        } else {
            CurrencyPair marketRate = currencyService.marketCurrencyPair(
                    fromAccount.getCurrency(),
                    toAccount.getCurrency()
            );
            sb.append("Рыночный курс конвертации:\n")
                    .append(marketRate.baseCurrency().getCurrencyCode())
                    .append("/")
                    .append(marketRate.quoteCurrency().getCurrencyCode())
                    .append(" ")
                    .append(Formatter.formatAmount(marketRate.rate(), marketRate.quoteCurrency()))
                    .append("\n");

            CurrencyPair userRate = currencyService.calcExchangeRate(
                    context.getFromAccountAmount(),
                    context.getToAccountAmount(),
                    fromAccount.getCurrency(),
                    toAccount.getCurrency()
            );
            sb.append("Твой курс конвертации:\n")
                    .append(userRate.baseCurrency().getCurrencyCode())
                    .append("/")
                    .append(userRate.quoteCurrency().getCurrencyCode())
                    .append(" ")
                    .append(Formatter.formatAmount(userRate.rate(), userRate.quoteCurrency()))
                    .append("\n");


            BigDecimal commission = currencyService.convert(context.getFromAccountAmount(), fromAccount.getCurrency(), toAccount.getCurrency())
                    .subtract(context.getToAccountAmount());

            sb.append("⚠️⚠️Не забудь добавить в расход комиссию конвертации⚠️⚠️\n\n");
        }

        sb.append(Formatter.getEmojiTime(context.getTime().toZonedDateTime()));
        sb.append("Время: ");
        sb.append(Formatter.format(context.getTime()));
        sb.append("\n\n");

        sb.append("⏺️Категория: ");
        sb.append(context.getCategory());
        sb.append("\n");

        sb.append("✍️Описание: ");
        sb.append(context.getDescription());
        sb.append("\n");

        sb.append("\n");
        sb.append("Введите число или выражение, чтобы изменить сумму со счёта списания");
        return sb.toString();
    }

    private InlineKeyboardMarkup renderKeyboard(TransactionContext context, Optional<Long> favoriteAccountId, Account fromAccount, Account toAccount) {
        return InlineKeyboardMarkup
                .builder()
                .keyboard(List.of(
                                new InlineKeyboardRow(
                                        InlineKeyboardButton
                                                .builder()
                                                .text("С " + Formatter.formatAccountName(favoriteAccountId, fromAccount))
                                                .callbackData(TransactionModel.Callback.CHANGE_FROM_ACCOUNT.name())
                                                .build(),
                                        InlineKeyboardButton
                                                .builder()
                                                .text(Formatter.formatAmount(context.getFromAccountAmount(), fromAccount.getCurrency()))
                                                .callbackData(TransactionModel.Callback.ENTER_FROM_AMOUNT.name())
                                                .build()
                                ),
                                new InlineKeyboardRow(
                                        InlineKeyboardButton
                                                .builder()
                                                .text("На " + Formatter.formatAccountName(favoriteAccountId, toAccount))
                                                .callbackData(TransactionModel.Callback.CHANGE_TO_ACCOUNT.name())
                                                .build(),
                                        InlineKeyboardButton
                                                .builder()
                                                .text(Formatter.formatAmount(context.getToAccountAmount(), toAccount.getCurrency()))
                                                .callbackData(TransactionModel.Callback.ENTER_TO_AMOUNT.name())
                                                .build()
                                ),
                                new InlineKeyboardRow(
                                        ENTER_TIME
                                ),
                                new InlineKeyboardRow(
                                        ENTER_CATEGORY,
                                        ENTER_DESCRIPTION
                                ),
                                new InlineKeyboardRow(
                                        COMPLETE
                                ),
                                new InlineKeyboardRow(
                                        CANCEL
                                )
                        )
                )
                .build();
    }
}
