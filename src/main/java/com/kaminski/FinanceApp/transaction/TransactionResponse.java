package com.kaminski.FinanceApp.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id, BigDecimal amount, TransactionType type, String category,
        String description, LocalDateTime transactionDate, Long accountId
) {}
