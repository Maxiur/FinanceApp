package com.kaminski.FinanceApp.account;

import jakarta.validation.constraints.NotBlank;

public record AccountRequest(@NotBlank(message = "Konto musi mieć nazwę!") String name) {
}
