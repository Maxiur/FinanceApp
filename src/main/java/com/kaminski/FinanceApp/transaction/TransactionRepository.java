package com.kaminski.FinanceApp.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(""" 
           SELECT t FROM Transaction t WHERE t.account.id =:accountId
           AND (:from IS NULL OR t.transactionDate >= :from)
           AND (:to IS NULL OR t.transactionDate <= :to)
           AND (:category IS NULL OR LOWER(t.category) = LOWER(:category))
           """)
    List<Transaction> findFilteredTransactions(Long accountId, LocalDateTime from, LocalDateTime to, String category);

    @Query("SELECT t FROM Transaction t WHERE t.account.id =:accountId")
    List<Transaction> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);
}
