package com.example.expenseanalyzer.dto;

import java.math.BigDecimal;
import java.util.List;

/** Everything the dashboard page needs for one month, in a single response. */
public record DashboardResponse(
        String month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal remainingBalance,
        BigDecimal monthlyBudget,        // 0 when no budget is set
        boolean budgetSet,
        BigDecimal budgetUsedPercent,
        String budgetStatus,             // NO_BUDGET, OK, WARNING, EXCEEDED
        String budgetMessage,
        String highestSpendingCategory,  // null when there are no expenses
        BigDecimal highestSpendingAmount,
        BigDecimal averageDailyExpense,
        List<String> categoryLabels,
        List<BigDecimal> categoryValues,
        List<String> trendLabels,
        List<BigDecimal> trendValues) { }
