package com.example.banking.strategy;

import com.example.banking.exception.NegativeAmountException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferStrategy implements TransactionStrategy {

    private final Account targetAccount;

    public TransferStrategy(Account targetAccount) {
        this.targetAccount = targetAccount;
    }

    @Override
    public Transaction execute(Account sourceAccount, BigDecimal amount, String targetAccountNumber) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegativeAmountException("Transfer amount must be positive");
        }
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // Deduct from source
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));

        // Credit to destination
        targetAccount.setBalance(targetAccount.getBalance().add(amount));

        return new Transaction(
                UUID.randomUUID().toString(),
                sourceAccount.getId(),
                targetAccount.getId(),
                "TRANSFER",
                amount
        );
    }
}
