package com.example.banking.service;

import com.example.banking.model.Transaction;
import com.example.banking.repository.TransactionRepository;

import java.util.*;

public class MiniStatementService {
    private static MiniStatementService instance;
    private final TransactionRepository transactionRepository;

    // Map<AccountNumber, Queue<Transaction>>
    private final Map<String, Deque<Transaction>> miniStatements = new HashMap<>();

    private MiniStatementService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public static MiniStatementService getInstance(TransactionRepository transactionRepository) {
        if (instance == null) {
            instance = new MiniStatementService(transactionRepository);
        }
        return instance;
    }

    // Load last 5 on login
    public void loadInitial(String accountId) {
        List<Transaction> txns = transactionRepository.findByAccountId(accountId);
        Deque<Transaction> queue = new ArrayDeque<>();

        txns.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
                .limit(5)
                .forEach(queue::add);

        miniStatements.put(accountId, queue);
    }

    // Add new successful txn
    public void addTransaction(String accountId, Transaction txn) {
        if (!"SUCCESS".equalsIgnoreCase(txn.getStatus())) return;

        Deque<Transaction> queue = miniStatements.computeIfAbsent(accountId, k -> new ArrayDeque<>());

        if (queue.size() == 5) queue.removeLast(); // FIFO
        queue.addFirst(txn);
    }

    // Get last 5 for display
    public List<Transaction> getMiniStatement(String accountId) {
        return new ArrayList<>(miniStatements.getOrDefault(accountId, new ArrayDeque<>()));
    }
}
