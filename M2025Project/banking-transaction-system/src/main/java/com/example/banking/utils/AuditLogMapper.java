package com.example.banking.utils;

import com.example.banking.model.AuditLog;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AuditLogMapper {

    public static Map<String, AttributeValue> toAttributeMap(AuditLog log) {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("logId", AttributeValue.builder().s(log.getId()).build());
        map.put("txnId", AttributeValue.builder().s(log.getTxnId()).build());
        map.put("accountId", AttributeValue.builder().s(log.getAccountId()).build());
        map.put("actor", AttributeValue.builder().s(log.getActor()).build());
        map.put("action", AttributeValue.builder().s(log.getAction()).build());
        map.put("beforeBalance", AttributeValue.builder().n(log.getBeforeBalance().toPlainString()).build());
        map.put("afterBalance", AttributeValue.builder().n(log.getAfterBalance().toPlainString()).build());
        map.put("createdAt", AttributeValue.builder().s(log.getCreatedAt().toString()).build());
        return map;
    }

    public static Map<String, AttributeValue> keyMap(String keyName, String keyValue) {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put(keyName, AttributeValue.builder().s(keyValue).build());
        return map;
    }

    public static AuditLog fromAttributeMap(Map<String, AttributeValue> map) {
        return new AuditLog(
                map.get("logId").s(),
                map.get("txnId").s(),
                map.get("accountId").s(),
                map.get("actor").s(),
                map.get("action").s(),
                new BigDecimal(map.get("beforeBalance").n()),
                new BigDecimal(map.get("afterBalance").n()),
                Instant.parse(map.get("createdAt").s())
        );
    }
}

