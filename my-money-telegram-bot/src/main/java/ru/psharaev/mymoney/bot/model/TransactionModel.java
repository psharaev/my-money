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
import ru.psharaev.mymoney.bot.context.StartContext;
import ru.psharaev.mymoney.bot.context.TransactionContext;
import ru.psharaev.mymoney.bot.model.command.StartCommand;
import ru.psharaev.mymoney.bot.util.Formatter;
import ru.psharaev.mymoney.bot.util.Parser;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.CurrencyService;
import ru.psharaev.mymoney.core.FlowService;
import ru.psharaev.mymoney.core.TransactionService;
import ru.psharaev.mymoney.core.entity.Account;
import ru.psharaev.mymoney.core.exception.MymoneyUserBadArgumentsException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.psharaev.mymoney.bot.model.ModelResult.notChanged;

@Slf4j
@Component
public final class TransactionModel extends AbstractModel<TransactionContext> {
    public enum Callback {
        CHANGE_FROM_ACCOUNT,
        CHANGE_TO_ACCOUNT,

        SET_FROM_ACCOUNT,
        SET_TO_ACCOUNT,

        ENTER_FROM_AMOUNT,
        ENTER_TO_AMOUNT,

        ENTER_TIME,
        ENTER_CATEGORY,
        ENTER_DESCRIPTION,

        COMPLETE,
        BACK
    }

    private final FlowService flowService;
    private final StartCommand startCommand;
    private final AccountService accountService;
    private final CurrencyService currencyService;
    private final TransactionService transactionService;

    public TransactionModel(TelegramClient telegramClient, FlowService flowService, StartCommand startCommand, AccountService accountService, CurrencyService currencyService, TransactionService transactionService) {
        super(telegramClient, TransactionContext.CONTEXT_NAME);
        this.flowService = flowService;
        this.startCommand = startCommand;
        this.accountService = accountService;
        this.currencyService = currencyService;
        this.transactionService = transactionService;
    }

    @Override
    protected ModelResult<Context> handleCallbackImpl(CallbackQuery callback, TransactionContext context) throws TelegramApiException {
        String[] callbackData = callback.getData().split(":");
        final String callbackPayload = callbackData.length == 1 ? "" : callbackData[1];
        Callback data = Callback.valueOf(callbackData[0]);
        return switch (data) {
            case CHANGE_FROM_ACCOUNT -> {
                ArrayList<InlineKeyboardRow> keyboard = new ArrayList<>();
                Optional<Long> favoriteAccountId = accountService.getFavoriteAccountId(context.getUserId());
                List<Account> allAccounts = accountService.getAllAccounts(context.getUserId());
                for (Account account : allAccounts) {
                    keyboard.add(new InlineKeyboardRow(
                                    InlineKeyboardButton.builder()
                                            .text(Formatter.formatAccountName(favoriteAccountId, account))
                                            .callbackData("%s:0x%x".formatted(Callback.SET_FROM_ACCOUNT.name(), account.getAccountId()))
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
            case CHANGE_TO_ACCOUNT -> {
                ArrayList<InlineKeyboardRow> keyboard = new ArrayList<>();
                Optional<Long> favoriteAccountId = accountService.getFavoriteAccountId(context.getUserId());
                List<Account> allAccounts = accountService.getAllAccounts(context.getUserId());
                for (Account account : allAccounts) {
                    keyboard.add(new InlineKeyboardRow(
                                    InlineKeyboardButton.builder()
                                            .text(Formatter.formatAccountName(favoriteAccountId, account))
                                            .callbackData("%s:0x%x".formatted(Callback.SET_TO_ACCOUNT.name(), account.getAccountId()))
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
            case SET_FROM_ACCOUNT -> {
                context.setFromAccountId(Long.decode(callbackPayload));
                yield ModelResult.editMessage(context);
            }
            case SET_TO_ACCOUNT -> {
                context.setToAccountId(Long.decode(callbackPayload));
                yield ModelResult.editMessage(context);
            }
            case ENTER_FROM_AMOUNT -> {
                context.setEnterData(Callback.ENTER_FROM_AMOUNT.name());
                sendText(context.getChatId(), "Ок. Напиши сумму списания.");
                yield ModelResult.replaceContext(context);
            }
            case ENTER_TO_AMOUNT -> {
                context.setEnterData(Callback.ENTER_TO_AMOUNT.name());
                sendText(context.getChatId(), "Ок. Напиши сумму зачисления.");
                yield ModelResult.replaceContext(context);
            }
            case ENTER_TIME -> {
                context.setEnterData(Callback.ENTER_TIME.name());
                sendText(context.getChatId(), """
                        Ок. Отправь мне дату и время.
                        Сегодня:
                        %s""".formatted(Parser.nowByPattern(context.getTime().getOffset())));
                yield ModelResult.replaceContext(context);
            }
            case ENTER_CATEGORY -> {
                context.setEnterData(Callback.ENTER_CATEGORY.name());
                sendText(context.getChatId(), "Ок. Отправь мне название категории");
                yield ModelResult.replaceContext(context);
            }
            case ENTER_DESCRIPTION -> {
                context.setEnterData(Callback.ENTER_DESCRIPTION.name());
                sendText(context.getChatId(), "Ок. Отправь мне описание");
                yield ModelResult.replaceContext(context);
            }
            case COMPLETE -> {
                if (context.getFromAccountId() == context.getToAccountId()) {
                    sendText(context.getChatId(), "Упс! Указан один и тот же счёт.");
                    yield notChanged();
                }

                if (context.getFromAccountAmount().signum() <= 0) {
                    sendText(context.getChatId(), "Упс! Сумма со счёта снятия должна быть больше нуля.");
                    yield notChanged();
                }

                if (context.getToAccountAmount().signum() <= 0) {
                    sendText(context.getChatId(), "Упс! Сумма счёта пополнения должна быть больше нуля.");
                    yield notChanged();
                }

                transactionService.createTransaction(
                        context.getFromAccountId(),
                        context.getToAccountId(),
                        context.getFromAccountAmount(),
                        context.getToAccountAmount(),
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
    protected ModelResult<Context> handleMessageImpl(Message message, TransactionContext context) throws TelegramApiException {
        String text = message.getText();

        if (!context.getEnterData().isEmpty()) {
            return switch (Callback.valueOf(context.getEnterData())) {
                case ENTER_FROM_AMOUNT -> {
                    yield enterFromAmount(context, text);
                }
                case ENTER_TO_AMOUNT -> {
                    yield enterToAmount(context, text);
                }
                case ENTER_CATEGORY -> {
                    if (text.length() > 50) {
                        sendText(context.getChatId(), "Извини, максимальная длина категории 50 символов. Попробуй снова");
                        yield notChanged();
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
                        yield notChanged();
                    }
                    context.setTime(time);
                    context.setEnterData("");
                    yield ModelResult.sendMessage(context);
                }
                case ENTER_DESCRIPTION -> {
                    if (text.length() > 200) {
                        sendText(context.getChatId(), "Извини, максимальная длина описания 200 символов. Попробуй снова");
                        yield notChanged();
                    }
                    context.setDescription(text);
                    context.setEnterData("");
                    yield ModelResult.sendMessage(context);
                }
                default -> {
                    log.error("Impossible context: {}", context);
                    sendText(context.getChatId(), Presenter.OOPS_IMPOSSIBLE);
                    yield notChanged();
                }
            };
        } else {
            return enterFromAmount(context, text);
        }
    }

    private ModelResult<Context> enterFromAmount(TransactionContext context, String text) throws TelegramApiException {
        BigDecimal fromAmount;
        try {
            fromAmount = Parser.parseExpression(text);
        } catch (MymoneyUserBadArgumentsException e) {
            sendText(context.getChatId(), "Не удалось разобрать число или выражение, попробуй снова");
            log.error("Fail parse expression", e);
            return notChanged();
        }

        if (fromAmount.signum() <= 0) {
            sendText(context.getChatId(), "Ожидалось число больше нуля. Попробуй снова");
            return notChanged();
        }

        if (context.getToAccountAmount().signum() == 0) {
            Account fromAccount = accountService.getAccount(context.getFromAccountId());
            Account toAccount = accountService.getAccount(context.getToAccountId());

            BigDecimal toAmount = currencyService.convert(fromAmount, fromAccount.getCurrency(), toAccount.getCurrency());
            context.setToAccountAmount(toAmount);
        }


        context.setFromAccountAmount(fromAmount);
        context.setEnterData("");
        return ModelResult.sendMessage(context);
    }

    private ModelResult<Context> enterToAmount(TransactionContext context, String text) throws TelegramApiException {
        BigDecimal toAmount;
        try {
            toAmount = Parser.parseExpression(text);
        } catch (MymoneyUserBadArgumentsException e) {
            sendText(context.getChatId(), "Не удалось разобрать число или выражение, попробуй снова");
            log.error("Fail parse expression", e);
            return notChanged();
        }

        if (toAmount.signum() <= 0) {
            sendText(context.getChatId(), "Ожидалось число больше нуля. Попробуй снова");
            return notChanged();
        }

        if (context.getToAccountAmount().signum() == 0) {
            Account fromAccount = accountService.getAccount(context.getFromAccountId());
            Account toAccount = accountService.getAccount(context.getToAccountId());

            BigDecimal fromAmount = currencyService.convert(toAmount, toAccount.getCurrency(), fromAccount.getCurrency());
            context.setFromAccountAmount(fromAmount);
        }

        context.setToAccountAmount(toAmount);
        context.setEnterData("");
        return ModelResult.sendMessage(context);
    }
}
