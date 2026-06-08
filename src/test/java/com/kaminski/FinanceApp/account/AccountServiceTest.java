package com.kaminski.FinanceApp.account;

import com.kaminski.FinanceApp.exception.ConflictException;
import com.kaminski.FinanceApp.exception.UnprocessableContentException;
import com.kaminski.FinanceApp.transaction.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountResolver accountResolver;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() {
        // GIVEN
        AccountRequest request = new AccountRequest("Hajs na kebsa");

        when(accountRepository.existsByName("Hajs na kebsa")).thenReturn(false);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // WHEN
        AccountResponse response = accountService.createAccount(request);

        // THEN
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Hajs na kebsa", response.name());
        assertEquals(BigDecimal.ZERO, response.balance());

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowConflictWhenAccountNameExists() {
        // GIVEN
        AccountRequest request = new AccountRequest("Hajs na kebsa");

        when(accountRepository.existsByName("Hajs na kebsa")).thenReturn(true);

        // WHEN & THEN
        assertThrows(ConflictException.class, () -> accountService.createAccount(request));

        // Sprawdzamy, czy serwis na pewno zrezygnował z zapisu
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldDeleteAccountSuccessfully() {
        // GIVEN
        Account accountToDelete = new Account(1L, "Skarpeta", BigDecimal.ZERO);

        when(accountResolver.resolve("1")).thenReturn(accountToDelete);

        // Udajemy, że nie ma transakcji
        when(transactionRepository.existsByAccountId(1L)).thenReturn(false);

        // WHEN
        accountService.deleteAccount("1");

        // THEN
        verify(accountRepository).delete(accountToDelete);
    }

    @Test
    void shouldThrowExceptionWhenDeletingAccountWithTransactions() {
        // GIVEN
        Account accountToDelete = new Account(1L, "Skarpeta", BigDecimal.ZERO);
        when(accountResolver.resolve("1")).thenReturn(accountToDelete);

        // Tym razem baza krzyczy, że są transakcje!
        when(transactionRepository.existsByAccountId(1L)).thenReturn(true);

        // WHEN & THEN
        assertThrows(UnprocessableContentException.class, () -> accountService.deleteAccount("1"));

        // Baza nie mogła usunąć konta
        verify(accountRepository, never()).delete(any());
    }

    @Test
    void shouldGetAllAccounts() {
        // GIVEN
        Account acc1 = new Account(1L, "Konto 1", BigDecimal.ZERO);
        Account acc2 = new Account(2L, "Konto 2", BigDecimal.TEN);
        when(accountRepository.findAll()).thenReturn(List.of(acc1, acc2));

        // WHEN
        List<AccountResponse> result = accountService.getAllAccounts();

        // THEN
        assertEquals(2, result.size());
        assertEquals("Konto 1", result.get(0).name());
        assertEquals(BigDecimal.TEN, result.get(1).balance());
    }
}
