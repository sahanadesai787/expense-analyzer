package com.example.expenseanalyzer.controller;

import com.example.expenseanalyzer.dto.DashboardResponse;
import com.example.expenseanalyzer.model.Category;
import com.example.expenseanalyzer.repository.CategoryRepository;
import com.example.expenseanalyzer.service.DashboardService;
import java.time.YearMonth;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CategoryRepository categoryRepository;

    public DashboardController(DashboardService dashboardService, CategoryRepository categoryRepository) {
        this.dashboardService = dashboardService;
        this.categoryRepository = categoryRepository;
    }

    // GET /api/dashboard?month=2026-10  (defaults to the current month)
    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestParam(required = false) String month) {
        String selected = (month == null || month.isBlank()) ? YearMonth.now().toString() : month;
        return dashboardService.getDashboard(selected);
    }

    // GET /api/categories  (used for dropdowns and suggestions)
    @GetMapping("/categories")
    public List<String> categories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(Category::getName).toList();
    }
}
