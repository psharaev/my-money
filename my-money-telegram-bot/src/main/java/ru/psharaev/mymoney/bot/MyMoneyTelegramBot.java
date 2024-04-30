package ru.psharaev.mymoney.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyMoneyTelegramBot implements LongPollingSingleThreadUpdateConsumer {
    private final Presenter presenter;

    @Override
    public void consume(Update update) {
        try {
            presenter.handleUpdate(update);
        } catch (TelegramApiException e) {
            log.error("Fail handle update", e);
        }
    }
}
