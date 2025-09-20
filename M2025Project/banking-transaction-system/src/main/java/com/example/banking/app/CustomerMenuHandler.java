package com.example.banking.app;

import com.example.banking.exception.*;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.model.Transaction;
import com.example.banking.service.AccountService;
import com.example.banking.service.AuthService;
import com.example.banking.service.MiniStatementService;
import com.example.banking.service.TransactionService;
import com.example.banking.utils.SessionManager;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class CustomerMenuHandler {

    private final AuthService authService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final MiniStatementService miniStatementService;

    public CustomerMenuHandler(AuthService authService,
                               AccountService accountService,
                               TransactionService transactionService,
                               MiniStatementService miniStatementService) {
        this.authService = authService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.miniStatementService = miniStatementService;
    }

    public Customer showMenu(Scanner scanner, Customer loggedInCustomer) {

        // ✅ Check session first
        if (com.example.banking.utils.SessionManager.getInstance().isActive()) {
            System.out.println("⚠ Session expired! Auto-logging out...");
            throw new com.example.banking.exception.LogoutException("Session expired. Please login again.");
        }

        System.out.println();
        System.out.println("\u001B[34m╔═══════════════════════════════════════════════════════════════════════════════════════════════╗\u001B[0m");
        System.out.println("\u001B[36m|                                   <-- CUSTOMER DASHBOARD -->                                  |\u001B[0m");
        System.out.println("\u001B[34m╠═══════════════════════════════════════════════════════════════════════════════════════════════╣\u001B[0m");
        System.out.println("|                                                                                               |");
        System.out.println("|\t\u001B[33m💳  ACCOUNT SERVICES\u001B[0m                                                                        |");
        System.out.println("|\t───────────────────────────────────────────────────────────────────────────────────────     |");
        System.out.println("|\t\u001B[32m1. 🏦 Create Account\u001B[0m \t\t\t \u001B[32m2. 💲 View Balance\u001B[0m \t\t\t \u001B[31m3. ❌ Close Account\u001B[0m        |");
        System.out.println("|                                                                                               |");
        System.out.println("|\t\u001B[33m💰  TRANSACTIONS SERVICES\u001B[0m                                                                   |");
        System.out.println("|\t───────────────────────────────────────────────────────────────────────────────────────     |");
        System.out.println("|\t\u001B[32m4. 💵 Deposit Money\u001B[0m \t\t\t \u001B[32m5. 💸 Withdraw Money\u001B[0m \t\t\t \u001B[32m6. 🔄 Transfer Funds\u001B[0m       |");
        System.out.println("|\t\u001B[32m7. 📜 Transaction History\u001B[0m \t\t \u001B[32m8. 🧾 Mini Statement\u001B[0m                                       |");
        System.out.println("|                                                                                               |");
        System.out.println("|\t\u001B[33m👤  CUSTOMER SERVICES\u001B[0m                                                                       |");
        System.out.println("|\t───────────────────────────────────────────────────────────────────────────────────────     |");
        System.out.println("|\t\u001B[32m9. 👤 View Profile & Accounts\u001B[0m \t \u001B[32m10.🔑 Change Password\u001B[0m \t\t\t \u001B[31m11.🚪 Logout\u001B[0m               |");
        System.out.println("|                                                                                               |");
        System.out.println("\u001B[34m╚═══════════════════════════════════════════════════════════════════════════════════════════════╝\u001B[0m");
        System.out.print("\u001B[36m➡ Your choice ➤ \u001B[0m");



        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            // ✅ Check session first
            if (SessionManager.getInstance().isActive()) {
                System.out.println();
                System.out.print("⚠ Session expired! Auto-logging out...");
                throw new LogoutException("Please login again.");
            }

            return switch (choice) {
                case 1 -> { handleCreateAccount(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 2 -> { handleViewBalance(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 3 -> { handleCloseAccount(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 4 -> { handleDeposit(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 5 -> { handleWithdraw(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 6 -> { handleTransfer(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 7 -> { handleHistory(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 8 -> { handleMiniStatement(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 9 -> { handleProfile(loggedInCustomer); yield loggedInCustomer; }
                case 10 -> { handleChangePassword(scanner, loggedInCustomer); yield loggedInCustomer; }
                case 11 -> throw new LogoutException("👋 Logged out.");
                default -> {
                    System.out.println("❌ Invalid choice!");
                    yield loggedInCustomer;
                }
            };
        } catch (LogoutException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("\n❌ " + (e.getMessage() != null ? e.getMessage() : "Unexpected error"));
        }

        return loggedInCustomer;
    }


    private void handleCreateAccount(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.createAccount(loggedInCustomer.getId(), BigDecimal.ZERO, type);
        System.out.println();
        System.out.println(type + " Account created successfully. Account No: " + acc.getNumber());
        System.out.println("ℹ️  Please deposit an amount to activate/use the account.");
    }

    private void handleViewBalance(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);
        BigDecimal balance = accountService.viewBalance(acc.getNumber(), loggedInCustomer.getId());
        System.out.println();
        System.out.println("💰 Balance (" + type + "): ₹" + balance);
    }

    private void handleDeposit(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);
        System.out.println();
        System.out.print("Enter deposit amount: ");
        if (!scanner.hasNextBigDecimal()) {
            System.out.println("❌ Invalid amount. Please enter numeric value.");
            scanner.nextLine(); // consume invalid input
            return;
        }
        BigDecimal amount = scanner.nextBigDecimal(); scanner.nextLine();

        Transaction txn = transactionService.deposit(acc.getNumber(), amount, loggedInCustomer.getId());
        // show amount with arrow (credit)
        System.out.println();
        System.out.println("✅ Deposit successful. Txn ID: " + txn.getId() + " | Amount: ↑ " + txn.getAmount());
    }

    private void handleWithdraw(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);
        System.out.println();
        System.out.print("Enter withdrawal amount: ");
        if (!scanner.hasNextBigDecimal()) {
            System.out.println("❌ Invalid amount. Please enter numeric value.");
            scanner.nextLine();
            return;
        }
        BigDecimal amount = scanner.nextBigDecimal(); scanner.nextLine();

        Transaction txn = transactionService.withdraw(acc.getNumber(), amount, loggedInCustomer.getId());
        System.out.println();
        System.out.println("✅ Withdrawal successful. Txn ID: " + txn.getId() + " | Amount: ↓ " + txn.getAmount());
    }

    private void handleTransfer(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account srcAcc = accountService.getAccountByType(loggedInCustomer.getId(), type);

        System.out.println();
        System.out.print("Enter destination account number: ");
        String destAccNum = scanner.nextLine();

        System.out.print("Enter transfer amount: ");
        if (!scanner.hasNextBigDecimal()) {
            System.out.println();
            System.out.println("❌ Invalid amount. Please enter numeric value.");
            scanner.nextLine(); // consume invalid input
            return;
        }
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine(); // consume newline

        Transaction txn = transactionService.transfer(srcAcc.getNumber(), destAccNum, amount, loggedInCustomer.getId());

        System.out.println();
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

        System.out.println();
        System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("\t\t\t\t\t\t\t\t" + CYAN + "📜  Transaction History : " + type + " Account (" + acc.getNumber() + ")" + RESET);
        System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");

        if (history.isEmpty()) {
            System.out.println(YELLOW + "⚠ No transactions found." + RESET);
            System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
            return;
        }

        // Table header (amount width increased to 20)
        System.out.printf(BOLD + "%-13s | %-9s | %-40s | %-18s | %-14s | %-16s%n" + RESET,
                "TXN ID", "TYPE", "FROM → TO", "AMOUNT", "STATUS", "TIMESTAMP");
        System.out.println("──────────────┼───────────┼──────────────────────────────────────────┼────────────────────┼────────────────┼─────────────────");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());

        String accountId = acc.getId();

        for (Transaction t : history) {
            String from = (t.getFromAccountId() == null) ? "CASH" : accountService.getAccountNumberById(t.getFromAccountId());
            String to   = (t.getToAccountId()   == null) ? "CASH" : accountService.getAccountNumberById(t.getToAccountId());

            // Shorten Txn ID
            String shortTxnId = t.getId().substring(0, 6) + "..." + t.getId().substring(t.getId().length() - 4);

            // Amount (with proper signs and large number padding)
            String rawAmount;
            if (t.getToAccountId() != null && t.getToAccountId().equals(accountId)) {
                rawAmount = "↑ ₹" + String.format("%.2f", t.getAmount());
            } else if (t.getFromAccountId() != null && t.getFromAccountId().equals(accountId)) {
                rawAmount = "↓ ₹" + String.format("%.2f", t.getAmount());
            } else {
                rawAmount = String.format("%.2f", t.getAmount());
            }
            String coloredAmount = (rawAmount.contains("↑") ? GREEN :
                    rawAmount.contains("↓") ? RED : RESET)
                    + padVisible(rawAmount, 20) + RESET;

            // Status
            String rawStatus = t.getStatus();
            String coloredStatus;
            switch (rawStatus.toLowerCase()) {
                case "success":
                    coloredStatus = GREEN + padVisible("✅ " + rawStatus, 14) + RESET;
                    break;
                case "failed":
                    coloredStatus = RED + padVisible("❌ " + rawStatus, 14) + RESET;
                    break;
                case "pending":
                    coloredStatus = YELLOW + padVisible("⏳ " + rawStatus, 14) + RESET;
                    break;
                default:
                    coloredStatus = CYAN + padVisible(rawStatus, 14) + RESET;
            }

            // Print row
            System.out.printf("%-13s | %-9s | %-40s | %s | %s | %-16s%n",
                    CYAN + shortTxnId + RESET,
                    t.getType(),
                    from + " → " + to,
                    coloredAmount,
                    coloredStatus,
                    fmt.format(t.getCreatedAt()));
        }

        System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println(" Legend: ↑ Credit   ↓ Debit   ✅ Success   ❌ Failed");
        System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Pads a string based on visible width (ignores ANSI codes, counts emojis as 2).
     */
    private String padVisible(String text, int width) {
        String stripped = text.replaceAll("\u001B\\[[;\\d]*m", "");

        int visibleLength = 0;
        for (int i = 0; i < stripped.length(); i++) {
            int cp = stripped.codePointAt(i);
            visibleLength += (cp > 0x1FFF) ? 2 : 1;
            if (Character.isSupplementaryCodePoint(cp)) i++;
        }

        int padding = Math.max(0, width - visibleLength);
        return text + " ".repeat(padding);
    }

    private void handleCloseAccount(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);
        accountService.closeAccount(acc.getNumber(), loggedInCustomer.getId());
        System.out.println();
        System.out.println("✅ Account closed successfully: " + type);
    }

    private void handleChangePassword(Scanner scanner, Customer loggedInCustomer) {
        System.out.println();
        System.out.print("Old password: ");
        String oldP = scanner.nextLine();
        System.out.print("New password: ");
        String newP = scanner.nextLine();

        authService.changePassword(loggedInCustomer.getId(), oldP, newP);
        System.out.println();
        System.out.println("✅ Password changed.");
    }

    private void handleProfile(Customer loggedInCustomer) {
        // Colors
        final String RESET = "\u001B[0m";
        final String GREEN = "\u001B[32m";
        final String RED = "\u001B[31m";
        final String CYAN = "\u001B[36m";
        final String YELLOW = "\u001B[33m";
        final String BOLD = "\u001B[1m";

        System.out.println();
        System.out.println(CYAN + "━━━━━━━━━━━━━━━━━━━━ \uD83D\uDC64 PROFILE ━━━━━━━━━━━━━━━━━━━━" + RESET);
        System.out.println("🆔 ID       : " + loggedInCustomer.getId());
        System.out.println("🙍 Name     : " + loggedInCustomer.getFullName());
        System.out.println("📧 Email    : " + loggedInCustomer.getEmail());
        System.out.println("📱 Phone    : " + loggedInCustomer.getPhone());
        System.out.println("🗓️ Joined   : " + loggedInCustomer.getCreatedAt()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString());

        // show accounts
        List<Account> accounts = accountService.getCustomerAccounts(loggedInCustomer.getId());
        System.out.println();
        System.out.println(YELLOW + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 💳 ACCOUNTS (" + accounts.size() + ") ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);

        if (accounts.isEmpty()) {
            System.out.println(RED + "No accounts found for this customer." + RESET);
        } else {
            // Table Header
            System.out.printf(BOLD + "%-3s %-19s | %-8s | %-14s | %-12s | %-10s" + RESET + "%n",
                    "#", "#️⃣ Account No.", "\uD83C\uDFE6 Type", "\uD83D\uDCB2 Balance", "\uD83D\uDCC5 Created", "\uD83D\uDCCC Status");
            System.out.println("───────────────────────────────────────────────────────────────━──────────────");

            int index = 1;
            for (Account a : accounts) {
                String status = "ACTIVE".equalsIgnoreCase(a.getStatus())
                        ? GREEN + "✅ ACTIVE" + RESET
                        : RED + "❌ CLOSED" + RESET;

                // Format balance safely for display (two decimals)
                String balanceStr = String.format("%,.2f", a.getBalance().doubleValue());

                // Convert Instant -> LocalDate using system zone (fix for toLocalDate not resolved)
                String createdDate = a.getCreatedAt()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .toString(); // e.g. 2025-09-15

                System.out.printf("%-3s %-18s | %-9s | %-14s | %-12s | %-10s%n",
                        index,
                        a.getNumber(),
                        a.getType(),
                        "₹" + balanceStr,
                        createdDate,
                        status
                );
                index++;
            }
        }
    }

    private void handleMiniStatement(Scanner scanner, Customer loggedInCustomer) {
        String type = askAccountType(scanner);
        Account acc = accountService.getAccountByType(loggedInCustomer.getId(), type);

        List<Transaction> mini = miniStatementService.getMiniStatement(acc.getId());

        if (mini.isEmpty()) {
            System.out.println("\u001B[33m\n⚠ No transactions found for mini statement.\u001B[0m"); // Yellow
            return;
        }

        // ANSI Colors
        final String RESET = "\u001B[0m";
        final String CYAN = "\u001B[36m";
        final String GREEN = "\u001B[32m";
        final String RED = "\u001B[31m";
        final String YELLOW = "\u001B[33m";
        final String BOLD = "\u001B[1m";

        // ===== HEADER =====
        System.out.println();
        System.out.println(CYAN + "================================================" + RESET);
        System.out.println(BOLD + "                MINI STATEMENT" + RESET);
        System.out.println(CYAN + "================================================" + RESET);
        System.out.println(YELLOW + "Customer Name   : " + RESET + loggedInCustomer.getFullName());
        System.out.println(YELLOW + "Account Number  : " + RESET + "XXXX" + acc.getNumber().substring(acc.getNumber().length() - 4));
        System.out.println(YELLOW + "Account Type    : " + RESET + type);
        System.out.println();

        // ===== TRANSACTION TABLE =====
        System.out.println(CYAN + "------------------------------------------------" + RESET);
        System.out.printf(BOLD + "%-20s | %-12s | %-12s%n" + RESET, "Date & Time", "Type", "Amount");
        System.out.println(CYAN + "------------------------------------------------" + RESET);

        for (Transaction t : mini) {
            // Format Date & Amount
            String dateTime = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm a")
                    .format(t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()));

            String amount;
            String color;

            if (t.getType().equalsIgnoreCase("DEPOSIT")) {
                amount = "+₹" + t.getAmount();
                color = GREEN; // credit
            } else if (t.getType().equalsIgnoreCase("WITHDRAW")) {
                amount = "-₹" + t.getAmount();
                color = RED; // debit
            } else if (t.getType().equalsIgnoreCase("TRANSFER")) {
                if (t.getToAccountId() != null && t.getToAccountId().equals(acc.getId())) {
                    amount = "+₹" + t.getAmount();
                    color = GREEN; // credited to this account
                } else if (t.getFromAccountId() != null && t.getFromAccountId().equals(acc.getId())) {
                    amount = "-₹" + t.getAmount();
                    color = RED; // debited from this account
                } else {
                    amount = "₹" + t.getAmount(); // fallback
                    color = CYAN;
                }
            } else {
                amount = "₹" + t.getAmount(); // fallback
                color = CYAN;
            }

            System.out.printf("%-20s | %-12s | %s%-12s%s%n",
                    dateTime,
                    t.getType(),
                    color, amount, RESET);
        }

        System.out.println(CYAN + "------------------------------------------------" + RESET);
        System.out.println(BOLD + "Available Balance: " + RESET + GREEN + "₹" + acc.getBalance() + RESET);
        System.out.println();

        // ===== FOOTER =====
        String generatedOn = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a")
                .format(java.time.Instant.now().atZone(java.time.ZoneId.systemDefault()));
        System.out.println(YELLOW + "Generated on Date: " + RESET + generatedOn);
        System.out.println();
        System.out.println(GREEN + "Thank you for banking with us!" + RESET);
        System.out.println(CYAN + "================================================" + RESET);
    }



    // helper
    private String askAccountType(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("1. SAVINGS  2. CURRENT");
            System.out.print("Select account type : ");

            if (!scanner.hasNextInt()) {
                System.out.println("\n❌ Please enter a number (1 or 2).");
                scanner.nextLine(); // discard invalid input
                continue;
            }

            int accChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            if (accChoice == 1) return "SAVINGS";
            if (accChoice == 2) return "CURRENT";

            System.out.println("\n❌ Invalid choice! Please select 1 or 2.");
        }
    }
}
