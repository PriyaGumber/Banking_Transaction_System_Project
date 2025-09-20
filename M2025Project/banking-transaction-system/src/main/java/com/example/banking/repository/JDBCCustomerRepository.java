package com.example.banking.repository;

import com.example.banking.model.Customer;
import com.example.banking.utils.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of CustomerRepository
 * This replaces the in-memory repository and persists customers in MySQL.
 */
public class JDBCCustomerRepository implements CustomerRepository {

    /**
     * Save a customer to the database.
     * - If the customer does not exist → INSERT new record.
     * - If the customer already exists (same id or email) → UPDATE record.
     */
    @Override
    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (id, full_name, email, phone, password, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE full_name=?, phone=?, password=?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Insert values
            stmt.setString(1, customer.getId());
            stmt.setString(2, customer.getFullName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getPassword());
            stmt.setTimestamp(6, Timestamp.from(customer.getCreatedAt()));

            // Update values if duplicate key (email or id) found
            stmt.setString(7, customer.getFullName());
            stmt.setString(8, customer.getPhone());
            stmt.setString(9, customer.getPassword());

            stmt.executeUpdate();
            return customer;

        } catch (SQLException e) {
            throw new RuntimeException("❌ Error saving customer", e);
        }
    }

    /**
     * Find a customer by their ID (UUID).
     */
    @Override
    public Customer findById(String id) {
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Error finding customer by id", e);
        }
        return null; // not found
    }

    /**
     * Find a customer by their email (unique).
     */
    @Override
    public Customer findByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Error finding customer by email", e);
        }
        return null; // not found
    }

    /**
     * Fetch all customers in the database.
     */
    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("❌ Error fetching customers", e);
        }
        return customers;
    }

    /**
     * Utility method: Convert a database row into a Customer object.
     */
    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("phone"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}

