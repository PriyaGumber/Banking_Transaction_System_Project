package com.example.banking.strategy;

import com.example.banking.model.Account;
import com.example.banking.model.Transaction;

import java.math.BigDecimal;

public interface TransactionStrategy {
    Transaction execute(Account account, BigDecimal amount, String targetAccountNumber);
}
