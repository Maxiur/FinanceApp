package com.kaminski.FinanceApp.transaction;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    // GET /api/v1/accounts/{idOrName}/transactions?from=2026-01-01&to=2026-12-31&category=Jedzenie
    @GetMapping(value = "/accounts/{idOrName}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable String idOrName,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required=false) String category) {
        return transactionService.getTransactionsForAccount(idOrName, from, to, category);
    }

    // POST /api/v1/accounts/{idOrName}transactions - Dodanie transakcji
    @PostMapping(value = "/accounts/{idOrName}/transactions")
    @ResponseStatus(HttpStatus.CREATED) // Zwraca status 201
    public TransactionResponse addTransaction(
            @PathVariable String idOrName,
            @Valid @RequestBody TransactionRequest request) {
        return transactionService.addTransaction(idOrName, request);
    }

    // GET /api/v1/accounts/{idOrName}/transactions/export
    @GetMapping(value = "/accounts/{idOrName}/transactions/export", produces = "text/csv")
    public ResponseEntity<String> exportToCsv(@PathVariable String idOrName) {
        String csvData = transactionService.exportToCsv(idOrName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transakcje.csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csvData);
    }

    // DELETE /api/v1/transactions/{id} - Usunięcie transakcji
    @DeleteMapping("/transactions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Zwraca status 204
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}
