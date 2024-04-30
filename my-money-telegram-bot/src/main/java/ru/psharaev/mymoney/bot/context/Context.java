package ru.psharaev.mymoney.bot.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "T")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FlowContext.class, name = FlowContext.CONTEXT_NAME),
        @JsonSubTypes.Type(value = StartContext.class, name = StartContext.CONTEXT_NAME),
})
public interface Context {
    long getUserId();

    long getChatId();

    void setChatId(long chatId);

    int getMessageId();

    void setMessageId(int messageId);

    @JsonIgnore
    String getContextName();
}
