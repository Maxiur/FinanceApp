package com.kaminski.FinanceApp.transaction;

import com.kaminski.FinanceApp.account.Account;
import com.kaminski.FinanceApp.account.AccountResolver;
import com.kaminski.FinanceApp.exception.ResourceNotFoundException;
import com.kaminski.FinanceApp.exception.UnprocessableContentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountResolver accountResolver;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldAddIncomeAndUpdateBalance() {
        // GIVEN
        Account account = new Account(1L, "Skarpeta", new BigDecimal("10.00"));
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"), TransactionType.INCOME, "Wypłata", "Z premią"
        );

        when(accountResolver.resolve("1")).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(99L); // Symulujemy, że baza nadała ID
            return t;
        });

        // WHEN - przelew
        TransactionResponse response = transactionService.addTransaction("1", request);

        // THEN - Powinno się zgadzać, że 10 + 100 = 110
        assertEquals(new BigDecimal("110.00"), account.getBalance());
        assertEquals(99L, response.id());
        assertEquals(TransactionType.INCOME, response.type());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldAddExpenseAndAllowNegativeBalance() {
        // GIVEN - ma 50 zł, ale kupuje coś za 200 zł
        Account account = new Account(1L, "Karta", new BigDecimal("50.00"));
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("200.00"), TransactionType.EXPENSE, "Zabawa", "Klub"
        );

        when(accountResolver.resolve("1")).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        // WHEN - Płacimy
        TransactionResponse response = transactionService.addTransaction("1", request);

        // THEN - Wchodzi debet -150 zł
        assertEquals(new BigDecimal("-150.00"), account.getBalance());
        assertEquals(TransactionType.EXPENSE, response.type());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldDeleteExpenseAndRestoreBalance() {
        // GIVEN - Mamy na koncie 100 zł
        Account account = new Account(1L, "Konto", new BigDecimal("100.00"));

        // Ktoś wcześniej wydał 50 zł (czyli przed wydatkiem miał 150)
        Transaction expenseTransaction = Transaction.builder()
                .id(5L)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .account(account)
                .build();

        when(transactionRepository.findById(5L)).thenReturn(Optional.of(expenseTransaction));

        // WHEN - Anulujemy/usuwamy ten wydatek
        transactionService.deleteTransaction(5L);

        // THEN - Hajs (50 zł) musi wrócić na konto! (100 + 50 = 150)
        assertEquals(new BigDecimal("150.00"), account.getBalance());
        verify(transactionRepository).delete(expenseTransaction);
    }

    @Test
    void shouldDeleteIncomeAndSubtractBalance() {
        // GIVEN - Mamy na koncie 500 zł
        Account account = new Account(1L, "Konto", new BigDecimal("500.00"));

        Transaction incomeTransaction = Transaction.builder()
                .id(10L)
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.INCOME)
                .account(account)
                .build();

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(incomeTransaction));

        // WHEN - Usuwamy
        transactionService.deleteTransaction(10L);

        // THEN - Zabieramy te 200 zł (500 - 200 = 300)
        assertEquals(new BigDecimal("300.00"), account.getBalance());
        verify(transactionRepository).delete(incomeTransaction);
    }

    @Test
    void shouldThrowExceptionWhenDeletingWithInvalidId() {
        // WHEN & THEN - Próbujemy usunąć ID 0 lub ujemne
        assertThrows(UnprocessableContentException.class, () -> transactionService.deleteTransaction(0L));
        assertThrows(UnprocessableContentException.class, () -> transactionService.deleteTransaction(-5L));

        // Baza ma nic nei robić
        verify(transactionRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTransaction() {
        // GIVEN
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(99L));
    }
}
