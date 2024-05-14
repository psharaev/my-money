package ru.psharaev.mymoney.bot.util;

import ru.psharaev.mymoney.bot.view.StartView;
import ru.psharaev.mymoney.core.entity.Account;
import ru.psharaev.mymoney.core.entity.Currency;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Formatter {
    private static final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter shortDateFmt = DateTimeFormatter.ofPattern("dd.MM HH:mm");
    private static final DateTimeFormatter fullDateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    private static final String[] times = new String[]{
            "🕛", "🕧", "🕐", "🕜",
            "🕑", "🕝", "🕒", "🕞",
            "🕓", "🕟", "🕔", "🕠",
            "🕕", "🕡", "🕖", "🕢",
            "🕗", "🕣", "🕘", "🕤",
            "🕙", "🕥", "🕚", "🕦"
    };

    public static String formatAccountName(Optional<Long> favoriteAccountId, Account account) {
        if (favoriteAccountId.isPresent() && favoriteAccountId.get().equals(account.getAccountId())) {
            return StartView.FAVORITE_ACCOUNT_STAR + account.getName();
        }
        return account.getName();
    }

    public static String formatAmount(BigDecimal amount, Currency currency) {
        return DECIMAL_FORMAT.format(amount) + " " + currency.getCurrencySymbol();
    }

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
