package ru.psharaev.mymoney.bot.view;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public record ViewResult(String text, InlineKeyboardMarkup keyboard) {
}
