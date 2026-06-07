package com.kaminski.FinanceApp.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @Schema(example = "100.00")
        @NotNull(message = "Należy wypełnić kwotę!")
        @Positive(message = "Kwota musi być większa od zera!")
        BigDecimal amount,

        @Schema(example = "INCOME")
        @NotNull(message = "Wybierz INCOME lub EXPENSE")
        TransactionType type,

        @Schema(example = "Ogólne")
        @NotBlank(message = "Wybierz kategorię!")
        String category,

        @Schema(example = "Przyjemności")
        String description
) {}
