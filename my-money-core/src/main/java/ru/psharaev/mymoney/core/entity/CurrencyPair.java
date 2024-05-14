package ru.psharaev.mymoney.core.entity;

import java.math.BigDecimal;

public record CurrencyPair(
        Currency baseCurrency,
        Currency quoteCurrency,
        BigDecimal rate
) {
}
