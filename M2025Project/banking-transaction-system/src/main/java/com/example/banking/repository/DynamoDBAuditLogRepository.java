package com.example.banking.repository;

import com.example.banking.model.AuditLog;
import com.example.banking.utils.AuditLogMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.List;

public class DynamoDBAuditLogRepository implements AuditLogRepository {

    private final DynamoDbClient dynamoDb;
    private final String tableName = "AuditLogs";

    public DynamoDBAuditLogRepository(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    @Override
    public AuditLog save(AuditLog log) {
        try {
            dynamoDb.putItem(builder -> builder
                    .tableName(tableName)
                    .item(AuditLogMapper.toAttributeMap(log))
            );
            return log;
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to save audit log in DynamoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public AuditLog findById(String id) {
        try {
            GetItemResponse response = dynamoDb.getItem(builder -> builder
                    .tableName(tableName)
                    .key(AuditLogMapper.keyMap("logId", id))
            );
            if (!response.hasItem()) return null;
            return AuditLogMapper.fromAttributeMap(response.item());
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to fetch audit log by ID from DynamoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AuditLog> findAll() {
        try {
            ScanResponse response = dynamoDb.scan(builder -> builder.tableName(tableName));
            List<AuditLog> list = new ArrayList<>();
            response.items().forEach(item -> list.add(AuditLogMapper.fromAttributeMap(item)));
            return list;
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to fetch audit logs from DynamoDB: " + e.getMessage(), e);
        }
    }
}

