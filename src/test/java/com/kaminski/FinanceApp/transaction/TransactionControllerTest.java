package com.kaminski.FinanceApp.transaction;

import tools.jackson.databind.ObjectMapper;
import com.kaminski.FinanceApp.account.Account;
import com.kaminski.FinanceApp.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    // Zmienna, w której przechowamy konto do testów transakcji
    private Account testAccount;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        // Tworzymy skarbonkę na potrzeby testów
        testAccount = Account.builder()
                .name("Konto testowe")
                .balance(new BigDecimal("1000.00"))
                .build();
        testAccount = accountRepository.save(testAccount);
    }

    @Test
    void shouldAddTransactionSuccessfully() throws Exception {
        // GIVEN
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("200.00"),
                TransactionType.EXPENSE,
                "Jedzenie",
                "Kebsik z ostrym"
        );
        String jsonRequest = objectMapper.writeValueAsString(request);

        // WHEN & THEN - Strzelamy pod adres powiązany z naszym testowym kontem
        mockMvc.perform(post("/api/v1/accounts/" + testAccount.getId() + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated()) // Oczekujemy 201
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.category").value("Jedzenie"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsNegative() throws Exception {
        // GIVEN - Kwota ujemna!
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("-50.00"),
                TransactionType.EXPENSE,
                "Błędy",
                "To nie ma prawa przejść"
        );
        String jsonRequest = objectMapper.writeValueAsString(request);

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/accounts/" + testAccount.getId() + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest()); // Oczekujemy 400 Bad Request
    }

    @Test
    void shouldGetTransactionsForAccount() throws Exception {
        // GIVEN - Dorzucamy transakcję prosto do bazy, z pominięciem kontrolera
        Transaction t = Transaction.builder()
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.INCOME)
                .category("Wypłata")
                .transactionDate(LocalDateTime.now())
                .account(testAccount)
                .build();
        transactionRepository.save(t);

        // WHEN & THEN - Pobieramy GETem
        mockMvc.perform(get("/api/v1/accounts/" + testAccount.getId() + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(150.00)) // Sprawdzamy pierwszy element tablicy JSON
                .andExpect(jsonPath("$[0].type").value("INCOME"))
                .andExpect(jsonPath("$[0].category").value("Wypłata"));
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        // GIVEN - Tworzymy transakcję do usunięcia
        Transaction t = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .category("Kawa")
                .transactionDate(LocalDateTime.now())
                .account(testAccount)
                .build();
        t = transactionRepository.save(t);

        // WHEN & THEN - Strzelamy w endpoint DELETE
        mockMvc.perform(delete("/api/v1/transactions/" + t.getId()))
                .andExpect(status().isNoContent()); // Oczekujemy 204 No Content
    }
}
