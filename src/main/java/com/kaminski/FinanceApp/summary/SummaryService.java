package com.kaminski.FinanceApp.summary;

import com.kaminski.FinanceApp.transaction.Transaction;
import com.kaminski.FinanceApp.transaction.TransactionRepository;
import com.kaminski.FinanceApp.transaction.TransactionType;
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

    public SummaryResponse getSummary() {
        List<Transaction> transactions = transactionRepository.findAll();

        // Łączne pieniądze z przychodów
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Łaczne pieniądze z wydatków
        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Grupowanie wydatków po kategorii (np. "Jedzenie" -> 150.50)
        Map<String, BigDecimal> expensesByCategory = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        return new SummaryResponse(totalIncome, totalExpenses, expensesByCategory);
    }
}
