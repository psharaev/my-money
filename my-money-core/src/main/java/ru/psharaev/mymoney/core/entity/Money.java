package ru.psharaev.mymoney.core.entity;


import java.math.BigDecimal;

public record Money(BigDecimal amount, Currency currency) {
}
