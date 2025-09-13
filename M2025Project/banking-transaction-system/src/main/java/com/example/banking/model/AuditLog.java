package com.example.banking.model;

import java.math.BigDecimal;
import java.time.Instant;

public class AuditLog {
    private final String id;          // UUID
    private final String txnId;       // Related transaction
    private final String accountId;
    private final String actor;       // Customer ID (who performed it)
    private final String action;      // DEPOSIT / WITHDRAW / TRANSFER / CLOSE_ACCOUNT
    private final BigDecimal beforeBalance;
    private final BigDecimal afterBalance;
    private final Instant createdAt;

    public AuditLog(String id, String txnId, String accountId, String actor, String action,
                    BigDecimal beforeBalance, BigDecimal afterBalance) {
        this.id = id;
        this.txnId = txnId;
        this.accountId = accountId;
        this.actor = actor;
        this.action = action;
        this.beforeBalance = beforeBalance;
        this.afterBalance = afterBalance;
        this.createdAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getTxnId() { return txnId; }
    public String getAccountId() { return accountId; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public BigDecimal getBeforeBalance() { return beforeBalance; }
    public BigDecimal getAfterBalance() { return afterBalance; }
    public Instant getCreatedAt() { return createdAt; }
}
