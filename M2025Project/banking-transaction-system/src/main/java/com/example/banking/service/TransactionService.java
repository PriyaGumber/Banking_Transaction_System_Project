package com.example.banking.service;

import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.AccountClosedException;
import com.example.banking.exception.UnauthorizedAccessException;
import com.example.banking.exception.TransactionFailedException;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.AuditLog;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.AuditLogRepository;
import com.example.banking.strategy.DepositStrategy;
import com.example.banking.strategy.WithdrawStrategy;
import com.example.banking.strategy.TransferStrategy;
import com.example.banking.strategy.TransactionStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class TransactionService {

    private static TransactionService instance;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    private TransactionService(AccountRepository accountRepository,
                               TransactionRepository transactionRepository,
                               AuditLogRepository auditLogRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public static TransactionService getInstance(AccountRepository accountRepository,
                                                 TransactionRepository transactionRepository,
                                                 AuditLogRepository auditLogRepository) {
        if (instance == null) {
            instance = new TransactionService(accountRepository, transactionRepository, auditLogRepository);
        }
        return instance;
    }

    // Deposit
    public Transaction deposit(String accountNumber, BigDecimal amount, String actorId) {
        Account account = accountRepository.findByNumber(accountNumber);
        if (account == null) throw new AccountNotFoundException("Account not found");

        BigDecimal before = account.getBalance();

        if (!account.getCustomerId().equals(actorId)) {
            throw new UnauthorizedAccessException("Unauthorized: cannot deposit to this account");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new AccountClosedException("Account is closed");
        }

        try {
            TransactionStrategy strategy = new DepositStrategy();
            Transaction txn = strategy.execute(account, amount, null);

            accountRepository.save(account);
            transactionRepository.save(txn);

            AuditLog log = new AuditLog(UUID.randomUUID().toString(), txn.getId(), account.getId(),
                    actorId, "DEPOSIT", before, account.getBalance());
            auditLogRepository.save(log);

            txn.setStatus("SUCCESS");
            return txn;
        } catch (Exception ex) {
            Transaction failTxn = new Transaction(UUID.randomUUID().toString(), null, account.getId(), "DEPOSIT", amount);
            failTxn.setStatus("FAILED");
            transactionRepository.save(failTxn);

            AuditLog failLog = new AuditLog(UUID.randomUUID().toString(), failTxn.getId(), account.getId(),
                    actorId, "DEPOSIT_FAILED:" + ex.getMessage(), before, before);
            auditLogRepository.save(failLog);

            throw new TransactionFailedException("Deposit failed: " + ex.getMessage());
        }
    }

    // Withdraw
    public Transaction withdraw(String accountNumber, BigDecimal amount, String actorId) {
        Account account = accountRepository.findByNumber(accountNumber);
        if (account == null) throw new AccountNotFoundException("Account not found");

        BigDecimal before = account.getBalance();

        if (!account.getCustomerId().equals(actorId)) {
            throw new UnauthorizedAccessException("Unauthorized: cannot withdraw from this account");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new AccountClosedException("Account is closed");
        }

        try {
            TransactionStrategy strategy = new WithdrawStrategy();
            Transaction txn = strategy.execute(account, amount, null);

            accountRepository.save(account);
            transactionRepository.save(txn);

            AuditLog log = new AuditLog(UUID.randomUUID().toString(), txn.getId(), account.getId(),
                    actorId, "WITHDRAW", before, account.getBalance());
            auditLogRepository.save(log);

            txn.setStatus("SUCCESS");
            return txn;
        } catch (Exception ex) {
            Transaction failTxn = new Transaction(UUID.randomUUID().toString(), account.getId(), null, "WITHDRAW", amount);
            failTxn.setStatus("FAILED");
            transactionRepository.save(failTxn);

            AuditLog failLog = new AuditLog(UUID.randomUUID().toString(), failTxn.getId(), account.getId(),
                    actorId, "WITHDRAW_FAILED:" + ex.getMessage(), before, before);
            auditLogRepository.save(failLog);

            throw new TransactionFailedException("Withdrawal failed: " + ex.getMessage());
        }
    }

    // Transfer
    public Transaction transfer(String srcAccountNumber, String destAccountNumber, BigDecimal amount, String actorId) {
        if (srcAccountNumber.equals(destAccountNumber)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        Account source = accountRepository.findByNumber(srcAccountNumber);
        if (source == null) throw new AccountNotFoundException("Source account not found");

        Account dest = accountRepository.findByNumber(destAccountNumber);
        if (dest == null) throw new AccountNotFoundException("Destination account not found");

        if (!source.getCustomerId().equals(actorId)) {
            throw new UnauthorizedAccessException("Unauthorized: cannot transfer from this account");
        }
        if (!"ACTIVE".equals(source.getStatus()) || !"ACTIVE".equals(dest.getStatus())) {
            throw new AccountClosedException("Source or destination account is closed");
        }

        BigDecimal beforeSrc = source.getBalance();
        BigDecimal beforeDest = dest.getBalance();

        try {
            TransactionStrategy strategy = new TransferStrategy(dest);
            Transaction txn = strategy.execute(source, amount, dest.getNumber());

            source = accountRepository.save(source);
            dest = accountRepository.save(dest);

            txn.setStatus("SUCCESS");
            transactionRepository.save(txn);

            AuditLog log1 = new AuditLog(UUID.randomUUID().toString(), txn.getId(), source.getId(),
                    actorId, "TRANSFER-DEBIT", beforeSrc, source.getBalance());
            AuditLog log2 = new AuditLog(UUID.randomUUID().toString(), txn.getId(), dest.getId(),
                    actorId, "TRANSFER-CREDIT", beforeDest, dest.getBalance());

            auditLogRepository.save(log1);
            auditLogRepository.save(log2);

            return txn;
        } catch (Exception ex) {
            // ✅ Only source account gets a failed transaction log
            Transaction failTxn = new Transaction(UUID.randomUUID().toString(), source.getId(), null, "TRANSFER", amount);
            failTxn.setStatus("FAILED");
            transactionRepository.save(failTxn);

            AuditLog failLog = new AuditLog(UUID.randomUUID().toString(), failTxn.getId(), source.getId(),
                    actorId, "TRANSFER_FAILED:" + ex.getMessage(), beforeSrc, beforeSrc);
            auditLogRepository.save(failLog);

            throw new TransactionFailedException("Transfer failed: " + ex.getMessage());
        }
    }

    // Transaction history
    public List<Transaction> getHistory(String accountNumber, String actorId) {
        Account account = accountRepository.findByNumber(accountNumber);
        if (account == null) throw new AccountNotFoundException("Account not found");

        if (!account.getCustomerId().equals(actorId)) {
            throw new UnauthorizedAccessException("Unauthorized: cannot view this account's transactions");
        }

        return transactionRepository.findByAccountNumber(account.getId());
    }
}
