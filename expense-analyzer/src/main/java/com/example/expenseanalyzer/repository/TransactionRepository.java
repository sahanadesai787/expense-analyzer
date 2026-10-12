package com.example.expenseanalyzer.repository;

import com.example.expenseanalyzer.model.Transaction;
import com.example.expenseanalyzer.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// JpaSpecificationExecutor lets us build the search/filter query dynamically.
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /** Total amount of one type between two dates (null when there are no rows). */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.date BETWEEN :start AND :end")
    BigDecimal sumByType(@Param("type") TransactionType type,
                         @Param("start") LocalDate start,
                         @Param("end") LocalDate end);

    /** Each row is [categoryName, totalAmount], biggest first. */
    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.type = :type AND t.date BETWEEN :start AND :end " +
           "GROUP BY t.category.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumByCategory(@Param("type") TransactionType type,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);
}
