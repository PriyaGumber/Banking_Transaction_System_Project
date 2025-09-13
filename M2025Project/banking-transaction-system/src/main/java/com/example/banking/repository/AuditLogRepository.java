package com.example.banking.repository;

import com.example.banking.model.AuditLog;
import java.util.List;

public interface AuditLogRepository {
    AuditLog save(AuditLog log);

    AuditLog findById(String id);

    List<AuditLog> findAll();
}
