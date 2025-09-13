package com.example.banking.repository;

import com.example.banking.model.Transaction;
import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction txn);

    Transaction findById(String id);

    List<Transaction> findByAccountNumber(String accountNumber);

    List<Transaction> findAll();
}
