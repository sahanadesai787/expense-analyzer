package com.example.expenseanalyzer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.expenseanalyzer.dto.DashboardResponse;
import com.example.expenseanalyzer.model.Budget;
import com.example.expenseanalyzer.model.TransactionType;
import com.example.expenseanalyzer.repository.BudgetRepository;
import com.example.expenseanalyzer.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DashboardServiceTest {

    private DashboardResponse run(String expenses, String budgetAmount) {
        TransactionRepository txRepo = mock(TransactionRepository.class);
        BudgetRepository budgetRepo = mock(BudgetRepository.class);
        when(txRepo.sumByType(eq(TransactionType.INCOME), any(), any())).thenReturn(new BigDecimal("5000"));
        when(txRepo.sumByType(eq(TransactionType.EXPENSE), any(), any())).thenReturn(new BigDecimal(expenses));
        when(txRepo.sumByCategory(eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{"Food", new BigDecimal(expenses)}));
        Budget b = new Budget();
        b.setAmount(new BigDecimal(budgetAmount));
        when(budgetRepo.findByMonth("2026-10"))
                .thenReturn(budgetAmount.equals("0") ? Optional.empty() : Optional.of(b));
        return new DashboardService(txRepo, budgetRepo).getDashboard("2026-10", LocalDate.of(2026, 10, 10));
    }

    @Test void underEightyPercentIsOk() {
        assertEquals("OK", run("700", "1000").budgetStatus());
    }
    @Test void exactlyEightyPercentIsStillOk() {
        assertEquals("OK", run("800", "1000").budgetStatus());
    }
    @Test void overEightyPercentIsWarning() {
        DashboardResponse r = run("850", "1000");
        assertEquals("WARNING", r.budgetStatus());
        assertEquals(new BigDecimal("85.0"), r.budgetUsedPercent());
    }
    @Test void overBudgetIsExceeded() {
        assertEquals("EXCEEDED", run("1200", "1000").budgetStatus());
    }
    @Test void noBudgetIsReported() {
        assertEquals("NO_BUDGET", run("100", "0").budgetStatus());
    }
    @Test void balanceAndHighestCategory() {
        DashboardResponse r = run("1200", "1000");
        assertEquals(new BigDecimal("3800"), r.remainingBalance());
        assertEquals("Food", r.highestSpendingCategory());
    }
    @Test void averageDailyUsesDaysElapsedInCurrentMonth() {
        // 10th of the month -> 10 days
        assertEquals(new BigDecimal("30.00"),
                DashboardService.averageDailyExpense(new BigDecimal("300"), YearMonth.of(2026, 10), LocalDate.of(2026, 10, 10)));
    }
    @Test void averageDailyUsesFullMonthForPastMonths() {
        // September has 30 days
        assertEquals(new BigDecimal("10.00"),
                DashboardService.averageDailyExpense(new BigDecimal("300"), YearMonth.of(2026, 9), LocalDate.of(2026, 10, 10)));
    }
}
