package com.example.banking.strategy;

import com.example.banking.exception.NegativeAmountException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

public class WithdrawStrategy implements TransactionStrategy {

    @Override
    public Transaction execute(Account account, BigDecimal amount, String targetAccountNumber) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegativeAmountException("Withdraw amount must be positive");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        account.setBalance(account.getBalance().subtract(amount));

        return new Transaction(
                UUID.randomUUID().toString(),
                account.getId(), // fromAccountId
                null,            // toAccountId
                "WITHDRAW",
                amount
        );
    }
}
