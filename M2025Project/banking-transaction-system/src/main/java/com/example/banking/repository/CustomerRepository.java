package com.example.banking.repository;

import com.example.banking.model.Customer;
import java.util.List;

public interface CustomerRepository {
    Customer save(Customer customer);

    Customer findById(String id);

    Customer findByEmail(String email);

    List<Customer> findAll();
}
