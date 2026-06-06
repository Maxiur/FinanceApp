package com.kaminski.FinanceApp.transaction;

import com.kaminski.FinanceApp.account.Account;
import com.kaminski.FinanceApp.account.AccountRepository;

import com.kaminski.FinanceApp.exception.ResourceNotFoundException;
import com.kaminski.FinanceApp.exception.UnprocessableContentException;
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

    public List<TransactionResponse> getTransactionsForAccount(String accountId, LocalDate from, LocalDate to, String category) {
        Account account = resolveAccount(accountId);
        // Konwersja LocalDate na LocalDateTime
        // 'from' ustawiamy na początek dnia (00:00:00)
        LocalDateTime fromDateTime = (from != null) ? from.atStartOfDay() : null;
        // 'to' ustawiamy na koniec dnia (23:59:59), żeby łapało transakcje z tego dnia
        LocalDateTime toDateTime = (to != null) ? to.atTime(23, 59, 59) : null;

        return transactionRepository.findFilteredTransactions(account.getId(), fromDateTime, toDateTime, category)
                .stream()
                .map(t -> new TransactionResponse(
                        t.getId(), t.getAmount(), t.getType(), t.getCategory(),
                        t.getDescription(), t.getTransactionDate(), t.getAccount().getId()
                ))
                .toList();
    }

    @Transactional
    public TransactionResponse addTransaction(String accountId, TransactionRequest request) {
        Account account = resolveAccount(accountId);

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
        Account account = resolveAccount(accountId);

        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getAccount().getId().equals(account.getId()))
                .toList();

        StringBuilder csv = new StringBuilder("ID,Kwota,Typ,Kategoria,Opis,Data\n");

        transactions.forEach(t -> csv.append(t.getId()).append(",")
                .append(t.getAmount()).append(",")
                .append(t.getType()).append(",")
                .append(t.getCategory()).append(",")
                .append(t.getDescription() == null ? "" : t.getDescription()).append(",")
                .append(t.getTransactionDate()).append("\n"));

        return csv.toString();
    }

    private Account resolveAccount(String param) {
        try {
            Long numericId = Long.parseLong(param);
            if (numericId <= 0) {
                throw new UnprocessableContentException("ID konta musi być liczbą dodatnią!");
            }
            return accountRepository.findById(numericId)
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o ID: " + numericId));
        } catch (NumberFormatException e) {
            return accountRepository.findByName(param)
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o nazwie: " + param));
        }
    }
}
