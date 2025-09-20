package com.example.banking.app;

import com.example.banking.exception.ExitException;
import com.example.banking.exception.LogoutException;
import com.example.banking.model.Customer;
import com.example.banking.repository.*;
import com.example.banking.service.*;
import com.example.banking.utils.DynamoDBUtil;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // === Initialize repositories ===
        CustomerRepository customerRepo = new JDBCCustomerRepository();
        AccountRepository accountRepo = new JDBCAccountRepository();
        TransactionRepository transactionRepo = new JDBCTransactionRepository();
        AuditLogRepository auditRepo = new JDBCAuditLogRepository();
        DynamoDbClient dynamoDbClient = DynamoDBUtil.getLocalClient();

        // === Initialize services ===
        AuthService authService = AuthService.getInstance(customerRepo);
        AccountService accountService = AccountService.getInstance(accountRepo);
        TransactionService transactionService = TransactionService.getInstance(accountRepo, transactionRepo, auditRepo, dynamoDbClient);
        MiniStatementService miniStatementService = MiniStatementService.getInstance(transactionRepo);

        // === Initialize menu handlers ===
        GuestMenuHandler guestMenu = new GuestMenuHandler(authService, accountService, transactionRepo);
        CustomerMenuHandler customerMenu = new CustomerMenuHandler(authService, accountService, transactionService, miniStatementService);

        Customer loggedInCustomer = null;
        boolean running = true;

        while (running) {
            try {
                if (loggedInCustomer == null) {
                    loggedInCustomer = guestMenu.showMenu(scanner);
                } else {
                    loggedInCustomer = customerMenu.showMenu(scanner, loggedInCustomer);
                }
            } catch (LogoutException e) {
                System.out.println();
                System.out.println(e.getMessage());
                loggedInCustomer = null;   // ✅ back to guest menu
            } catch (ExitException e) {
                System.out.println();
                System.out.println("👋 Exiting Banking System...");
                running = false;   // ✅ ends the loop cleanly
            } catch (Exception e) {
                System.out.println();
                ExceptionHandler.handle(e);
            }
        }

        scanner.close();
    }
}
