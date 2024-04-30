package ru.psharaev.mymoney.bot.model.command;

import ru.psharaev.mymoney.bot.context.Context;
import ru.psharaev.mymoney.bot.model.ModelResult;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
public class DeleteContextCommand implements Command {
    @Override
    public String getDescription() {
        return "Удалить контекст";
    }

    @Override
    public String getCommand() {
        return "/deletecontext";
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public ModelResult<Context> handleMessage(Message msg) {
        return ModelResult.deleteContext();
    }
}
