package ru.psharaev.mymoney.bot.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Data
@Validated
public class RedisConfig {
    @NotNull
    private final URI url;
}
