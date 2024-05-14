package ru.psharaev.mymoney.bot.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.psharaev.mymoney.bot.Presenter;
import ru.psharaev.mymoney.bot.context.Context;
import ru.psharaev.mymoney.bot.context.FlowContext;
import ru.psharaev.mymoney.bot.context.StartContext;
import ru.psharaev.mymoney.bot.model.command.StartCommand;
import ru.psharaev.mymoney.bot.util.Formatter;
import ru.psharaev.mymoney.bot.util.Parser;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.FlowService;
import ru.psharaev.mymoney.core.entity.Account;
import ru.psharaev.mymoney.core.exception.MymoneyUserBadArgumentsException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.psharaev.mymoney.bot.model.FlowModel.Callback.*;
import static ru.psharaev.mymoney.bot.model.ModelResult.notChanged;

@Slf4j
@Component
public final class FlowModel extends AbstractModel<FlowContext> {
    public enum Callback {
        CHANGE_SIGN,
        CHANGE_ACCOUNT,
        SET_ACCOUNT,
        ENTER_TIME,
        ENTER_CATEGORY,
        ENTER_DESCRIPTION,
        COMPLETE,
        BACK
    }

    private final FlowService flowService;
    private final StartCommand startCommand;
    private final AccountService accountService;

    public FlowModel(TelegramClient telegramClient, FlowService flowService, StartCommand startCommand, AccountService accountService) {
        super(telegramClient, FlowContext.CONTEXT_NAME);
        this.flowService = flowService;
        this.startCommand = startCommand;
        this.accountService = accountService;
    }

    @Override
    protected ModelResult<Context> handleCallbackImpl(CallbackQuery callback, FlowContext context) throws TelegramApiException {
        String[] callbackData = callback.getData().split(":");
        final String callbackPayload = callbackData.length == 1 ? "" : callbackData[1];
        Callback data = Callback.valueOf(callbackData[0]);
        return switch (data) {
            case CHANGE_SIGN -> {
                if (context.getAmount().signum() == 0) {
                    sendText(context.getChatId(), "Ой, расход равен нулю, поэтому не могу поменять на доход. Введи число");
                    yield ModelResult.notChanged();
                }
                context.setAmount(context.getAmount().negate());
                yield ModelResult.editMessage(context);
            }
            case SET_ACCOUNT -> {
                context.setAccountId(Long.decode(callbackPayload));
                yield ModelResult.editMessage(context);
            }
            case CHANGE_ACCOUNT -> {
                ArrayList<InlineKeyboardRow> keyboard = new ArrayList<>();
                Optional<Long> favoriteAccountId = accountService.getFavoriteAccountId(context.getUserId());
                List<Account> allAccounts = accountService.getAllAccounts(context.getUserId());
                for (Account account : allAccounts) {
                    keyboard.add(new InlineKeyboardRow(
                                    InlineKeyboardButton.builder()
                                            .text(Formatter.formatAccountName(favoriteAccountId, account))
                                            .callbackData("%s:0x%x".formatted(Callback.SET_ACCOUNT.name(), account.getAccountId()))
                                            .build()
                            )
                    );
                }
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(context.getChatId())
                        .text("Выбери счёт")
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboard(
                                                keyboard
                                        )
                                        .build()
                        )
                        .build();
                Message execute = telegramClient.execute(sendMessage);
                context.setMessageId(execute.getMessageId());
                yield ModelResult.replaceContext(context);
            }
            case ENTER_TIME -> {
                context.setEnterData(ENTER_TIME.name());
                sendText(context.getChatId(), """
                        Ок. Отправь мне дату и время.
                        Сегодня:
                        %s""".formatted(Parser.nowByPattern(context.getTime().getOffset())));
                yield ModelResult.replaceContext(context);
            }
            case ENTER_CATEGORY -> {
                context.setEnterData(ENTER_CATEGORY.name());
                sendText(context.getChatId(), "Ок. Отправь мне название категории");
                yield ModelResult.replaceContext(context);
            }
            case ENTER_DESCRIPTION -> {
                context.setEnterData(ENTER_DESCRIPTION.name());
                sendText(context.getChatId(), "Ок. Отправь мне описание");
                yield ModelResult.replaceContext(context);
            }
            case COMPLETE -> {
                if (context.getAmount().signum() == 0) {
                    sendText(context.getChatId(), "Расход не может быть равен нулю. Введи число");
                    yield ModelResult.notChanged();
                }
                flowService.createFlow(
                        context.getAccountId(),
                        context.getAmount(),
                        context.getTime(),
                        context.getCategory(),
                        context.getDescription()
                );
                sendText(context.getChatId(), "Успешно создано!");
                StartContext startContext = startCommand.defaultContext(callback.getMessage().getChatId());
                startContext.setMessageId(callback.getMessage().getMessageId());
                yield ModelResult.sendMessage(startContext);
            }
            case BACK -> {
                StartContext startContext = startCommand.defaultContext(callback.getMessage().getChatId());
                startContext.setMessageId(callback.getMessage().getMessageId());
                yield ModelResult.editMessage(startContext);
            }
        };
    }

    @Override
    protected ModelResult<Context> handleMessageImpl(Message message, FlowContext context) throws TelegramApiException {
        String text = message.getText();

        if (!context.getEnterData().isEmpty()) {
            return switch (Callback.valueOf(context.getEnterData())) {
                case ENTER_CATEGORY -> {
                    if (text.length() > 50) {
                        sendText(context.getChatId(), "Извини, максимальная длина категории 50 символов. Попробуй снова");
                        yield ModelResult.notChanged();
                    }
                    context.setCategory(text);
                    context.setEnterData("");
                    yield ModelResult.sendMessage(context);
                }
                case ENTER_TIME -> {
                    OffsetDateTime time;
                    try {
                        time = Parser.parseDateTime(text, context.getTime().getOffset());
                    } catch (DateTimeParseException e) {
                        sendText(context.getChatId(), "Не удалось разобрать, попробуй снова");
                        yield ModelResult.notChanged();
                    }
                    context.setTime(time);
                    context.setEnterData("");
                    yield ModelResult.sendMessage(context);
                }
                case ENTER_DESCRIPTION -> {
                    if (text.length() > 200) {
                        sendText(context.getChatId(), "Извини, максимальная длина описания 200 символов. Попробуй снова");
                        yield ModelResult.notChanged();
                    }
                    context.setDescription(text);
                    context.setEnterData("");
                    yield ModelResult.sendMessage(context);
                }
                default -> {
                    log.error("Impossible context: {}", context);
                    sendText(context.getChatId(), Presenter.OOPS_IMPOSSIBLE);
                    yield ModelResult.notChanged();
                }
            };
        } else {
            BigDecimal amount;
            try {
                amount = Parser.parseExpression(text);
            } catch (MymoneyUserBadArgumentsException e) {
                sendText(message.getChatId(), "Не удалось разобрать число или выражение, попробуй снова");
                log.error("Fail parse expression", e);
                return notChanged();
            }

            if (context.getAmount().signum() <= 0) {
                amount = amount.negate();
            }

            context.setAmount(amount);
            return ModelResult.sendMessage(context);
        }
    }
}
