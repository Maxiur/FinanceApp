package com.kaminski.FinanceApp.account;

import java.math.BigDecimal;

public record AccountResponse(Long id, String name, BigDecimal balance) {
}
