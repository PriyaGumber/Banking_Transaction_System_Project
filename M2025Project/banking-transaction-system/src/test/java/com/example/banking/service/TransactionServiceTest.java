package com.example.banking.service;

import com.example.banking.exception.TransactionFailedException;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.AuditLogRepository;
import com.example.banking.repository.DynamoDBAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private DynamoDBAuditLogRepository dynamoDbAuditLogRepository;

    private TransactionService transactionService;

    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instances
        resetSingleton(TransactionService.class);
        resetSingleton(MiniStatementService.class);

        transactionService = TransactionService.getInstance(
                accountRepository, transactionRepository, auditLogRepository, null
        );

        // Inject DynamoDB mock to avoid NullPointerException
        var dynField = TransactionService.class.getDeclaredField("dynamoDbAuditLogRepository");
        dynField.setAccessible(true);
        dynField.set(transactionService, dynamoDbAuditLogRepository);

        // Lenient stubbing for DynamoDB to prevent NPE
        lenient().when(dynamoDbAuditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        account1 = new Account("A1", "ACTOR1", "ACC1001", BigDecimal.valueOf(1000), "SAVINGS");
        account2 = new Account("A2", "ACTOR1", "ACC2001", BigDecimal.valueOf(500), "CURRENT");

        // ✅ Lenient stubbing to prevent unnecessary stubbing errors
        lenient().when(accountRepository.findByNumber("ACC1001")).thenReturn(account1);
        lenient().when(accountRepository.findByNumber("ACC2001")).thenReturn(account2);

        lenient().when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void resetSingleton(Class<?> clazz) throws Exception {
        var field = clazz.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }

    // ---------- Deposit ----------
    @Test
    void deposit_success_increasesBalance() {
        transactionService.deposit("ACC1001", BigDecimal.valueOf(200), "ACTOR1");
        assertEquals(BigDecimal.valueOf(1200), account1.getBalance());
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    @Test
    void deposit_negativeAmount_throwsException() {
        assertThrows(TransactionFailedException.class,
                () -> transactionService.deposit("ACC1001", BigDecimal.valueOf(-100), "ACTOR1"));
    }

    // ---------- Withdraw ----------
    @Test
    void withdraw_success_decreasesBalance() {
        transactionService.withdraw("ACC1001", BigDecimal.valueOf(300), "ACTOR1");
        assertEquals(BigDecimal.valueOf(700), account1.getBalance());
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    @Test
    void withdraw_insufficientFunds_throwsException() {
        assertThrows(TransactionFailedException.class,
                () -> transactionService.withdraw("ACC1001", BigDecimal.valueOf(2000), "ACTOR1"));
    }

    @Test
    void withdraw_negativeAmount_throwsException() {
        assertThrows(TransactionFailedException.class,
                () -> transactionService.withdraw("ACC1001", BigDecimal.valueOf(-50), "ACTOR1"));
    }

    // ---------- Transfer ----------
    @Test
    void transfer_success_movesMoney_betweenAccounts() {
        transactionService.transfer("ACC1001", "ACC2001", BigDecimal.valueOf(400), "ACTOR1");
        assertEquals(BigDecimal.valueOf(600), account1.getBalance());
        assertEquals(BigDecimal.valueOf(900), account2.getBalance());
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    @Test
    void transfer_insufficientFunds_throwsException() {
        assertThrows(TransactionFailedException.class,
                () -> transactionService.transfer("ACC1001", "ACC2001", BigDecimal.valueOf(2000), "ACTOR1"));
    }

    @Test
    void transfer_negativeAmount_throwsException() {
        assertThrows(TransactionFailedException.class,
                () -> transactionService.transfer("ACC1001", "ACC2001", BigDecimal.valueOf(-100), "ACTOR1"));
    }
}



