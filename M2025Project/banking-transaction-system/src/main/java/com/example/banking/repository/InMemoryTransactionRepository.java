package com.example.banking.repository;

import com.example.banking.model.Transaction;

import java.util.*;

public class InMemoryTransactionRepository implements TransactionRepository {
    private final Map<String, Transaction> transactionsById = new HashMap<>();
    private final Map<String, List<Transaction>> transactionsByAccount = new HashMap<>();

    @Override
    public Transaction save(Transaction txn) {
        transactionsById.put(txn.getId(), txn);

        if (txn.getFromAccountId() != null) {
            transactionsByAccount
                    .computeIfAbsent(txn.getFromAccountId(), k -> new ArrayList<>())
                    .add(txn);
        }
        if (txn.getToAccountId() != null) {
            transactionsByAccount
                    .computeIfAbsent(txn.getToAccountId(), k -> new ArrayList<>())
                    .add(txn);
        }

        return txn;
    }

    @Override
    public Transaction findById(String id) {
        return transactionsById.get(id); // null if not found
    }

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) {
        return transactionsByAccount.getOrDefault(accountNumber, new ArrayList<>());
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactionsById.values());
    }
}


