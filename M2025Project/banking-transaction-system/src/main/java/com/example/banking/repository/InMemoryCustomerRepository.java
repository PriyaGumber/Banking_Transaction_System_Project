package com.example.banking.repository;

import com.example.banking.model.Customer;

import java.util.*;

public class InMemoryCustomerRepository implements CustomerRepository {
    private final Map<String, Customer> customersById = new HashMap<>();
    private final Map<String, Customer> customersByEmail = new HashMap<>();

    @Override
    public Customer save(Customer customer) {
        customersById.put(customer.getId(), customer);
        customersByEmail.put(customer.getEmail(), customer);
        return customer;
    }

    @Override
    public Customer findById(String id) {
        return customersById.get(id); // null if not found
    }

    @Override
    public Customer findByEmail(String email) {
        return customersByEmail.get(email); // null if not found
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(customersById.values());
    }
}
