package ru.psharaev.mymoney.bot.model;

import lombok.extern.slf4j.Slf4j;
import ru.psharaev.mymoney.bot.Presenter;
import ru.psharaev.mymoney.bot.context.Context;
import ru.psharaev.mymoney.bot.context.FlowContext;
import ru.psharaev.mymoney.bot.context.StartContext;
import ru.psharaev.mymoney.bot.util.Parser;
import ru.psharaev.mymoney.core.exception.MymoneyUserBadArgumentsException;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.CategoryService;
import ru.psharaev.mymoney.core.entity.Category;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.time.Instant;

import static ru.psharaev.mymoney.bot.model.ModelResult.notChanged;

@Slf4j
@Component
public final class StartModel extends AbstractModel<StartContext> {
    public static final String CREATE_FLOW = "FLOW";

    private final AccountService accountService;
    private final CategoryService categoryService;

    public StartModel(TelegramClient telegramClient, AccountService accountService, CategoryService categoryService) {
        super(telegramClient, StartContext.CONTEXT_NAME);
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    @Override
    ModelResult<Context> handleCallbackImpl(CallbackQuery callback, StartContext context) throws TelegramApiException {
        String data = callback.getData();
        return switch (data) {
            case CREATE_FLOW -> {
                yield ModelResult.editMessage(createFlow(context, context.getFavoriteAccountId(), BigDecimal.ZERO));
            }
            default -> {
                sendText(context.getChatId(), Presenter.OOPS_IMPOSSIBLE);
                yield notChanged();
            }
        };
    }

    @Override
    ModelResult<Context> handleMessageImpl(Message msg, StartContext context) throws TelegramApiException {
        if (context.getFavoriteAccountId() == null) {
            sendText(msg.getChatId(), "Не задан приоритетный счёт для быстрого ввода расхода");
            return notChanged();
        }
        String text = msg.getText();
        BigDecimal amount;
        try {
            amount = Parser.parseExpression(text);
        } catch (MymoneyUserBadArgumentsException e) {
            sendText(msg.getChatId(), "Не удалось разобрать число или выражение для быстрого ввода расхода. " + e.getMessage());
            log.error("Fail parse expression", e);
            return notChanged();
        }
        return ModelResult.editMessage(createFlow(context, context.getFavoriteAccountId(), amount.negate()));
    }

    private FlowContext createFlow(StartContext context, long accountId, BigDecimal amount) {
        return new FlowContext(
                context.getUserId(),
                context.getChatId(),
                context.getMessageId(),
                accountId,
                true,
                amount,
                accountService.getAccount(accountId).getCurrency().getCurrencySymbol(),
                Instant.now().atOffset(context.getTimezone()),
                getCategory(context.getFavoriteCategoryFlowId()),
                "",
                ""
        );
    }

    private String getCategory(Long categoryId) {
        if (categoryId == null) {
            return "";
        }
        return categoryService.findCategory(categoryId)
                .map(Category::getName)
                .orElse("");
    }
}
