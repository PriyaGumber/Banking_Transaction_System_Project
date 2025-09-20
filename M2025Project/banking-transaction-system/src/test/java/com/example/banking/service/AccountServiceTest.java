package com.example.banking.service;

import com.example.banking.exception.*;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    private Account accSavings;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resetSingleton();
        accountService = AccountService.getInstance(accountRepository);

        // Pre-create accounts for viewBalance & closeAccount tests
        accSavings = new Account("A1", "C1", "ACC1001", BigDecimal.valueOf(1000), "SAVINGS");
        Account accCurrent = new Account("A2", "C1", "ACC2001", BigDecimal.valueOf(500), "CURRENT");

        when(accountRepository.findByCustomerId("C1")).thenReturn(Arrays.asList(accSavings, accCurrent));
        when(accountRepository.findByNumber("ACC1001")).thenReturn(accSavings);
        when(accountRepository.findByNumber("ACC2001")).thenReturn(accCurrent);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void resetSingleton() {
        try {
            var field = AccountService.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception ignored) {}
    }

    // ---------- createAccount ----------
    @Test
    void createAccount_Success() {
        // Simulate no existing accounts
        when(accountRepository.findByCustomerId("C2")).thenReturn(Collections.emptyList());

        Account created = accountService.createAccount("C2", BigDecimal.ZERO, "SAVINGS");

        assertNotNull(created);
        assertEquals("SAVINGS", created.getType());
        verify(accountRepository, atLeastOnce()).save(created);
    }

    @Test
    void createAccount_FailsIfDuplicate() {
        // Customer already has SAVINGS account
        when(accountRepository.findByCustomerId("C1")).thenReturn(Collections.singletonList(accSavings));

        assertThrows(AccountAlreadyExistsException.class,
                () -> accountService.createAccount("C1", BigDecimal.ZERO, "SAVINGS"));
    }

    // ---------- viewBalance ----------
    @Test
    void viewBalance_Success() {
        BigDecimal balance = accountService.viewBalance("ACC1001", "C1");
        assertEquals(BigDecimal.valueOf(1000), balance);
    }

    @Test
    void viewBalance_FailsIfNotFound() {
        when(accountRepository.findByNumber("NON_EXISTENT")).thenReturn(null);
        assertThrows(AccountNotFoundException.class,
                () -> accountService.viewBalance("NON_EXISTENT", "C1"));
    }

    @Test
    void viewBalance_FailsIfClosed() {
        accSavings.setStatus("CLOSED");
        assertThrows(AccountClosedException.class,
                () -> accountService.viewBalance("ACC1001", "C1"));
    }

    // ---------- closeAccount ----------
    @Test
    void closeAccount_Success() {
        accountService.closeAccount("ACC1001", "C1");
        assertEquals("CLOSED", accSavings.getStatus());
    }

    @Test
    void closeAccount_FailsIfAlreadyClosed() {
        accSavings.setStatus("CLOSED");
        assertThrows(AccountClosedException.class,
                () -> accountService.closeAccount("ACC1001", "C1"));
    }
}
