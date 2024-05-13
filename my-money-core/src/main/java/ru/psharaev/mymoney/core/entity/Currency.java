package ru.psharaev.mymoney.core.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    // TODO use java.util.Currency
    RUB("RUB", 643, "₽"),
    USD("USD", 840, "$"),
    EUR("EUR", 978, "€"),
    CNY("CNY", 156, "¥"),
    HKD("HKD", 344, "HK$");

    /**
     * ISO 4217 currency code for this currency.
     *
     * @serial
     */
    private final String currencyCode;

    /**
     * ISO 4217 numeric code for this currency.
     * Set from currency data tables.
     */
    private final int numericCode;

    private final String currencySymbol;
}
