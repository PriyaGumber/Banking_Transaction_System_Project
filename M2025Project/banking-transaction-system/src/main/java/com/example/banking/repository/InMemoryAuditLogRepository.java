package com.example.banking.repository;

import com.example.banking.model.AuditLog;

import java.util.*;

public class InMemoryAuditLogRepository implements AuditLogRepository {
    private final Map<String, AuditLog> logsById = new HashMap<>();

    @Override
    public AuditLog save(AuditLog log) {
        logsById.put(log.getId(), log);
        return log;
    }

    @Override
    public AuditLog findById(String id) {
        return logsById.get(id); // null if not found
    }

    @Override
    public List<AuditLog> findAll() {
        return new ArrayList<>(logsById.values());
    }
}

