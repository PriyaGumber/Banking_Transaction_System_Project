package com.example.banking.strategy;

import com.example.banking.exception.NegativeAmountException;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

public class DepositStrategy implements TransactionStrategy {

    @Override
    public Transaction execute(Account account, BigDecimal amount, String targetAccountNumber) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegativeAmountException("Deposit amount must be positive");
        }

        account.setBalance(account.getBalance().add(amount));

        return new Transaction(
                UUID.randomUUID().toString(),
                null, // fromAccountId
                account.getId(), // toAccountId
                "DEPOSIT",
                amount
        );
    }
}
