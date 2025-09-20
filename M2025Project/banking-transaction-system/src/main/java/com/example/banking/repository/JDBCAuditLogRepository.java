package com.example.banking.repository;

import com.example.banking.model.AuditLog;
import com.example.banking.utils.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCAuditLogRepository implements AuditLogRepository {

    @Override
    public AuditLog save(AuditLog log) {
        String sql = "INSERT INTO audit_logs (id, account_id, actor_id, transaction_id, action, before_balance, after_balance, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, log.getId());
            ps.setString(2, log.getAccountId());
            ps.setString(3, log.getActor());
            ps.setString(4, log.getTxnId());
            ps.setString(5, log.getAction());
            ps.setBigDecimal(6, log.getBeforeBalance());
            ps.setBigDecimal(7, log.getAfterBalance());
            ps.setTimestamp(8, Timestamp.from(log.getCreatedAt()));

            ps.executeUpdate();
            return log;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving audit log: " + e.getMessage(), e);
        }
    }

    @Override
    public AuditLog findById(String id) {
        String sql = "SELECT * FROM audit_logs WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRowToAuditLog(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching audit log: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AuditLog> findAll() {
        String sql = "SELECT * FROM audit_logs ORDER BY created_at DESC";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToAuditLog(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching audit logs: " + e.getMessage(), e);
        }
    }

    // 🔹 Helper method for mapping ResultSet to AuditLog object
    private AuditLog mapRowToAuditLog(ResultSet rs) throws SQLException {
        return new AuditLog(
                rs.getString("id"),
                rs.getString("transaction_id"),
                rs.getString("account_id"),
                rs.getString("actor_id"),
                rs.getString("action"),
                rs.getBigDecimal("before_balance"),
                rs.getBigDecimal("after_balance"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
