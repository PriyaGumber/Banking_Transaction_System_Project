package com.example.banking.service;

import com.example.banking.exception.*;
import com.example.banking.factory.AccountFactory;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;

public class AccountService {

    private static AccountService instance; // Singleton
    private final AccountRepository accountRepository;

    private AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public static AccountService getInstance(AccountRepository accountRepository) {
        if (instance == null) {
            instance = new AccountService(accountRepository);
        }
        return instance;
    }

    // ✅ Create a new account
    public Account createAccount(String customerId, BigDecimal initialDeposit, String type) {
        String normalizedType = type.toUpperCase();

        // Check if customer already has this type
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        for (Account acc : accounts) {
            if (acc.getType().equals(normalizedType) && !"CLOSED".equals(acc.getStatus())) {
                throw new AccountAlreadyExistsException(
                        "Customer already has an active " + normalizedType + " account");
            }
        }

        // Factory creates account with zero balance
        Account account = AccountFactory.createAccount(customerId, BigDecimal.ZERO, normalizedType);
        return accountRepository.save(account);
    }

    // ✅ View balance
    public BigDecimal viewBalance(String accountNumber, String actorId) {
        Account acc = accountRepository.findByNumber(accountNumber);
        if (acc == null) throw new AccountNotFoundException("Account not found: " + accountNumber);
        if (!acc.getCustomerId().equals(actorId))
            throw new UnauthorizedAccessException("You are not authorized to view this account");
        if (!"ACTIVE".equals(acc.getStatus()))
            throw new AccountClosedException("Account " + accountNumber + " is closed");

        return acc.getBalance();
    }

    // Helper: return account number (ACC...) for a given account id (UUID).
// If accountId is null -> returns " ".
// If account not found -> returns the original id (fallback).
    public String getAccountNumberById(String accountId) {
        if (accountId == null) return "-";
        Account acc = accountRepository.findById(accountId);
        return (acc != null) ? acc.getNumber() : accountId;
    }

// ✅ Close account
    public void closeAccount(String accountNumber, String actorId) {
        Account acc = accountRepository.findByNumber(accountNumber);
        if (acc == null) throw new AccountNotFoundException("Account not found: " + accountNumber);
        if (!acc.getCustomerId().equals(actorId))
            throw new UnauthorizedAccessException("You are not authorized to close this account");
        if ("CLOSED".equals(acc.getStatus()))
            throw new AccountClosedException("Account " + accountNumber + " is already closed");

        acc.setStatus("CLOSED");
        accountRepository.save(acc);
    }

    // ✅ List all accounts of a customer
    public List<Account> getCustomerAccounts(String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    // ✅ Get account by type
    public Account getAccountByType(String customerId, String type) {
        Account acc = accountRepository.findByCustomerIdAndType(customerId, type.toUpperCase());
        if (acc == null) {
            throw new AccountNotFoundException("No " + type + " account found for this customer");
        }
        return acc;
    }
}
