package com.kaminski.FinanceApp.account;

import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import com.kaminski.FinanceApp.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Sprzątanie po sobie
class AccountControllerTest extends com.kaminski.FinanceApp.BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // wirtualny Postman

    @Autowired
    private ObjectMapper objectMapper; // Narzędzie do zamiany obiektów na JSON

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Zanim odpalimy jakikolwiek test, czyścimy bazę, żeby testy nie wchodziły sobie w drogę!
    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void shouldCreateNewAccountSuccessfully() throws Exception {
        // GIVEN - przygotowujemy request z JSON-em
        AccountRequest request = new AccountRequest("Konto testowe");
        String jsonRequest = objectMapper.writeValueAsString(request);

        // WHEN & THEN - wirtualny Postman rzuca POST
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated()) // Sprawdzamy czy kontroler zwrócił 201 CREATED
                .andExpect(jsonPath("$.name").value("Konto testowe")) // Sprawdzamy co jest w JSONie
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.id").exists()); // ID musi zostać wygenerowane przez bazę
    }

    @Test
    void shouldReturnBadRequestWhenNameIsInvalid() throws Exception {
        // GIVEN - request z pustą nazwą
        AccountRequest badRequest = new AccountRequest("");
        String jsonRequest = objectMapper.writeValueAsString(badRequest);

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest()); // Oczekujemy 400 Bad Request, bo poleciał @Valid
    }
}
