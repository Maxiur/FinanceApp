package com.kaminski.FinanceApp.account;

import com.kaminski.FinanceApp.exception.ConflictException;
import com.kaminski.FinanceApp.exception.ResourceNotFoundException;
import com.kaminski.FinanceApp.exception.UnprocessableContentException;
import com.kaminski.FinanceApp.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountResolver accountResolver;
    // Wstrzykujemy repozytorium
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // TODO żeby nie ładować całej bazy do RAM można używać PAGE'ów
    // Wszystkie konta
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Nowe konto
    public AccountResponse createAccount(AccountRequest request) {
        if (accountRepository.existsByName(request.name())) {
            throw new ConflictException("Konto o nazwie '" + request.name() + "' już istnieje! Spróbuj inną nazwę!");
        }

        // Saldo z Buildera domyślnie jest 0
        Account account = Account.builder()
                .name(request.name()).build();

        Account savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    // Usuwanie konta
    @Transactional
    public void deleteAccount(String idOrName) {
        Account account = accountResolver.resolve(idOrName);

        if (transactionRepository.existsByAccountId(account.getId())) {
            throw new UnprocessableContentException("Nie można usunąć konta, które ma historię transakcji!");
        }
        accountRepository.delete(account);
    }

    public AccountResponse getAccountDetails(String param) {
        Account account = accountResolver.resolve(param);
        return mapToResponse(account);
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(account.getId(), account.getName(), account.getBalance());
    }
}
