package ru.psharaev.mymoney.bot.model.command;

import lombok.RequiredArgsConstructor;
import ru.psharaev.mymoney.bot.context.Context;
import ru.psharaev.mymoney.bot.context.StartContext;
import ru.psharaev.mymoney.bot.model.ModelResult;
import ru.psharaev.mymoney.core.UserService;
import ru.psharaev.mymoney.core.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneOffset;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class StartCommand implements Command {
    private final TelegramClient telegramClient;
    private final UserService userService;

    private static final Map<String, ZoneOffset> timezones = Map.of(
            "ru", ZoneOffset.ofHours(3)
    );


    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "Главное меню";
    }

    public StartContext defaultContext(long chatId) {
        return defaultContext(chatId, ZoneOffset.UTC, "ru");
    }

    public StartContext defaultContext(long chatId, ZoneOffset timezone, String languageCode) {
        User user = userService.getOrCreateTelegramUser(chatId, languageCode, timezone);
        return new StartContext(
                user.getUserId(),
                chatId,
                -1,
                user.getTimezone(),
                user.getLanguageCode(),
                user.getFavoriteAccountId(),
                user.getFavoriteCategoryFlowId(),
                user.getFavoriteCategoryTransactionId()
        );
    }

    @Override
    public ModelResult<Context> handleMessage(Message msg) {
        String languageCode = msg.getFrom().getLanguageCode();
        ZoneOffset timezone = timezones.getOrDefault(languageCode, ZoneOffset.UTC);
        return ModelResult.sendMessage(
                defaultContext(msg.getChatId(), timezone, languageCode)
        );
    }
}
