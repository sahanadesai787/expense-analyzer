package com.example.expenseanalyzer.repository;

import com.example.expenseanalyzer.model.Budget;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByMonth(String month);
}
