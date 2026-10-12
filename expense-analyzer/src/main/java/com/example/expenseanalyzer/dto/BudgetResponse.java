package com.example.expenseanalyzer.dto;

import java.math.BigDecimal;

public record BudgetResponse(String month, BigDecimal amount) { }
