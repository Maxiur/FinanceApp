package com.kaminski.FinanceApp.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    // GET /api/v1/accounts - Lista wszystkich kont
    @GetMapping
    public List<AccountResponse> getAll() {
        return accountService.getAllAccounts();
    }

    // GET /api/v1/accounts/{id} - Szczegóły konta z saldem
    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    // POST /api/v1/accounts - Utwórz nowe konto
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // StatusCode 201
    public AccountResponse create(@Valid @RequestBody AccountRequest request) {
        // Adnotacja @Valid uruchamia validator z DTO
        return accountService.createAccount(request);
    }

    // DELETE /api/v1/accounts/{id} - Usuń konto
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // StatusCode 204
    public void delete(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}
