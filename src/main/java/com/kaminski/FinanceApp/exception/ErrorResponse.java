package com.kaminski.FinanceApp.exception;

import java.time.LocalDateTime;

// Pudełko na Odpowiedź błędu
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {}