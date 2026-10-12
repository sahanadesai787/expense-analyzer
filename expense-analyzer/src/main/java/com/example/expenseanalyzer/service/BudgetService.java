package com.example.expenseanalyzer.service;

import com.example.expenseanalyzer.dto.BudgetResponse;
import com.example.expenseanalyzer.exception.ResourceNotFoundException;
import com.example.expenseanalyzer.model.Budget;
import com.example.expenseanalyzer.repository.BudgetRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /** Create the budget for a month, or update it if it already exists. */
    @Transactional
    public BudgetResponse setBudget(String month, BigDecimal amount) {
        String normalizedMonth = MonthUtil.parse(month).toString();
        Budget budget = budgetRepository.findByMonth(normalizedMonth).orElseGet(Budget::new);
        budget.setMonth(normalizedMonth);
        budget.setAmount(amount);
        Budget saved = budgetRepository.save(budget);
        return new BudgetResponse(saved.getMonth(), saved.getAmount());
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(String month) {
        String normalizedMonth = MonthUtil.parse(month).toString();
        Budget budget = budgetRepository.findByMonth(normalizedMonth)
                .orElseThrow(() -> new ResourceNotFoundException("No budget set for " + normalizedMonth));
        return new BudgetResponse(budget.getMonth(), budget.getAmount());
    }

    @Transactional
    public void deleteBudget(String month) {
        String normalizedMonth = MonthUtil.parse(month).toString();
        Budget budget = budgetRepository.findByMonth(normalizedMonth)
                .orElseThrow(() -> new ResourceNotFoundException("No budget set for " + normalizedMonth));
        budgetRepository.delete(budget);
    }
}
