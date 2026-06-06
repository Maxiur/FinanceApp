package com.kaminski.FinanceApp.summary;

import java.math.BigDecimal;
import java.util.Map;

public record SummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        Map<String, BigDecimal> expensesByCategory
) {}
