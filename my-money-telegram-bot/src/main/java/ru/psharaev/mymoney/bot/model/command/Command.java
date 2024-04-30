package ru.psharaev.mymoney.bot.model.command;

import ru.psharaev.mymoney.bot.context.Context;
import ru.psharaev.mymoney.bot.model.ModelResult;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface Command {

    String getCommand();

    String getDescription();

    ModelResult<Context> handleMessage(Message msg) throws TelegramApiException;

    default boolean isVisible() {
        return true;
    }
}
