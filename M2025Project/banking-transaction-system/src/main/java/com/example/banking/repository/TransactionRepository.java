package com.example.banking.repository;

import com.example.banking.model.Transaction;
import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction txn);

    Transaction findById(String id);

    List<Transaction> findByAccountId(String accountId);

    List<Transaction> findAll();
}
