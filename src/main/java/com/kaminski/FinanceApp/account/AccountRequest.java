package com.kaminski.FinanceApp.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountRequest(
        @NotBlank(message = "Konto musi mieć nazwę!")
        @Pattern(regexp = "^(?!\\d+$).+$", message = "Nazwa konta nie może być samą liczbą!")
        String name) {
}
