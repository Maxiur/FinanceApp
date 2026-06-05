package com.kaminski.FinanceApp.transaction;

import com.kaminski.FinanceApp.account.Account;
import com.kaminski.FinanceApp.account.AccountRepository;

import com.kaminski.FinanceApp.account.AccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public List<TransactionResponse> getAllTransactions(LocalDate from, LocalDate to, String category) {
        return transactionRepository.findAll().stream()
                // Jeśli 'from' nie jest nullem, odrzucamy transakcje młodze od 'from'
                .filter(t -> from == null || !t.getTransactionDate().toLocalDate().isBefore(from))
                // Jeśli 'to' nie jest nullem, odrzucamy transakcje nowsze niż 'to'
                .filter(t -> to == null || !t.getTransactionDate().toLocalDate().isAfter(to))
                // Jeśli 'category' nie jest nullem, sprawdzamy czy kategoria się zgadza (ignorując wielkość liter)
                .filter(t -> category == null || t.getCategory().equalsIgnoreCase(category))
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getAmount(),
                        t.getType(),
                        t.getCategory(),
                        t.getDescription(),
                        t.getTransactionDate(),
                        t.getAccount().getId()
                ))
                .toList();
    }

    @Transactional
    public TransactionResponse addTransaction(TransactionRequest request) {
        // Szukamy konta
        Account account = accountRepository.findById(request.accountId()).orElseThrow(
                () -> new RuntimeException("Nie znaleziono konta o podanym ID!"));

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

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getCategory(),
                savedTransaction.getDescription(),
                savedTransaction.getTransactionDate(),
                account.getId()
        );
    }

    @Transactional
    public void deleteTransaction(Long id) {
        // Szukanie transakcji
        Transaction transaction = transactionRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Nie znaleziono transakcji o podanym ID!"));

        Account account = transaction.getAccount();

        // Cofamy saldo jeśli to był przychód, jak wydatek to dodajemy do salda
        if (transaction.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }

        transactionRepository.delete(transaction);
    }

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Nie znaleziono konta o podanym ID!")
        );
        return new AccountResponse(account.getId(), account.getName(), account.getBalance());
    }
}
