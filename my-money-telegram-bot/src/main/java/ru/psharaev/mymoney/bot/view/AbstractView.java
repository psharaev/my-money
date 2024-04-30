package ru.psharaev.mymoney.bot.view;

import ru.psharaev.mymoney.bot.context.Context;

abstract class AbstractView<T extends Context> implements View {
    private final String contextName;

    AbstractView(String contextName) {
        this.contextName = contextName;
    }

    @Override
    public final ViewResult render(Context context) {
        return renderImpl((T) context);
    }

    abstract ViewResult renderImpl(T context);

    @Override
    public final String getContextName() {
        return contextName;
    }
}
