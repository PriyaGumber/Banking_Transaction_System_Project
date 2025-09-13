package com.example.banking.app;

import com.example.banking.exception.EmailAlreadyRegisteredException;
import com.example.banking.exception.ExitException;
import com.example.banking.exception.InvalidCredentialsException;
import com.example.banking.model.Customer;
import com.example.banking.service.AuthService;

import java.util.Scanner;

public class GuestMenuHandler {

    private final AuthService authService;

    public GuestMenuHandler(AuthService authService) {
        this.authService = authService;
    }

    public Customer showMenu(Scanner scanner) {
        System.out.println("\n=== Banking System ===");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        try {
            return switch (choice) {
                case 1 -> handleRegister(scanner);
                case 2 -> handleLogin(scanner);
                case 3 -> throw new ExitException("Exiting Banking System...");
                default -> {
                    System.out.println("❌ Invalid choice!");
                    yield null;
                }
            };
        } catch (EmailAlreadyRegisteredException | InvalidCredentialsException e) {
            System.out.println("❌ " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid input: " + e.getMessage());
        }
        return null;
    }

    private Customer handleRegister(Scanner scanner) {
        System.out.println("Please follow these rules:");
        System.out.println("1. Email must be valid (example@domain.com)");
        System.out.println("2. Password: 6-12 characters");
        System.out.println("3. Phone: exactly 10 digits");

        System.out.print("Full Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();

        Customer c = authService.register(name, email, password, phone);
        System.out.println("✅ Registered successfully! Customer ID: " + c.getId());
        return null; // stay in guest menu
    }

    private Customer handleLogin(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Customer loggedIn = authService.login(email, password);
        System.out.println("✅ Welcome, " + loggedIn.getFullName());
        return loggedIn; // move to customer menu
    }
}

