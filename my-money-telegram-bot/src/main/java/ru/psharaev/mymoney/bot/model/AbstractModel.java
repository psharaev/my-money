package ru.psharaev.mymoney.bot.model;

import ru.psharaev.mymoney.bot.context.Context;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

abstract class AbstractModel<T extends Context> implements Model {
    protected final TelegramClient telegramClient;
    private final String contextName;

    AbstractModel(TelegramClient telegramClient, String contextName) {
        this.telegramClient = telegramClient;
        this.contextName = contextName;
    }

    void sendText(long chatId, String text) throws TelegramApiException {
        SendMessage msg = new SendMessage(Long.toString(chatId), text);
        telegramClient.execute(msg);
    }

    @Override
    public final String getContextName() {
        return contextName;
    }

    @Override
    public final ModelResult<Context> handleMessage(Message msg, Context context) throws TelegramApiException {
        return handleMessageImpl(msg, (T) context);
    }

    @Override
    public final ModelResult<Context> handleCallback(CallbackQuery callback, Context context) throws TelegramApiException {
        return handleCallbackImpl(callback, (T) context);
    }

    abstract ModelResult<Context> handleMessageImpl(Message msg, T context) throws TelegramApiException;


    abstract ModelResult<Context> handleCallbackImpl(CallbackQuery callback, T context) throws TelegramApiException;
}
