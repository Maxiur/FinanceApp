package com.kaminski.FinanceApp.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountRequest(
        @Schema(example = "Konto na bzdury")
        @NotBlank(message = "Konto musi mieć nazwę!")
        @Pattern(regexp = "^(?!\\d+$).+$", message = "Nazwa konta nie może być samą liczbą!")
        String name) {
}
