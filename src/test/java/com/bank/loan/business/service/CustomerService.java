package com.bank.loan.business.service;

import com.bank.loan.business.dto.CustomerDTO;

public class CustomerService {
    public CustomerDTO getCustomerById(Integer customerId) {
        // Simulate fetching customer data
        return new CustomerDTO(customerId, "John Doe", "john.doe@universe.com");
    }

    public CustomerDTO getCustomerByIdNameEmail(Integer customerId, String name, String email) {
        // Simulate fetching customer data
        return new CustomerDTO(customerId, name, email);
    }
}
