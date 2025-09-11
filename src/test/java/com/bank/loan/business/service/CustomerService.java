package com.bank.loan.business.service;

import com.bank.loan.business.dto.CustomerDTO;

public class CustomerService {
    public CustomerDTO getCustomerById(Integer customerId) {
        // Simulate fetching customer data
        return new CustomerDTO(customerId, "John Doe", "john.doe@universe.com");
    }
}
