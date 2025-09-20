package com.example.banking.model;
import java.time.Instant;

public class Customer {
    private final String id;          // Unique ID (UUID)
    private String fullName;
    private String email;
    private String password;          // (plain for demo; should be hashed in real app)
    private String phone;
    private final Instant createdAt;

    public Customer(String id, String fullName, String email, String password, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.createdAt = Instant.now();
    }
    /**
     * Constructor used when fetching an existing customer from the database.
     * Here, createdAt comes directly from the DB instead of Instant.now().
     */
    public Customer(String id, String fullName, String email, String password, String phone, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.createdAt = createdAt; // Use DB timestamp
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Instant getCreatedAt() { return createdAt; }
}

