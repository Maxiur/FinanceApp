package com.kaminski.FinanceApp.summary;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/summary")
@RequiredArgsConstructor
public class SummaryController {
    private final SummaryService summaryService;

    @GetMapping
    public SummaryResponse getSummary() {
        return summaryService.getSummary();
    }

    @GetMapping("/limits")
    public Map<String, BigDecimal> getLimits() {
        return summaryService.getLimits();
    }
}
