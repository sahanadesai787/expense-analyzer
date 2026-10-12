package com.example.expenseanalyzer.dto;

import com.example.expenseanalyzer.model.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Data the browser sends when adding or editing a transaction. */
public class TransactionRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount can have at most 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Type must be INCOME or EXPENSE")
    private TransactionType type;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category can be at most 50 characters")
    private String categoryName;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Size(max = 255, message = "Description can be at most 255 characters")
    private String description;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
