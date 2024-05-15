package ru.psharaev.mymoney.bot.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.psharaev.mymoney.bot.Presenter;
import ru.psharaev.mymoney.bot.context.*;
import ru.psharaev.mymoney.bot.util.Formatter;
import ru.psharaev.mymoney.bot.util.Parser;
import ru.psharaev.mymoney.core.CategoryService;
import ru.psharaev.mymoney.core.entity.Category;
import ru.psharaev.mymoney.core.entity.Currency;
import ru.psharaev.mymoney.core.excel.ExcelGenerator;
import ru.psharaev.mymoney.core.exception.MymoneyUserBadArgumentsException;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;

import static ru.psharaev.mymoney.bot.model.ModelResult.notChanged;

@Slf4j
@Component
public final class StartModel extends AbstractModel<StartContext> {
    private final ExcelGenerator excelGenerator;

    public enum Callback {
        CREATE_FLOW,
        CREATE_TRANSACTION,
        ACCOUNT_MANAGEMENT,
        UNLOAD_TABLE
    }

    private final CategoryService categoryService;

    public StartModel(TelegramClient telegramClient, CategoryService categoryService, ExcelGenerator excelGenerator) {
        super(telegramClient, StartContext.CONTEXT_NAME);
        this.categoryService = categoryService;
        this.excelGenerator = excelGenerator;
    }

    @Override
    ModelResult<Context> handleCallbackImpl(CallbackQuery callback, StartContext context) throws TelegramApiException {
        String data = callback.getData();
        return switch (Callback.valueOf(data)) {
            case CREATE_FLOW -> {
                if (context.getFavoriteAccountId() == null) {
                    sendText(context.getChatId(), "Нет предпочитаемого счёта, для создания расхода/дохода. Зайди в настройки аккаунта и назначь предпочитаемый счёт.");
                    yield ModelResult.notChanged();
                }
                yield ModelResult.editMessage(createFlow(context, context.getFavoriteAccountId(), BigDecimal.ZERO));
            }
            case CREATE_TRANSACTION -> {
                if (context.getFavoriteAccountId() == null) {
                    sendText(context.getChatId(), "Нет предпочитаемого счёта, для создания перевода. Зайди в настройки аккаунта и назначь предпочитаемый счёт.");
                    yield ModelResult.notChanged();
                }
                yield ModelResult.editMessage(createTransaction(context));
            }
            case ACCOUNT_MANAGEMENT -> {
                yield ModelResult.editMessage(createAccountManagement(context));
            }
            case UNLOAD_TABLE -> {
                sendText(context.getChatId(), "Начал выгружать данные. Это займёт какое-то время");
                InputStream table = excelGenerator.generate(context.getUserId());
                String fileName = Formatter.getCurrentDate() + ".xlsx";
                SendDocument sendDocument = SendDocument.builder()
                        .chatId(context.getChatId())
                        .caption("Твои данные готовы!")
                        .document(new InputFile(table, fileName))
                        .build();
                telegramClient.execute(sendDocument);
                yield ModelResult.notChanged();
            }
            default -> {
                sendText(context.getChatId(), Presenter.OOPS_IMPOSSIBLE);
                yield notChanged();
            }
        };
    }

    private TransactionContext createTransaction(StartContext context) {
        return new TransactionContext(
                context.getUserId(),
                context.getChatId(),
                context.getMessageId(),
                context.getFavoriteAccountId(),
                context.getFavoriteAccountId(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Instant.now().atOffset(context.getTimezone()),
                getCategory(context.getFavoriteCategoryFlowId()),
                "",
                ""
        );
    }

    private AccountManagementContext createAccountManagement(StartContext context) {
        return new AccountManagementContext(
                context.getUserId(),
                context.getChatId(),
                context.getMessageId(),
                context.getLanguageCode(),
                "",
                "",
                Currency.RUB
        );
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
            sendText(msg.getChatId(), "Не удалось разобрать число или выражение для быстрого ввода расхода.");
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
                amount,
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
