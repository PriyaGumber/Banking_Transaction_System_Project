package com.example.banking.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Transaction {
    private final String id;          // UUID
    private final String fromAccountId; // null for Deposit
    private final String toAccountId;   // null for Withdraw
    private final String type;          // DEPOSIT / WITHDRAW / TRANSFER
    private final BigDecimal amount;
    private String status;              // SUCCESS / FAIL
    private final Instant createdAt;

    public Transaction(String id, String fromAccountId, String toAccountId, String type, BigDecimal amount) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.type = type;
        this.amount = amount;
        this.status = "PENDING";
        this.createdAt = Instant.now();
    }

    public Transaction(String id, String fromAccountId, String toAccountId, String type, BigDecimal amount, String status, Instant createdAt) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getFromAccountId() { return fromAccountId; }
    public String getToAccountId() { return toAccountId; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
