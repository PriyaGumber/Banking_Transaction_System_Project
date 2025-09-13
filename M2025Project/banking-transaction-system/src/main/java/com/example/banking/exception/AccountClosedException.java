package com.example.banking.exception;
public class AccountClosedException extends RuntimeException {
    public AccountClosedException(String message) { super(message); }
}

