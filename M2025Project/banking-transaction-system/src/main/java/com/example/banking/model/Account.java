package com.example.banking.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Account {
    private final String id;           // UUID
    private final String customerId;   // Link to Customer
    private final String number;       // Account Number (e.g., ACC1001)
    private BigDecimal balance;
    private String status;             // ACTIVE / CLOSED
    private final String type;         // SAVINGS / CURRENT
    private final Instant createdAt;

    public Account(String id, String customerId, String number, BigDecimal initialBalance, String type) {
        this.id = id;
        this.customerId = customerId;
        this.number = number;
        this.balance = initialBalance;
        this.status = "ACTIVE";
        this.type = type;
        this.createdAt = Instant.now();
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getNumber() { return number; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }
}

