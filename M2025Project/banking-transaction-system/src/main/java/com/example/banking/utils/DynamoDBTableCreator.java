package com.example.banking.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class DynamoDBTableCreator {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBTableCreator.class);

    public static void createAuditLogsTable(DynamoDbClient dynamoDb) {
        String tableName = "AuditLogs";

        try {
            if (DynamoDBUtil.tableExists(dynamoDb, tableName)) {
                logger.info("✅ Table already exists: {}", tableName);
                return;
            }

            // Create new table
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("logId")
                                    .keyType(KeyType.HASH)
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("logId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder()
                                    .readCapacityUnits(5L)
                                    .writeCapacityUnits(5L)
                                    .build()
                    )
                    .build();

            dynamoDb.createTable(request);
            logger.info("✅ Created table: {}", tableName);

        } catch (ResourceInUseException e) {
            logger.warn("⚠️ Table already being created: {}", tableName);
        } catch (Exception e) {
            logger.error("❌ Error creating table: {}", tableName, e);
        }
    }

    public static void main(String[] args) {
        DynamoDbClient dynamoDb = DynamoDBUtil.getLocalClient();
        createAuditLogsTable(dynamoDb);
        dynamoDb.close();
    }
}

