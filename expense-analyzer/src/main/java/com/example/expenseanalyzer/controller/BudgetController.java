package com.example.expenseanalyzer.controller;

import com.example.expenseanalyzer.dto.BudgetRequest;
import com.example.expenseanalyzer.dto.BudgetResponse;
import com.example.expenseanalyzer.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/{month}")
    public BudgetResponse get(@PathVariable String month) {
        return budgetService.getBudget(month);
    }

    // PUT creates the budget or updates it if the month already has one
    @PutMapping("/{month}")
    public BudgetResponse set(@PathVariable String month, @Valid @RequestBody BudgetRequest request) {
        return budgetService.setBudget(month, request.getAmount());
    }

    @DeleteMapping("/{month}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String month) {
        budgetService.deleteBudget(month);
    }
}
