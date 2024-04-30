package ru.psharaev.mymoney.bot.model;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.psharaev.mymoney.bot.context.Context;

import java.util.NoSuchElementException;
import java.util.Objects;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelResult<T extends Context> {
    private static final ModelResult<Context> NOT_CHANGED = new ModelResult<>(null, Result.NOT_CHANGED);
    private static final ModelResult<Context> DELETE_CONTEXT = new ModelResult<>(null, Result.DELETE_CONTEXT);

    private final T context;
    @Getter
    private final Result result;

    /**
     * Изменить текущий контекст и отправить пользователю новое сообщения
     */
    public static <T extends Context> ModelResult<T> sendMessage(T context) {
        return new ModelResult<>(Objects.requireNonNull(context), Result.SEND_MESSAGE);
    }

    /**
     * Изменить текущий контекст и отредактировать пользователю текущее сообщение
     */
    public static <T extends Context> ModelResult<T> editMessage(T context) {
        return new ModelResult<>(Objects.requireNonNull(context), Result.EDIT_MESSAGE);
    }

    /**
     * Изменить текущий контекст и ничего не делать в чате
     */
    public static <T extends Context> ModelResult<T> replaceContext(T context) {
        return new ModelResult<>(Objects.requireNonNull(context), Result.REPLACE_CONTEXT);
    }

    /**
     * Ничего не делать
     */
    public static ModelResult<Context> notChanged() {
        return NOT_CHANGED;
    }

    /**
     * Удалить текущий контекст и ничего не делать в чате
     */
    public static ModelResult<Context> deleteContext() {
        return DELETE_CONTEXT;
    }

    public T getContext() {
        if (context == null) {
            throw new NoSuchElementException("No context present");
        }
        return context;
    }

    public enum Result {
        SEND_MESSAGE,
        EDIT_MESSAGE,
        REPLACE_CONTEXT,
        NOT_CHANGED,
        DELETE_CONTEXT
    }
}
