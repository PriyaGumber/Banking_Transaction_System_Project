package com.example.banking.repository;

import com.example.banking.model.Account;
import com.example.banking.utils.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCAccountRepository implements AccountRepository {

    @Override
    public Account save(Account account) {
        String sql = "INSERT INTO accounts (id, customer_id, number, type, balance, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE customer_id=?, number=?, type=?, balance=?, status=?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Insert part
            stmt.setString(1, account.getId());
            stmt.setString(2, account.getCustomerId());
            stmt.setString(3, account.getNumber());
            stmt.setString(4, account.getType());
            stmt.setBigDecimal(5, account.getBalance());
            stmt.setString(6, account.getStatus());
            stmt.setTimestamp(7, Timestamp.from(account.getCreatedAt()));

            // Update part
            stmt.setString(8, account.getCustomerId());
            stmt.setString(9, account.getNumber());
            stmt.setString(10, account.getType());
            stmt.setBigDecimal(11, account.getBalance());
            stmt.setString(12, account.getStatus());

            stmt.executeUpdate();
            return account;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving account", e);
        }
    }

    @Override
    public Account findById(String id) {
        String sql = "SELECT * FROM accounts WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRowToAccount(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by id", e);
        }
    }

    @Override
    public Account findByNumber(String number) {
        String sql = "SELECT * FROM accounts WHERE number=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, number);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRowToAccount(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by number", e);
        }
    }

    @Override
    public List<Account> findByCustomerId(String customerId) {
        String sql = "SELECT * FROM accounts WHERE customer_id=?";
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) accounts.add(mapRowToAccount(rs));
            return accounts;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding accounts by customerId", e);
        }
    }

    @Override
    public Account findByCustomerIdAndType(String customerId, String type) {
        String sql = "SELECT * FROM accounts WHERE customer_id=? AND type=? AND status='ACTIVE' ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRowToAccount(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by customerId and type", e);
        }
    }

    // 🔹 Helper method for mapping ResultSet to Account object
    private Account mapRowToAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getString("id"),
                rs.getString("customer_id"),
                rs.getString("number"),
                rs.getBigDecimal("balance"),
                rs.getString("type"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
