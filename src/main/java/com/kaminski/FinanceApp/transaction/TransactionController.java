package com.kaminski.FinanceApp.transaction;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    // GET /api/v1/accounts/{accountId}/transactions?from=2026-01-01&to=2026-12-31&category=Jedzenie
    @GetMapping(value = "/accounts/{accountId}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable String accountId,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required=false) String category) {
        return transactionService.getTransactionsForAccount(accountId, from, to, category);
    }

    // POST /api/v1/accounts/{accountId}transactions - Dodanie transakcji
    @PostMapping(value = "/accounts/{accountId}/transactions")
    @ResponseStatus(HttpStatus.CREATED) // Zwraca status 201
    public TransactionResponse addTransaction(
            @PathVariable String accountId,
            @Valid @RequestBody TransactionRequest request) {
        return transactionService.addTransaction(accountId, request);
    }

    // GET /api/v1/accounts/{accountId}/transactions/export
    @GetMapping(value = "/accounts/{accountId}/transactions/export", produces = "text/csv")
    public ResponseEntity<String> exportToCsv(@PathVariable String accountId) {
        String csvData = transactionService.exportToCsv(accountId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "atttachment; filename=\"transakcje.csv\"")
                .body(csvData);
    }

    // DELETE /api/v1/transactions/{id} - Usunięcie transakcji
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Zwraca status 204
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}
