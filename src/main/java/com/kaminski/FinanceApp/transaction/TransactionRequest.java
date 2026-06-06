package com.kaminski.FinanceApp.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "Należy wypełnić kwotę!")
        @Positive(message = "Kwota musi być większa od zera!")
        BigDecimal amount,

        @NotNull(message = "Wybierz INCOME lub EXPENSE")
        TransactionType type,

        @NotBlank(message = "Wybierz kategorię!")
        String category,

        String description
) {}
