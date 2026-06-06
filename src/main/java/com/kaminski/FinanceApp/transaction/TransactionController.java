package com.kaminski.FinanceApp.transaction;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    // GET /api/v1/transactions?from=2026-01-01&to=2026-12-31&category=Jedzenie
    @GetMapping
    public List<TransactionResponse> getTransactions(
            @RequestParam(required=false) Long accountId,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required=false) String category) {
        return transactionService.getAllTransactions(accountId, from, to, category);
    }

    // POST /api/v1/transactions - Dodanie transakcji
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Zwraca status 201
    public TransactionResponse addTransaction(@Valid @RequestBody TransactionRequest request) {
        return transactionService.addTransaction(request);
    }

    // DELETE /api/v1/transactions/{id} - Usunięcie transakcji
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Zwraca status 204
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}
