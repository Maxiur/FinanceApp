package com.kaminski.FinanceApp.account;

import com.kaminski.FinanceApp.exception.ConflictException;
import com.kaminski.FinanceApp.exception.ResourceNotFoundException;
import com.kaminski.FinanceApp.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    // Wstrzykujemy repozytorium
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Wszystkie konta
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(acc -> new AccountResponse(acc.getId(), acc.getName(), acc.getBalance()))
                .toList();
    }

    // Nowe konto
    public AccountResponse createAccount(AccountRequest request) {
        // Saldo z Buildera domyślnie jest 0
        Account account = Account.builder()
                .name(request.name()).build();

        Account savedAccount = accountRepository.save(account);
        return new AccountResponse(savedAccount.getId(), savedAccount.getName(), savedAccount.getBalance());
    }

    // Usuwanie konta
    @Transactional
    public void deleteAccount(Long id) {
        if (transactionRepository.existsByAccountId(id)) {
            throw new ConflictException("Nie można usunąć konta, które ma historię transakcji!");
        }
        accountRepository.deleteById(id);
    }

    public AccountResponse getAccountById(Long id) {
        return accountRepository.findById(id).map(acc -> new AccountResponse(acc.getId(), acc.getName(), acc.getBalance()))
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o podanym ID!"));
    }
}
