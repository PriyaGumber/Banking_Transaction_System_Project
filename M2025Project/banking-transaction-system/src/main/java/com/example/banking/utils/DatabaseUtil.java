package com.example.banking.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/banking_system";
    private static final String USER = "bank_user";        // ✅ your MySQL user
    private static final String PASSWORD = "Prreya@888"; // 🔹 replace with actual password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Quick test
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Connected to MySQL successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        }
    }
}
