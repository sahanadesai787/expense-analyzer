package com.example.expenseanalyzer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BudgetRequest {
    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Budget can have at most 2 decimal places")
    private BigDecimal amount;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
