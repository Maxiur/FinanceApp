package com.kaminski.FinanceApp.account;

import com.kaminski.FinanceApp.exception.ResourceNotFoundException;
import com.kaminski.FinanceApp.exception.UnprocessableContentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountResolver {
    private final AccountRepository accountRepository;

    public Account resolve(String param) {
        try {
            Long numericId = Long.parseLong(param);
            if (numericId <= 0) {
                throw new UnprocessableContentException("ID konta musi być liczbą dodatnią!");
            }
            return accountRepository.findById(numericId)
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o ID: " + numericId));
        } catch (NumberFormatException e) {
            return accountRepository.findByName(param)
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono konta o nazwie: " + param));
        }
    }
}
