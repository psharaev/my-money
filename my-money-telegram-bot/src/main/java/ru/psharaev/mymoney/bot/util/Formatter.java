package ru.psharaev.mymoney.bot.util;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Formatter {
    private static final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter shortDateFmt = DateTimeFormatter.ofPattern("dd.MM HH:mm");
    private static final DateTimeFormatter fullDateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final String[] times = new String[]{
            "🕛", "🕧", "🕐", "🕜",
            "🕑", "🕝", "🕒", "🕞",
            "🕓", "🕟", "🕔", "🕠",
            "🕕", "🕡", "🕖", "🕢",
            "🕗", "🕣", "🕘", "🕤",
            "🕙", "🕥", "🕚", "🕦"
    };

    public static String getEmojiTime(ZonedDateTime zonedDateTime) {
        int minutes = 60 * zonedDateTime.getHour() + zonedDateTime.getMinute();

        return times[(minutes / 30) % times.length];
    }

    public static String format(OffsetDateTime userTime) {
        LocalDate userDate = userTime.toLocalDate();
        LocalDate today = LocalDate.now();
        if (today.equals(userDate)) {
            return "Сегодня " + timeFmt.format(userTime);
        }
        LocalDate yesterday = today.minusDays(1);
        if (yesterday.equals(userDate)) {
            return "Вчера " + timeFmt.format(userTime);
        }
        LocalDate dayBeforeYesterday = yesterday.minusDays(1);
        if (dayBeforeYesterday.equals(userDate)) {
            return "Позавчера " + timeFmt.format(userTime);
        }

        if (today.getYear() == userDate.getYear()) {
            return shortDateFmt.format(userTime);
        }
        return fullDateFmt.format(userTime);
    }
}
