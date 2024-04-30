package ru.psharaev.mymoney.rest.api;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Value
@Builder
@Jacksonized
public class CreateFlowRequest {
    long accountId;
    @NotNull
    BigDecimal amount;
    @NotNull
    OffsetDateTime time;
    @NotNull
    String category;
    @NotNull
    String description;
}
