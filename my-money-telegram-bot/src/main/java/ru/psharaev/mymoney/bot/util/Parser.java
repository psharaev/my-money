package ru.psharaev.mymoney.bot.util;

import com.ezylang.evalex.BaseException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.MapBasedDataAccessor;
import com.ezylang.evalex.operators.OperatorIfc;
import ru.psharaev.mymoney.core.exception.MymoneyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class Parser {
    private static final ExpressionConfiguration EXPRESSION_CONFIGURATION = ExpressionConfiguration.builder()
            .allowOverwriteConstants(false)
            .arraysAllowed(false)
            .dataAccessorSupplier(MapBasedDataAccessor::new)
            .decimalPlacesRounding(4)
            .defaultConstants(ExpressionConfiguration.StandardConstants)
            .implicitMultiplicationAllowed(true)
            .mathContext(ExpressionConfiguration.DEFAULT_MATH_CONTEXT)
            .powerOfPrecedence(OperatorIfc.OPERATOR_PRECEDENCE_POWER)
            .stripTrailingZeros(true)
            .structuresAllowed(false)
            .singleQuoteStringLiteralsAllowed(false)
            .build();

    private static final Clock CLOCK = Clock.systemUTC();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("[dd.MM[.yyyy]][ ][HH[:mm]]");


    public static String nowByPattern(ZoneOffset offset) {
        return DATE_TIME_FORMATTER.format(Instant.now().atOffset(offset));
    }

    public static OffsetDateTime parseDateTime(String dateTime, ZoneOffset offset) throws DateTimeParseException {
        OffsetDateTime now = Instant.now(CLOCK).atOffset(offset);

        DateTimeFormatter fullDateWithoutYear = new DateTimeFormatterBuilder()
                .append(DATE_TIME_FORMATTER)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, now.getDayOfMonth())
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, now.getMonthValue())
                .parseDefaulting(ChronoField.YEAR_OF_ERA, now.getYear())
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 12)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .toFormatter();

        return LocalDateTime.parse(dateTime, fullDateWithoutYear).atOffset(offset);
    }

    public static BigDecimal parseExpression(String exp) {
        try {
            exp = exp.replaceAll("\\s+", "");
            Expression expression = new Expression(exp, EXPRESSION_CONFIGURATION);
            EvaluationValue evaluate = expression.evaluate();
            if (evaluate.getDataType() != EvaluationValue.DataType.NUMBER) {
                throw MymoneyException.userBadArguments("В результате вычисления выражения, не получилось число, результат: " + evaluate.getValue());
            }
            BigDecimal numberValue = evaluate.getNumberValue();
            numberValue = numberValue.setScale(2, RoundingMode.FLOOR);
            return numberValue;
        } catch (BaseException e) {
            throw MymoneyException.userBadArguments(e, "Ошибка: " + e.getMessage());
        }

    }
}
