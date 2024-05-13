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
import ru.psharaev.mymoney.bot.context.AccountManagementContext;
import ru.psharaev.mymoney.bot.context.Context;
import ru.psharaev.mymoney.bot.context.StartContext;
import ru.psharaev.mymoney.bot.model.command.StartCommand;
import ru.psharaev.mymoney.core.AccountService;
import ru.psharaev.mymoney.core.UserService;
import ru.psharaev.mymoney.core.entity.Currency;

import java.util.List;

import static ru.psharaev.mymoney.bot.model.ModelResult.oopsImpossible;

@Slf4j
@Component
public final class AccountManagementModel extends AbstractModel<AccountManagementContext> {
    public enum Callback {
        CREATE_ACCOUNT,
        SET_FAVORITE_ACCOUNT,
        RENAME_ACCOUNT,
        IGNORE_BUCKET_NAME,

        ENTER_RENAME_ACCOUNT,
        ENTER_CREATE_ACCOUNT_NAME,
        ENTER_CREATE_ACCOUNT_CURRENCY,

        DELETE_ACCOUNT,
        BACK
    }

    private final AccountService accountService;
    private final UserService userService;
    private final StartCommand startCommand;

    public AccountManagementModel(TelegramClient telegramClient, AccountService accountService, StartCommand startCommand, UserService userService) {
        super(telegramClient, AccountManagementContext.CONTEXT_NAME);
        this.accountService = accountService;
        this.startCommand = startCommand;
        this.userService = userService;
    }

    @Override
    protected ModelResult<Context> handleCallbackImpl(CallbackQuery callback, AccountManagementContext context) throws TelegramApiException {
        String[] callbackData = callback.getData().split(":");
        final String callbackPayload = callbackData.length == 1 ? "" : callbackData[1];
        Callback data = Callback.valueOf(callbackData[0]);
        return switch (data) {
            case SET_FAVORITE_ACCOUNT -> {
                Long accountId = Long.decode(callbackPayload);
                userService.setUserFavoriteAccount(context.getUserId(), accountId);
                yield ModelResult.editMessage(context);
            }
            case DELETE_ACCOUNT -> {
                Long accountId = Long.decode(callbackPayload);
                accountService.deleteAccount(accountId);
                yield ModelResult.editMessage(context);
            }
            case RENAME_ACCOUNT -> {
                context.setEnterData("%s:%s".formatted(Callback.ENTER_RENAME_ACCOUNT.name(), callbackPayload));
                sendText(context.getChatId(), "Ок, напиши новое имя для счёта: %s.".formatted(accountService.getAccount(Long.decode(callbackPayload)).getName()));
                yield ModelResult.replaceContext(context);
            }
            case CREATE_ACCOUNT -> {
                context.setEnterData(Callback.ENTER_CREATE_ACCOUNT_NAME.name());
                sendText(context.getChatId(), "Ок, напиши имя нового счёта.");
                yield ModelResult.replaceContext(context);
            }
            case BACK -> {
                StartContext startContext = startCommand.defaultContext(callback.getMessage().getChatId());
                startContext.setMessageId(callback.getMessage().getMessageId());
                yield ModelResult.editMessage(startContext);
            }
            case ENTER_CREATE_ACCOUNT_CURRENCY -> {
                accountService.createAccount(context.getUserId(), context.getCreateAccountName(), Currency.valueOf(callbackPayload));
                sendText(context.getChatId(), "Успешно, создано!");
                context.setEnterData("");
                yield ModelResult.sendMessage(context);
            }

            case IGNORE_BUCKET_NAME -> ModelResult.notChanged();

            default -> oopsImpossible();
        };
    }

    @Override
    protected ModelResult<Context> handleMessageImpl(Message message, AccountManagementContext context) throws TelegramApiException {
        String text = message.getText();

        if (!context.getEnterData().isEmpty()) {
            String[] callbackData = context.getEnterData().split(":");
            final String callbackPayload = callbackData.length == 1 ? "" : callbackData[1];
            Callback callback = Callback.valueOf(callbackData[0]);
            return switch (callback) {
                case ENTER_RENAME_ACCOUNT -> {
                    context.setEnterData("");
                    accountService.renameAccount(Long.decode(callbackPayload), text);
                    sendText(context.getChatId(), "Успешно, переименовано!");
                    yield ModelResult.sendMessage(context);
                }
                case ENTER_CREATE_ACCOUNT_NAME -> {
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(message.getChatId())
                            .text("Ок, теперь выбери валюту счёта")
                            .replyMarkup(InlineKeyboardMarkup
                                    .builder()
                                    .keyboard(
                                            List.of(
                                                    new InlineKeyboardRow(
                                                            currency(Currency.RUB)
                                                    ),
                                                    new InlineKeyboardRow(
                                                            currency(Currency.USD),
                                                            currency(Currency.HKD)
                                                    ),
                                                    new InlineKeyboardRow(
                                                            currency(Currency.EUR),
                                                            currency(Currency.CNY)
                                                    )
                                            )
                                    )
                                    .build()
                            )
                            .build();
                    Message execute = telegramClient.execute(sendMessage);
                    context.setMessageId(execute.getMessageId());
                    context.setCreateAccountName(text);
                    yield ModelResult.replaceContext(context);
                }
                default -> oopsImpossible();
            };
        }

        return ModelResult.oopsImpossible();
    }

    private static InlineKeyboardButton currency(Currency currency) {
        return InlineKeyboardButton
                .builder()
                .text(currency.getCurrencyCode() + " " + currency.getCurrencySymbol())
                .callbackData(AccountManagementModel.Callback.ENTER_CREATE_ACCOUNT_CURRENCY.name() + ":" + currency.name())
                .build();
    }
}
