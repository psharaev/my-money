package ru.psharaev.mymoney.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.psharaev.mymoney.core.entity.Currency;
import ru.psharaev.mymoney.core.entity.CurrencyPair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static ru.psharaev.mymoney.core.entity.Currency.*;

@Slf4j
@Service
public class CurrencyService {
    private static final Map<Currency, Set<Currency>> CURRENCY_PAIRS = Map.of(
            USD, Set.of(RUB, CNY),
            HKD, Set.of(RUB),
            CNY, Set.of(RUB),
            EUR, Set.of(USD, RUB)
    );

    static {
        for (Map.Entry<Currency, Set<Currency>> entry : CURRENCY_PAIRS.entrySet()) {
            Currency baseCurrency = entry.getKey();
            for (Currency quoteCurrency : entry.getValue()) {
                Set<Currency> currencies = CURRENCY_PAIRS.get(quoteCurrency);
                if (currencies != null && currencies.contains(baseCurrency)) {
                    throw new RuntimeException("Found conflict currency pair %s/%s".formatted(baseCurrency, quoteCurrency));
                }
            }
        }
    }

    private final String api;

    private final ObjectMapper objectMapper;
    private volatile Map<Currency, Map<Currency, BigDecimal>> exchangeRates;


    private final Environment environment;

    public CurrencyService(@Value("${mymoney.exchangerate.api-key}") String apiKey, ObjectMapper objectMapper, Environment environment) {
        this.api = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/";
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    @PostConstruct
    void init() throws JsonProcessingException {
        if (environment.matchesProfiles("local")) {
            exchangeRates = Map.of(
                    Currency.RUB, Map.of(USD, BigDecimal.valueOf(0.010928962)),
                    USD, Map.of(Currency.RUB, BigDecimal.valueOf(91.5))
            );
        } else {
            updateExchangeRatesLogic();
        }
    }

    @Scheduled(cron = "0 6 * * *")
    void updateExchangeRatesCron() {
        log.info("Start update currency exchange rate");

        try {
            updateExchangeRatesLogic();
        } catch (Exception e) {
            log.error("Error update currency exchange rate", e);
        }
        log.info("Finish update currency exchange rate");
    }

    private void updateExchangeRatesLogic() throws JsonProcessingException {
        Map<Currency, Map<Currency, BigDecimal>> m = new EnumMap<>(Currency.class);
        for (Currency from : Currency.values()) {
            RestClient defaultClient = RestClient.create();

            String body = defaultClient.get()
                    .uri(api + from.getCurrencyCode())
                    .retrieve()
                    .body(String.class);

            JsonNode jsonNode;
            jsonNode = objectMapper.readTree(body);
            JsonNode conversionRates = jsonNode.get("conversion_rates");

            for (Currency to : Currency.values()) {
                if (from == to) {
                    continue;
                }
                String rate = conversionRates.get(to.getCurrencyCode()).asText();
                m.computeIfAbsent(from, k -> new EnumMap<>(Currency.class)).put(to, new BigDecimal(rate));
            }
        }

        this.exchangeRates = m;
    }

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from == to) {
            return amount;
        }
        BigDecimal rate = exchangeRates.get(from).get(to);
        return amount.multiply(rate);
    }

    public CurrencyPair marketCurrencyPair(Currency from, Currency to) {
        Set<Currency> currencies = CURRENCY_PAIRS.get(from);
        if (currencies != null && currencies.contains(to)) {
            return new CurrencyPair(from, to, exchangeRates.get(from).get(to));
        }
        currencies = CURRENCY_PAIRS.get(to);
        if (currencies != null && currencies.contains(from)) {
            return new CurrencyPair(to, from, exchangeRates.get(to).get(from));
        }
        return new CurrencyPair(from, to, exchangeRates.get(from).get(to));
    }

    public CurrencyPair calcExchangeRate(BigDecimal fromAmount, BigDecimal toAmount, Currency from, Currency to) {
        Set<Currency> currencies = CURRENCY_PAIRS.get(from);
        if (currencies != null && currencies.contains(to)) {
            return new CurrencyPair(from, to, divide(toAmount, fromAmount));
        }
        currencies = CURRENCY_PAIRS.get(to);
        if (currencies != null && currencies.contains(from)) {
            return new CurrencyPair(to, from, divide(fromAmount, toAmount));
        }
        return new CurrencyPair(from, to, divide(toAmount, fromAmount));
    }

    private static BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (b.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return a.divide(b, RoundingMode.DOWN);
    }
}
