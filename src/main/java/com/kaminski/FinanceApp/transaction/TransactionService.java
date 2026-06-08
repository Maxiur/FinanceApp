package com.kaminski.FinanceApp.transaction;

import com.kaminski.FinanceApp.account.Account;

import com.opencsv.CSVWriter;
import com.kaminski.FinanceApp.account.AccountResolver;
import com.kaminski.FinanceApp.config.AppProperties;
import com.kaminski.FinanceApp.exception.ResourceNotFoundException;
import com.kaminski.FinanceApp.exception.UnprocessableContentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountResolver accountResolver;
    private final AppProperties appProperties;

    public List<TransactionResponse> getTransactionsForAccount(String accountId, LocalDate from, LocalDate to, String category) {
        Account account = accountResolver.resolve(accountId);
        // Konwersja LocalDate na LocalDateTime
        // 'from' ustawiamy na początek dnia (00:00:00)
        LocalDateTime fromDateTime = (from != null) ? from.atStartOfDay() : null;
        // 'to' ustawiamy na koniec dnia (23:59:59), żeby łapało transakcje z tego dnia
        LocalDateTime toDateTime = (to != null) ? to.atTime(23, 59, 59) : null;
        String categoryParam = (category == null) ? "" : category.toLowerCase();

        return transactionRepository.findFilteredTransactions(account.getId(), fromDateTime, toDateTime, categoryParam)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse addTransaction(String accountId, TransactionRequest request) {
        Account account = accountResolver.resolve(accountId);

        String warning = null;
        if (request.type() == TransactionType.EXPENSE) {
            String categoryKey = request.category().toLowerCase();
            java.math.BigDecimal limit = appProperties.getLimits().get(categoryKey);
            if (limit != null) {
                LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                java.math.BigDecimal currentExpenses = transactionRepository.calculateExpensesForCategoryInMonth(
                        account.getId(), request.category(), startOfMonth);
                java.math.BigDecimal totalWithNew = currentExpenses.add(request.amount());
                if (totalWithNew.compareTo(limit) > 0) {
                    warning = "Przekroczono limit budżetu dla kategorii '" + request.category() + "'! Limit: " + limit + ", Suma wydatków w tym miesiącu: " + totalWithNew;
                }
            }
        }

        // Aktualizacja salda konta
        if (request.type() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(request.amount()));
        } else {
            account.setBalance(account.getBalance().subtract(request.amount()));
        }

        // Budowa entity transakcji
        Transaction transaction = Transaction.builder()
                .amount(request.amount())
                .type(request.type())
                .category(request.category())
                .description(request.description())
                .transactionDate(LocalDateTime.now())
                .account(account)
                .build();

        // Zapis transakcji do DB
        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToResponse(savedTransaction, warning);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        if (id <= 0) {
            throw new UnprocessableContentException("ID konta musi być liczbą dodatnią!");
        }
        // Szukanie transakcji
        Transaction transaction = transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Nie znaleziono transakcji o podanym ID!"));

        Account account = transaction.getAccount();

        // Cofamy saldo jeśli to był przychód, jak wydatek to dodajemy do salda
        if (transaction.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }

        transactionRepository.delete(transaction);
    }

    public String exportToCsv(String accountId) {
        Account account = accountResolver.resolve(accountId);

        List<Transaction> transactions = transactionRepository.findByAccountId(account.getId());

        try (StringWriter writer = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // Nagłówek
            csvWriter.writeNext(new String[]{"ID", "Kwota", "Typ", "Kategoria", "Opis", "Data"});

            // Dane
            for (Transaction t : transactions) {
                String[] row = {
                        t.getId().toString(),
                        t.getAmount().toString(),
                        t.getType().toString(),
                        t.getCategory(),
                        t.getDescription() == null ? "" : t.getDescription(),
                        t.getTransactionDate().toString()
                };
                csvWriter.writeNext(row);
            }

            return writer.toString();
        } catch (Exception e) {
            throw new UnprocessableContentException("Nie udało się wygenerować CSV");
        }
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return mapToResponse(t, null);
    }

    private TransactionResponse mapToResponse(Transaction t, String warning) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getType(),
                t.getCategory(),
                t.getDescription(),
                t.getTransactionDate(),
                t.getAccount().getId(),
                warning
        );
    }
}
