package com.example.banking.app;

import com.example.banking.exception.*;

public class ExceptionHandler {

    public static void handle(Exception e) {
        if (e instanceof EmailAlreadyRegisteredException
                || e instanceof InvalidCredentialsException
                || e instanceof AccountAlreadyExistsException
                || e instanceof AccountNotFoundException
                || e instanceof AccountClosedException
                || e instanceof UnauthorizedAccessException
                || e instanceof NegativeAmountException
                || e instanceof InsufficientFundsException) {
            System.out.println("❌ " + e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            System.out.println("❌ Invalid input: " + e.getMessage());
        } else {
            System.out.println("❌ Unexpected error: " + e.getMessage());
        }
    }
}
