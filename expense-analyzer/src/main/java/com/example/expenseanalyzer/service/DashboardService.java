package com.example.expenseanalyzer.service;

import com.example.expenseanalyzer.dto.DashboardResponse;
import com.example.expenseanalyzer.model.TransactionType;
import com.example.expenseanalyzer.repository.BudgetRepository;
import com.example.expenseanalyzer.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** All the calculations: totals, budget usage, alerts, averages, chart data. */
@Service
public class DashboardService {

    static final BigDecimal WARNING_PERCENT = new BigDecimal("80");
    static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int TREND_MONTHS = 6;

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public DashboardService(TransactionRepository transactionRepository, BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String month) {
        return getDashboard(month, LocalDate.now());
    }

    // "today" is a parameter so the logic is easy to unit test.
    DashboardResponse getDashboard(String month, LocalDate today) {
        YearMonth selected = MonthUtil.parse(month);
        LocalDate start = selected.atDay(1);
        LocalDate end = selected.atEndOfMonth();

        BigDecimal income = sumOrZero(TransactionType.INCOME, start, end);
        BigDecimal expenses = sumOrZero(TransactionType.EXPENSE, start, end);
        BigDecimal balance = income.subtract(expenses);

        // Budget
        BigDecimal budget = budgetRepository.findByMonth(selected.toString())
                .map(b -> b.getAmount()).orElse(BigDecimal.ZERO);
        boolean budgetSet = budget.signum() > 0;
        BigDecimal usedPercent = budgetSet
                ? expenses.multiply(HUNDRED).divide(budget, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String status;
        String message;
        if (!budgetSet) {
            status = "NO_BUDGET";
            message = "No budget set for this month. Set one to track your spending.";
        } else if (expenses.compareTo(budget) > 0) {
            status = "EXCEEDED";
            message = "Budget exceeded! You are over by " + expenses.subtract(budget).setScale(2, RoundingMode.HALF_UP) + ".";
        } else if (usedPercent.compareTo(WARNING_PERCENT) > 0) {
            status = "WARNING";
            message = "Warning: you have used " + usedPercent + "% of your budget.";
        } else {
            status = "OK";
            message = "You are within budget (" + usedPercent + "% used).";
        }

        // Highest spending category + pie chart data
        List<Object[]> byCategory = transactionRepository.sumByCategory(TransactionType.EXPENSE, start, end);
        List<String> categoryLabels = new ArrayList<>();
        List<BigDecimal> categoryValues = new ArrayList<>();
        for (Object[] row : byCategory) {
            categoryLabels.add((String) row[0]);
            categoryValues.add((BigDecimal) row[1]);
        }
        String topCategory = categoryLabels.isEmpty() ? null : categoryLabels.get(0);
        BigDecimal topAmount = categoryValues.isEmpty() ? BigDecimal.ZERO : categoryValues.get(0);

        // Monthly trend: the selected month and the 5 before it
        List<String> trendLabels = new ArrayList<>();
        List<BigDecimal> trendValues = new ArrayList<>();
        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            YearMonth ym = selected.minusMonths(i);
            trendLabels.add(ym.toString());
            trendValues.add(sumOrZero(TransactionType.EXPENSE, ym.atDay(1), ym.atEndOfMonth()));
        }

        return new DashboardResponse(selected.toString(), income, expenses, balance, budget, budgetSet,
                usedPercent, status, message, topCategory, topAmount,
                averageDailyExpense(expenses, selected, today),
                categoryLabels, categoryValues, trendLabels, trendValues);
    }

    /**
     * Current month: divide by days passed so far. Past month: by days in that month.
     * Future month: by 1 (avoids dividing by zero).
     */
    static BigDecimal averageDailyExpense(BigDecimal expenses, YearMonth selected, LocalDate today) {
        YearMonth current = YearMonth.from(today);
        int days;
        if (selected.equals(current)) {
            days = today.getDayOfMonth();
        } else if (selected.isBefore(current)) {
            days = selected.lengthOfMonth();
        } else {
            days = 1;
        }
        return expenses.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumOrZero(TransactionType type, LocalDate start, LocalDate end) {
        BigDecimal total = transactionRepository.sumByType(type, start, end);
        return total == null ? BigDecimal.ZERO : total;
    }
}
