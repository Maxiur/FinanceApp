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

    // TODO żeby nie ładować całej bazy do RAM
    // Wszystkie konta
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(acc -> new AccountResponse(acc.getId(), acc.getName(), acc.getBalance()))
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
        return new AccountResponse(savedAccount.getId(), savedAccount.getName(), savedAccount.getBalance());
    }

    // Usuwanie konta
    @Transactional
    public void deleteAccount(String idOrName) {
        Account account = resolveAccount(idOrName);

        if (transactionRepository.existsByAccountId(account.getId())) {
            throw new ConflictException("Nie można usunąć konta, które ma historię transakcji!");
        }
        accountRepository.delete(account);
    }

    public AccountResponse getAccountDetails(String param) {
        Account account = resolveAccount(param);
        return new AccountResponse(account.getId(), account.getName(), account.getBalance());
    }

    private Account resolveAccount(String param) {
        try {
            Long numericId = Long.parseLong(param);
            if (numericId <= 0) {
                throw new ConflictException("ID konta musi być liczbą dodatnią!");
            }
            return accountRepository.findById(numericId)
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o ID: " + numericId));
        } catch (NumberFormatException e) {
            return accountRepository.findByName(param)
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o nazwie: " + param));
        }
    }
}
