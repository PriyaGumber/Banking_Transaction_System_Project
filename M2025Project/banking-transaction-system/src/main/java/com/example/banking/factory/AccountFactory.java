package com.example.banking.factory;

import com.example.banking.exception.InvalidAccountTypeException;
import com.example.banking.model.Account;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountFactory {

    public static Account createAccount(String customerId, BigDecimal initialDeposit, String type) {
        // Normalize type (uppercase)
        String t = type.toUpperCase();

        // Validate account type
        if (!t.equals("SAVINGS") && !t.equals("CURRENT")) {
            throw new InvalidAccountTypeException("Invalid account type: " + type + ". Must be SAVINGS or CURRENT");
        }

        // Generate unique account details
        String id = UUID.randomUUID().toString();
        String accountNumber = "ACC" + System.currentTimeMillis();

        // Create and return Account
        return new Account(id, customerId, accountNumber, initialDeposit, t);
    }
}
