package com.kaminski.FinanceApp.transaction;

import com.kaminski.FinanceApp.account.Account;
import com.kaminski.FinanceApp.account.AccountRepository;

import com.kaminski.FinanceApp.account.AccountResponse;
import com.kaminski.FinanceApp.exception.ConflictException;
import com.kaminski.FinanceApp.exception.ResourceNotFoundException;
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

    public List<TransactionResponse> getAllTransactions(Long accountId, LocalDate from, LocalDate to, String category) {
        return transactionRepository.findAll().stream()
                .filter(t -> accountId == null || t.getAccount().getId().equals(accountId))
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
        Account account;
        // Szukamy po ID albo po nazwie.
        if (request.accountId() != null) {
            account = accountRepository.findById(request.accountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o podanym ID!"));
        } else if (request.accountName() != null && !request.accountName().isBlank()) {
            account = accountRepository.findByName(request.accountName())
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o nazwie: " + request.accountName()));
        } else {
            throw new ConflictException("Musisz podać accountId lub accountName!");
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

    public AccountResponse getAccountById(Long id) {
        return accountRepository.findById(id).map(acc -> new AccountResponse(acc.getId(), acc.getName(), acc.getBalance()))
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o podanym ID!"));
    }
}
