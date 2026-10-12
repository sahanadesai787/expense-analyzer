package com.example.expenseanalyzer.dto;

import com.example.expenseanalyzer.model.Transaction;
import com.example.expenseanalyzer.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(Long id, BigDecimal amount, TransactionType type,
                                  String categoryName, LocalDate date, String description) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAmount(), t.getType(),
                t.getCategory().getName(), t.getDate(), t.getDescription());
    }
}
