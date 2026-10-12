package com.example.expenseanalyzer.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Month in the format YYYY-MM, e.g. 2026-10. One budget per month. */
    @Column(nullable = false, unique = true, length = 7)
    private String month;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    public Long getId() { return id; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
