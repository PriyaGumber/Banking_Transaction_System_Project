package com.example.banking.app;

import com.example.banking.exception.*;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.model.Transaction;
import com.example.banking.service.AccountService;
import com.example.banking.service.AuthService;
import com.example.banking.service.TransactionService;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class CustomerMenuHandler {

    private final AuthService authService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public CustomerMenuHandler(AuthService authService, AccountService accountService,
                               TransactionService transactionService) {
        this.authService = authService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public Customer showMenu(Scanner scanner, Customer loggedInCustomer) {
        System.out.println("\n=== Customer Menu ===");
        System.out.println("1. Create Account");
        System.out.println("2. View Balance");
        System.out.println("3. Deposit Money");
        System.out.println("4. Withdraw Money");
        System.out.println("5. Transfer Money");
        System.out.println("6. Transaction History");
        System.out.println("7. Close Account");
        System.out.println("8. Change Password");
        System.out.println("9. View Profile & Account Details");
        System.out.println("10. Logout");

        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            return switch (choice) {
                case 1 -> { handleCreateAccount(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 2 -> { handleViewBalance(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 3 -> { handleDeposit(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 4 -> { handleWithdraw(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 5 -> { handleTransfer(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 6 -> { handleHistory(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 7 -> { handleCloseAccount(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 8 -> { handleChangePassword(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 9 -> { handleProfile(loggedInCustomer); yield loggedInCustomer; }
                case 10 -> throw new LogoutException("👋 Logged out.");
                default -> {
                    System.out.println("❌ Invalid choice!");
                    yield loggedInCustomer;
                }
            };
        } catch (LogoutException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("❌ " + (e.getMessage() != null ? e.getMessage() : "Unexpected error"));
        }

        return loggedInCustomer;
    }
    private void handleCreateAccount(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.createAccount(loggedInCustomer.getId(), BigDecimal.ZERO, type);
        System.out.println(type + " Account created successfully. Account No: " + acc.getNumber());
        System.out.println("ℹ️  Please deposit an amount to activate/use the account.");
    }

    private void handleViewBalance(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);
        BigDecimal balance = accountService.viewBalance(acc.getNumber(), loggedInCustomer.getId());
        System.out.println("💰 Balance (" + type + "): " + balance);
    }

    private void handleDeposit(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);
        System.out.print("Enter deposit amount: ");
        if (!scanner.hasNextBigDecimal()) {
            System.out.println("❌ Invalid amount. Please enter numeric value.");
            scanner.nextLine(); // consume invalid input
            return;
        }
        BigDecimal amount = scanner.nextBigDecimal(); scanner.nextLine();

        Transaction txn = transactionService.deposit(acc.getNumber(), amount, loggedInCustomer.getId());
        // show amount with arrow (credit)
        System.out.println("✅ Deposit successful. Txn ID: " + txn.getId() + " | Amount: ↑ " + txn.getAmount());
    }

    private void handleWithdraw(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);
        System.out.print("Enter withdrawal amount: ");
        if (!scanner.hasNextBigDecimal()) {
            System.out.println("❌ Invalid amount. Please enter numeric value.");
            scanner.nextLine();
            return;
        }
        BigDecimal amount = scanner.nextBigDecimal(); scanner.nextLine();

        Transaction txn = transactionService.withdraw(acc.getNumber(), amount, loggedInCustomer.getId());
        System.out.println("✅ Withdrawal successful. Txn ID: " + txn.getId() + " | Amount: ↓ " + txn.getAmount());
    }

    private void handleTransfer(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account srcAcc = accountService.getAccountByType(loggedInCustomer.getId(), type);

        System.out.print("Enter destination account number: ");
        String destAccNum = scanner.nextLine();

        System.out.print("Enter transfer amount: ");
        if (!scanner.hasNextBigDecimal()) {
            System.out.println("❌ Invalid amount. Please enter numeric value.");
            scanner.nextLine(); // consume invalid input
            return;
        }
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine(); // consume newline

        Transaction txn = transactionService.transfer(srcAcc.getNumber(), destAccNum, amount, loggedInCustomer.getId());

        System.out.println("✅ Transfer successful. Txn ID: " + txn.getId() +
                " | From: " + srcAcc.getNumber() + " ↓ " + amount +
                " | To: " + destAccNum + " ↑ " + amount);
    }


    private void handleHistory(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);
        List<Transaction> history = transactionService.getHistory(acc.getNumber(), loggedInCustomer.getId());

        // ANSI Colors
        final String RESET = "\u001B[0m";
        final String GREEN = "\u001B[32m";
        final String RED = "\u001B[31m";
        final String CYAN = "\u001B[36m";
        final String YELLOW = "\u001B[33m";
        final String BOLD = "\u001B[1m";

        // Backgrounds for zebra effect
        final String BG_GRAY = "\u001B[47m";  // White background
        final String BG_NONE = "";            // Default

        System.out.println(BOLD + CYAN + "📜 Transaction History (" + type + " | " + acc.getNumber() + ")" + RESET);

        if (history.isEmpty()) {
            System.out.println(YELLOW + "⚠ No transactions found." + RESET);
            return;
        }

        System.out.printf(BOLD + "%-36s | %-8s | %-12s | %-12s | %-12s | %-12s | %-20s%n" + RESET,
                "Txn ID", "Type", "From", "To", "Amount", "Status", "Timestamp");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

            String accountId = acc.getId();

        int row = 0; // to track zebra rows
        for (Transaction t : history) {
            // Map stored account IDs -> account numbers for display
            String from = (t.getFromAccountId() == null) ? "-" : accountService.getAccountNumberById(t.getFromAccountId());
            String to   = (t.getToAccountId()   == null) ? "-" : accountService.getAccountNumberById(t.getToAccountId());


            // Color-coded amount with arrows + emoji
            String amountStr;
            if (t.getToAccountId() != null && t.getToAccountId().equals(accountId)) {
                amountStr = GREEN + "↑ " + t.getAmount() + " ✅" + RESET; // Credit
            } else if (t.getFromAccountId() != null && t.getFromAccountId().equals(accountId)) {
                amountStr = RED + "↓ " + t.getAmount() + " 💸" + RESET;   // Debit
            } else {
                amountStr = t.getAmount().toString();
            }


            // Color-coded status with emoji
            String statusColored;
            switch (t.getStatus().toLowerCase()) {
                case "success":
                    statusColored = GREEN + "✅ " + t.getStatus() + RESET;
                    break;
                case "failed":
                    statusColored = RED + "❌ " + t.getStatus() + RESET;
                    break;
                case "pending":
                    statusColored = YELLOW + "⏳ " + t.getStatus() + RESET;
                    break;
                default:
                    statusColored = CYAN + t.getStatus() + RESET;
            }

            // Zebra effect: alternate rows have background color
            String bg = (row % 2 == 0) ? BG_GRAY : BG_NONE;

            // Shorten Transaction ID (first 6 + last 4 chars)
            String shortTxnId = t.getId().substring(0, 6) + "..." + t.getId().substring(t.getId().length() - 4);

            System.out.printf(bg + "%-36s | %-8s | %-12s | %-12s | %-12s | %-12s | %-20s" + RESET + "%n",
                    CYAN + shortTxnId + RESET,
                    t.getType(),
                    from,
                    to,
                    amountStr,
                    statusColored,
                    fmt.format(t.getCreatedAt()));

            row++;
        }
    }

    private void handleCloseAccount(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);
        accountService.closeAccount(acc.getNumber(), loggedInCustomer.getId());
        System.out.println("❌ Account closed successfully: " + type);
    }

    private void handleChangePassword(Scanner scanner, Customer loggedInCustomer) {
        System.out.print("Old password: ");
        String oldP = scanner.nextLine();
        System.out.print("New password: ");
        String newP = scanner.nextLine();

        authService.changePassword(loggedInCustomer.getId(), oldP, newP);
        System.out.println("✅ Password changed.");
    }

    private void handleProfile(Customer loggedInCustomer) {
        // Colors
        final String RESET = "\u001B[0m";
        final String GREEN = "\u001B[32m";
        final String RED = "\u001B[31m";
        final String CYAN = "\u001B[36m";
        final String YELLOW = "\u001B[33m";

        System.out.println(CYAN + "=== Profile ===" + RESET);
        System.out.println("Customer ID : " + loggedInCustomer.getId());
        System.out.println("Name        : " + loggedInCustomer.getFullName());
        System.out.println("Email       : " + loggedInCustomer.getEmail());
        System.out.println("Phone       : " + loggedInCustomer.getPhone());
        System.out.println("Registered  : " + loggedInCustomer.getCreatedAt());

        // show accounts
        List<Account> accounts = accountService.getCustomerAccounts(loggedInCustomer.getId());
        System.out.println(YELLOW + "Number of Accounts: " + accounts.size() + RESET);

        if (accounts.isEmpty()) {
            System.out.println(RED + "No accounts found for this customer." + RESET);
        } else {
            System.out.printf("%-18s | %-10s | %-15s | %-20s | %-8s%n",
                    "Account Number", "Type", "Balance", "Created At", "Status");
            System.out.println("------------------+------------+-----------------+----------------------+----------");
            for (Account a : accounts) {
                String status = "ACTIVE".equalsIgnoreCase(a.getStatus())
                        ? GREEN + "ACTIVE" + RESET
                        : RED + "CLOSED" + RESET;

                System.out.printf("%-18s | %-10s | %-15s | %-20s | %-8s%n",
                        a.getNumber(),
                        a.getType(),
                        a.getBalance(),
                        a.getCreatedAt(),
                        status);
            }
        }
    }
    // helper
    private String askAccountType(Scanner scanner) {
        System.out.println("\n1. SAVINGS  2. CURRENT");
        System.out.print("Select account type : ");
        int accChoice = scanner.nextInt(); scanner.nextLine();
        return (accChoice == 1) ? "SAVINGS" : "CURRENT";
    }
}


