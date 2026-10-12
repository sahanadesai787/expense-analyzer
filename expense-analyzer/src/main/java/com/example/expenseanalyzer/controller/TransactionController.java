package com.example.expenseanalyzer.controller;

import com.example.expenseanalyzer.dto.TransactionRequest;
import com.example.expenseanalyzer.dto.TransactionResponse;
import com.example.expenseanalyzer.model.TransactionType;
import com.example.expenseanalyzer.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // GET /api/transactions?keyword=&category=&type=&month=2026-10&sortBy=amount&direction=desc
    @GetMapping
    public List<TransactionResponse> search(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) TransactionType type,
                                            @RequestParam(required = false) String month,
                                            @RequestParam(defaultValue = "date") String sortBy,
                                            @RequestParam(defaultValue = "desc") String direction) {
        return transactionService.search(keyword, category, type, month, sortBy, direction);
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable Long id) {
        return transactionService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request) {
        return transactionService.create(request);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        transactionService.delete(id);
    }
}
