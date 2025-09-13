
package com.example.banking.repository;

import com.example.banking.model.Account;

import java.util.*;

public class InMemoryAccountRepository implements AccountRepository {
    private final Map<String, Account> accountsById = new HashMap<>();
    private final Map<String, Account> accountsByNumber = new HashMap<>();
    private final Map<String, List<Account>> accountsByCustomer = new HashMap<>();

    @Override
    public Account save(Account account) {
        accountsById.put(account.getId(), account);
        accountsByNumber.put(account.getNumber(), account);

        accountsByCustomer
                .computeIfAbsent(account.getCustomerId(), k -> new ArrayList<>())
                .removeIf(a -> a.getId().equals(account.getId())); // update if exists
        accountsByCustomer
                .computeIfAbsent(account.getCustomerId(), k -> new ArrayList<>())
                .add(account);

        return account;
    }

    @Override
    public Account findById(String id) {
        return accountsById.get(id);
    }

    @Override
    public Account findByNumber(String number) {
        return accountsByNumber.get(number);
    }

    @Override
    public List<Account> findByCustomerId(String customerId) {
        return accountsByCustomer.getOrDefault(customerId, new ArrayList<>());
    }

    @Override
    public Account findByCustomerIdAndType(String customerId, String type) {
        return accountsByCustomer.getOrDefault(customerId, new ArrayList<>())
                .stream()
                // prefer ACTIVE accounts only
                .filter(a -> a.getType().equalsIgnoreCase(type) && "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .findFirst()
                .orElse(null);
    }
}




