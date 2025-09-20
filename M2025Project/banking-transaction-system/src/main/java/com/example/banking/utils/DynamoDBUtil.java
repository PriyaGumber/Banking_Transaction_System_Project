package com.example.banking.utils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

public class DynamoDBUtil {

    // Returns a DynamoDbClient connected to local DynamoDB
    public static DynamoDbClient getLocalClient() {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create("http://localhost:8000"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("dummyKey", "dummySecret")
                        )
                )
                .build();
    }

    // Check if a table exists
    public static boolean tableExists(DynamoDbClient client, String tableName) {
        return client.listTables().tableNames().contains(tableName);
    }
}
