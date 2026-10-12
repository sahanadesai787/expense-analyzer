package com.example.expenseanalyzer.repository;

import com.example.expenseanalyzer.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findAllByOrderByNameAsc();
}
