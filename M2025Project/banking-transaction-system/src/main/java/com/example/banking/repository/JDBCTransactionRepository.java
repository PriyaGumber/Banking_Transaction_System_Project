package com.example.banking.repository;

import com.example.banking.model.Transaction;
import com.example.banking.utils.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCTransactionRepository implements TransactionRepository {

    @Override
    public Transaction save(Transaction txn) {
        String sql = "INSERT INTO transactions (id, type, from_account_id, to_account_id, amount, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txn.getId());
            ps.setString(2, txn.getType());
            ps.setString(3, txn.getFromAccountId());
            ps.setString(4, txn.getToAccountId());
            ps.setBigDecimal(5, txn.getAmount());
            ps.setString(6, txn.getStatus());
            ps.setTimestamp(7, Timestamp.from(txn.getCreatedAt()));

            ps.executeUpdate();
            return txn;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public Transaction findById(String id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToTransaction(rs);
            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findByAccountId(String accountId) {
        String sql = "SELECT * FROM transactions WHERE from_account_id = ? OR to_account_id = ? ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ps.setString(2, accountId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToTransaction(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToTransaction(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all transactions: " + e.getMessage(), e);
        }
    }

    // 🔹 Helper method for mapping ResultSet to Transaction object
    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getString("id"),
                rs.getString("from_account_id"),
                rs.getString("to_account_id"),
                rs.getString("type"),
                rs.getBigDecimal("amount"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
