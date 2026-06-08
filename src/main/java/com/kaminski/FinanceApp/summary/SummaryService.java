package com.kaminski.FinanceApp.summary;

import com.kaminski.FinanceApp.transaction.Transaction;
import com.kaminski.FinanceApp.transaction.TransactionRepository;
import com.kaminski.FinanceApp.transaction.TransactionType;
import com.kaminski.FinanceApp.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummaryService {
    private final TransactionRepository transactionRepository;
    private final AppProperties appProperties;

    public Map<String, BigDecimal> getLimits() {
        return appProperties.getLimits();
    }

    public SummaryResponse getSummary() {
        List<Transaction> transactions = transactionRepository.findAll();

        // Łączne pieniądze z przychodów
        BigDecimal totalIncome = transactionRepository.calculateTotalAmountByType(TransactionType.INCOME);

        // Łaczne pieniądze z wydatków
        BigDecimal totalExpenses = transactionRepository.calculateTotalAmountByType(TransactionType.EXPENSE);

        // Grupowanie wydatków po kategorii (np. "Jedzenie" -> 150.50)
        // Mapujemy surową odpowiedź z bazy (Object[]) na Mapę dla JSON-a
        Map<String, BigDecimal> expensesByCategory = transactionRepository.getExpensesGroupedByCategory().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],         // kategoria
                        row -> (BigDecimal) row[1]      // suma dla kategorii
                ));

        return new SummaryResponse(totalIncome, totalExpenses, expensesByCategory);
    }
}
