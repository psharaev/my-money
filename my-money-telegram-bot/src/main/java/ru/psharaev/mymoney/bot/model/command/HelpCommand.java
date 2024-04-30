package ru.psharaev.mymoney.bot.model.command;

import lombok.RequiredArgsConstructor;
import ru.psharaev.mymoney.bot.context.Context;
import ru.psharaev.mymoney.bot.model.ModelResult;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class HelpCommand implements Command {
    private final TelegramClient telegramClient;

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Помощь";
    }

    @Override
    public ModelResult<Context> handleMessage(Message msg) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(msg.getChatId())
                .text("""
                        Скоро здесь будет помощь
                        """)
                .build();
        telegramClient.execute(sendMessage);
        return ModelResult.deleteContext();
    }
}
