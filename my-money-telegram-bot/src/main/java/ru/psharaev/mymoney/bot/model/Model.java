package ru.psharaev.mymoney.bot.model;

import ru.psharaev.mymoney.bot.context.Context;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface Model {
    String getContextName();

    ModelResult<Context> handleCallback(CallbackQuery callback, Context context) throws TelegramApiException;

    ModelResult<Context> handleMessage(Message msg, Context context) throws TelegramApiException;
}
