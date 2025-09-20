package com.example.banking.app;

import com.example.banking.exception.*;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.service.AccountService;
import com.example.banking.service.AuthService;
import com.example.banking.service.MiniStatementService;
import com.example.banking.utils.SessionManager;

import java.util.List;
import java.util.Scanner;

public class GuestMenuHandler {

    private final AuthService authService;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public GuestMenuHandler(AuthService authService,
                            AccountService accountService,
                            TransactionRepository transactionRepository) {
        this.authService = authService;
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    public Customer showMenu(Scanner scanner) {
        // Colored Guest Menu
        System.out.println();
        System.out.println("\u001B[36m██████╗ ███████╗ ███████╗\u001B[0m");
        System.out.println("\u001B[36m██╔══██╗██╔═══██╗██╔═══██╗\u001B[0m");
        System.out.println("\u001B[36m██████╔╝██║   ██║████████║\u001B[0m");
        System.out.println("\u001B[36m██╔═══╝ ██║   ██║██╔═══██║\u001B[0m");
        System.out.println("\u001B[36m██║     ███████╔╝███████╔╝\u001B[0m");
        System.out.println("\u001B[36m╚═╝     ╚══════╝ ╚══════╝ \u001B[0m");
        System.out.println();
        System.out.println("\u001B[33mWelcome to PAISA DOUBLE BANK 💰\u001B[0m");
        System.out.println("\u001B[33m--------------------------------\u001B[0m");
        System.out.println("\u001B[32m1️⃣  Register\u001B[0m");
        System.out.println("\u001B[32m2️⃣  Login\u001B[0m");
        System.out.println("\u001B[31m3️⃣  Exit\u001B[0m");
        System.out.println("\u001B[33m--------------------------------\u001B[0m");
        System.out.print("\u001B[36m➡ Your choice ➤ \u001B[0m");


        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        try {
            return switch (choice) {
                case 1 -> handleRegister(scanner);
                case 2 -> handleLogin(scanner);
                case 3 -> throw new ExitException("Exiting PD BANK...");
                default -> {
                    System.out.println("❌ Invalid choice!");
                    yield null;
                }
            };
        } catch (EmailAlreadyRegisteredException | InvalidCredentialsException e) {
            System.out.println("\n❌ " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\n❌ Invalid input: " + e.getMessage());
        }
        return null;
    }

    private Customer handleRegister(Scanner scanner) {
        System.out.println("\nPlease follow these instructions:");
        System.out.println("1. Email must be valid (example@domain.com)");
        System.out.println("2. Password: 6-12 characters");
        System.out.println("3. Phone: exactly 10 digits");

        System.out.println();
        System.out.print("Full Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();

        Customer c = authService.register(name, email, password, phone);
        System.out.println();
        System.out.println("✅ Registered successfully! Customer ID: " + c.getId());
        return null; // stay in guest menu
    }

    private Customer handleLogin(Scanner scanner) {
        System.out.println();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Customer loggedIn = authService.login(email, password);
        System.out.println();
        System.out.println("✅ Welcome, " + loggedIn.getFullName());

        // Start session timer
        SessionManager.getInstance().startSession(loggedIn);

        // === Preload MiniStatement for each account of this customer ===
        List<Account> accounts = accountService.getCustomerAccounts(loggedIn.getId());
        if (accounts.isEmpty()) {
            System.out.println("ℹ You don’t have any accounts yet. Create one to start banking.");
        } else {
            MiniStatementService miniService = MiniStatementService.getInstance(transactionRepository);
            for (Account acc : accounts) {
                miniService.loadInitial(acc.getId());
            }
        }

        return loggedIn; // move to customer menu
    }
}
