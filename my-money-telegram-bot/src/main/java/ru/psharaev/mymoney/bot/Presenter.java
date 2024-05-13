package ru.psharaev.mymoney.bot;

import lombok.extern.slf4j.Slf4j;
import ru.psharaev.mymoney.bot.repository.ContextRepository;
import ru.psharaev.mymoney.bot.context.Context;
import ru.psharaev.mymoney.bot.model.Model;
import ru.psharaev.mymoney.bot.model.ModelResult;
import ru.psharaev.mymoney.bot.model.command.Command;
import ru.psharaev.mymoney.bot.view.View;
import ru.psharaev.mymoney.bot.view.ViewResult;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Presenter {
    public static final String OOPS_I_DONT_KNOW = "Упс! Я не знаю что от меня ожидалось как насчёт /start?";
    public static final String OOPS_IMPOSSIBLE = "Упс! Я думал данное состояние невозможно, но ты это сделал🎉 Я не знаю как мне реагировать на твоё действие, похоже это просто не работает";

    private final TelegramClient telegramClient;
    private final Map<String, Command> commands;
    private final ContextRepository contextRepository;
    private final Map<String, Model> models;
    private final Map<String, View> views;

    public Presenter(TelegramClient telegramClient,
                     ContextRepository contextRepository,
                     List<Command> commands,
                     List<Model> models,
                     List<View> views) {
        this.telegramClient = telegramClient;
        this.contextRepository = contextRepository;

        this.commands = commands.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Command::getCommand,
                        Function.identity()
                ));

        this.models = models.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Model::getContextName,
                        Function.identity()
                ));

        this.views = views.stream()
                .collect(Collectors.toUnmodifiableMap(
                        View::getContextName,
                        Function.identity()
                ));

        try {
            telegramClient.execute(new SetMyCommands(
                    commands.stream()
                            .filter(Command::isVisible)
                            .map(c -> new BotCommand(c.getCommand(), c.getDescription()))
                            .toList()
            ));
        } catch (TelegramApiException e) {
            log.error("Fail set bot commands", e);
        }
    }

    public void handleTextMessage(Update update) throws TelegramApiException {
        Message msg = update.getMessage();
        String text = msg.getText();
        long chatId = msg.getChatId();
        if (text.startsWith("/")) {
            Command command = commands.get(text);
            if (command == null) {
                sendText(chatId, "Неизвестная команда");
                return;
            }
            contextRepository.delete(chatId);
            ModelResult<Context> result = command.handleMessage(msg);
            processHandleResult(chatId, result);
        } else {
            Optional<Context> contextOptional = contextRepository.get(chatId);
            if (contextOptional.isEmpty()) {
                sendText(chatId, OOPS_I_DONT_KNOW);
                return;
            }
            Context context = contextOptional.get();
            ModelResult<Context> result = models.get(context.getContextName()).handleMessage(msg, context);
            processHandleResult(chatId, result);
        }
    }

    private void processHandleResult(long chatId, ModelResult<Context> modelResult) {
        try {
            switch (modelResult.getResult()) {
                case SEND_MESSAGE -> {
                    Context context = modelResult.getContext();

                    ViewResult viewResult = views.get(context.getContextName()).render(context);
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(chatId)
                            .text(viewResult.text())
                            .replyMarkup(viewResult.keyboard())
                            .build();

                    Message result = telegramClient.execute(sendMessage);
                    context.setChatId(result.getChatId());
                    context.setMessageId(result.getMessageId());

                    contextRepository.save(context);
                }
                case EDIT_MESSAGE -> {
                    Context context = modelResult.getContext();

                    ViewResult viewResult = views.get(context.getContextName()).render(context);
                    EditMessageText editMessage = EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(context.getMessageId())
                            .text(viewResult.text())
                            .replyMarkup(viewResult.keyboard())
                            .build();

                    telegramClient.execute(editMessage);

                    contextRepository.save(context);
                }
                case REPLACE_CONTEXT -> {
                    Context context = modelResult.getContext();

                    contextRepository.save(context);
                }
                case DELETE_CONTEXT -> {
                    contextRepository.delete(chatId);
                }
                case OOPS -> {
                    sendText(chatId, Presenter.OOPS_IMPOSSIBLE);
                }
            }
        } catch (TelegramApiException e) {
            log.error("Fail send rendered context", e);
        } catch (Exception e) {
            log.error("Fail process handled result", e);
        }
    }

    public void handleCallback(Update update) throws TelegramApiException {
        CallbackQuery callback = update.getCallbackQuery();
        long chatId = callback.getMessage().getChatId();

        Optional<Context> contextOptional = contextRepository.get(chatId);
        if (contextOptional.isEmpty()) {
            sendText(chatId, "Упс! Я не знаю что от меня ожидалось как насчёт /start?");
            return;
        } else if (contextOptional.get().getMessageId() != callback.getMessage().getMessageId()) {
            editText(chatId, callback.getMessage().getMessageId(), "Упс! Данное сообщение устарело, создай новое /start");
            return;
        }
        Context context = contextOptional.get();
        ModelResult<Context> result = models.get(context.getContextName()).handleCallback(callback, context);
        processHandleResult(chatId, result);
    }

    public void handleUpdate(Update update) throws TelegramApiException {
        if (update.hasMessage()) {
            Message msg = update.getMessage();
            if (msg.hasText()) {
                try {
                    handleTextMessage(update);
                } catch (Exception e) {
                    log.error("Fail handleTextMessage", e);
                    sendText(msg.getChatId(), "Ой, я сломался😢. Но не переживай меня уже чинят");
                }
            } else {
                sendText(msg.getChatId(), "Упс! Я точно не ожидал такого, пока я умею понимать только текстовые сообщения");
            }
        } else if (update.hasCallbackQuery()) {
            try {
                handleCallback(update);
            } catch (Exception e) {
                log.error("Fail handleCallback", e);
                sendText(update.getCallbackQuery().getMessage().getChatId(), "Ой, я сломался😢");
            }
        } else {
            handleUnexpectedState(update);
        }
    }

    private void handleUnexpectedState(Update update) throws TelegramApiException {
        if (update.hasEditedMessage()) {
            return;
        }
        log.error("Unhandled update: {}", update);
    }

    private void sendText(long chatId, String text) throws TelegramApiException {
        SendMessage msg = new SendMessage(Long.toString(chatId), text);
        telegramClient.execute(msg);
    }

    private void editText(long chatId, int messageId, String text) throws TelegramApiException {
        EditMessageText msg = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .build();
        telegramClient.execute(msg);
    }
}

