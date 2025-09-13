package com.example.banking.service;

import com.example.banking.exception.EmailAlreadyRegisteredException;
import com.example.banking.exception.InvalidCredentialsException;
import com.example.banking.model.Customer;
import com.example.banking.repository.CustomerRepository;

import java.util.UUID;

public class AuthService {

    private static AuthService instance; // Singleton
    private final CustomerRepository customerRepository;

    private AuthService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Singleton getter
    public static AuthService getInstance(CustomerRepository customerRepository) {
        if (instance == null) {
            instance = new AuthService(customerRepository);
        }
        return instance;
    }

    // ✅ Register new customer
    public Customer register(String fullName, String email, String password, String phone) {

        // --- Email validation ---
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("Invalid email format. Example: user@example.com");
        }

        // --- Password validation ---
        if (password.length() < 6 || password.length() > 12) {
            throw new IllegalArgumentException("Password must be 6-12 characters long.");
        }

        // --- Phone validation ---
        if (!phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits.");
        }

        // --- Check if email already exists ---
        Customer existing = customerRepository.findByEmail(email);
        if (existing != null) {
            throw new EmailAlreadyRegisteredException("Email already registered: " + email);
        }

        // --- Create customer ---
        String id = UUID.randomUUID().toString();
        Customer customer = new Customer(id, fullName, email, password, phone);
        return customerRepository.save(customer);
    }

    // ✅ Login
    public Customer login(String email, String password) {
        Customer customer = customerRepository.findByEmail(email);

        if (customer == null || !customer.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return customer;
    }

    // ✅ Change Password
    public void changePassword(String customerId, String oldPassword, String newPassword) {

        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new InvalidCredentialsException("Customer not found");
        }

        if (!customer.getPassword().equals(oldPassword)) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        // --- New password validation ---
        if (newPassword.length() < 6 || newPassword.length() > 12) {
            throw new IllegalArgumentException("New password must be 6-12 characters long.");
        }

        // --- New password must be different from old password ---
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from old password.");
        }


        customer.setPassword(newPassword);
        customerRepository.save(customer); // Update in repo
    }
}
