package ru.psharaev.mymoney.bot.view;

import ru.psharaev.mymoney.bot.context.Context;

public interface View {
    String getContextName();

    ViewResult render(Context context);
}
