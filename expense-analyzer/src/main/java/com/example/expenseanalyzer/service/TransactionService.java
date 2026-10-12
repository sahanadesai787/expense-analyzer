package com.example.expenseanalyzer.service;

import com.example.expenseanalyzer.dto.TransactionRequest;
import com.example.expenseanalyzer.dto.TransactionResponse;
import com.example.expenseanalyzer.exception.ResourceNotFoundException;
import com.example.expenseanalyzer.model.Category;
import com.example.expenseanalyzer.model.Transaction;
import com.example.expenseanalyzer.model.TransactionType;
import com.example.expenseanalyzer.repository.CategoryRepository;
import com.example.expenseanalyzer.repository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Transaction transaction = new Transaction();
        copyFields(request, transaction);
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        Transaction transaction = findOrThrow(id);
        copyFields(request, transaction);
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(Long id) {
        transactionRepository.delete(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        return TransactionResponse.from(findOrThrow(id));
    }

    /** Search + filter + sort. Every parameter is optional. */
    @Transactional(readOnly = true)
    public List<TransactionResponse> search(String keyword, String category, TransactionType type,
                                            String month, String sortBy, String direction) {
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> filters = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                filters.add(cb.or(
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("category").get("name")), like)));
            }
            if (category != null && !category.isBlank()) {
                filters.add(cb.equal(cb.lower(root.get("category").get("name")), category.trim().toLowerCase()));
            }
            if (type != null) {
                filters.add(cb.equal(root.get("type"), type));
            }
            if (month != null && !month.isBlank()) {
                YearMonth ym = MonthUtil.parse(month);
                filters.add(cb.between(root.<LocalDate>get("date"), ym.atDay(1), ym.atEndOfMonth()));
            }
            return cb.and(filters.toArray(new Predicate[0]));
        };

        String field = "amount".equalsIgnoreCase(sortBy) ? "amount" : "date";
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, field).and(Sort.by(Sort.Direction.DESC, "id"));

        return transactionRepository.findAll(spec, sort).stream().map(TransactionResponse::from).toList();
    }

    // ----- helpers -----
    private Transaction findOrThrow(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id " + id));
    }

    private void copyFields(TransactionRequest request, Transaction transaction) {
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(findOrCreateCategory(request.getCategoryName().trim()));
    }

    /** Re-use an existing category (case-insensitive) or create a new one. */
    private Category findOrCreateCategory(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new Category(name)));
    }
}
