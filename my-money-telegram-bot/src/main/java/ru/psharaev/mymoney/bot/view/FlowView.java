package ru.psharaev.mymoney.bot.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.psharaev.mymoney.bot.context.FlowContext;
import ru.psharaev.mymoney.bot.model.FlowModel;
import ru.psharaev.mymoney.bot.util.Formatter;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.entity.Account;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class FlowView extends AbstractView<FlowContext> {
    private static final InlineKeyboardButton CHANGE_ACCOUNT = InlineKeyboardButton
            .builder()
            .text("Поменять счёт")
            .callbackData(FlowModel.Callback.CHANGE_ACCOUNT.name())
            .build();

    private static final InlineKeyboardButton ENTER_TIME = InlineKeyboardButton
            .builder()
            .text("Время")
            .callbackData(FlowModel.Callback.ENTER_TIME.name())
            .build();

    private static final InlineKeyboardButton CATEGORY = InlineKeyboardButton
            .builder()
            .text("Категория")
            .callbackData(FlowModel.Callback.ENTER_CATEGORY.name())
            .build();

    private static final InlineKeyboardButton DESCRIPTION = InlineKeyboardButton
            .builder()
            .text("Описание")
            .callbackData(FlowModel.Callback.ENTER_DESCRIPTION.name())
            .build();

    private static final InlineKeyboardButton COMPLETE = InlineKeyboardButton
            .builder()
            .text("✅Создать")
            .callbackData(FlowModel.Callback.COMPLETE.name())
            .build();

    private static final InlineKeyboardButton CANCEL = InlineKeyboardButton
            .builder()
            .text("« Отмена")
            .callbackData(FlowModel.Callback.BACK.name())
            .build();

    private final AccountService accountService;

    public FlowView(AccountService accountService) {
        super(FlowContext.CONTEXT_NAME);
        this.accountService = accountService;
    }

    @Override
    ViewResult renderImpl(FlowContext context) {
        return new ViewResult(
                renderText(context),
                renderKeyboard(context)
        );
    }

    private String renderText(FlowContext context) {
        StringBuilder sb = new StringBuilder(100);

        sb.append("💸 Давай создадим денежный поток\n\n");

        sb.append("💳Счёт: ");
        Account account = accountService.getAccount(context.getAccountId());
        Optional<Long> favoriteAccountId = accountService.getFavoriteAccountId(context.getUserId());
        sb.append(Formatter.formatAccountName(favoriteAccountId, account));
        sb.append("\n");

        if (context.getAmount().signum() <= 0) {
            sb.append("🤬Расход: ");
        } else {
            sb.append("🤑Доход: ");
        }

        sb.append(Formatter.formatAmount(context.getAmount().abs(), account.getCurrency()));
        sb.append("\n");

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
        sb.append("Введите число или выражение, чтобы изменить ").append(context.getAmount().signum() <= 0 ? "расход" : "доход");
        return sb.toString();
    }

    private InlineKeyboardMarkup renderKeyboard(FlowContext context) {
        return InlineKeyboardMarkup
                .builder()
                .keyboard(List.of(
                                new InlineKeyboardRow(
                                        InlineKeyboardButton
                                                .builder()
                                                .text("Поменять на " + (context.getAmount().signum() <= 0 ? "доход" : "расход"))
                                                .callbackData(FlowModel.Callback.CHANGE_SIGN.name())
                                                .build()
                                ),
                                new InlineKeyboardRow(
                                        CHANGE_ACCOUNT,
                                        ENTER_TIME
                                ),
                                new InlineKeyboardRow(
                                        CATEGORY,
                                        DESCRIPTION
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
