package com.kaminski.FinanceApp.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(""" 
           SELECT t FROM Transaction t WHERE t.account.id =:accountId
           AND (cast(:from as timestamp) IS NULL OR t.transactionDate >= :from)
           AND (cast(:to as timestamp) IS NULL OR t.transactionDate <= :to)
           AND (:category = '' OR LOWER(t.category) = :category)
           """)
    List<Transaction> findFilteredTransactions(Long accountId, LocalDateTime from, LocalDateTime to, String category);

    List<Transaction> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type")
    BigDecimal calculateTotalAmountByType(@Param("type") TransactionType type);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t where t.type = 'EXPENSE' GROUP BY t.category")
    List<Object[]> getExpensesGroupedByCategory();
}
