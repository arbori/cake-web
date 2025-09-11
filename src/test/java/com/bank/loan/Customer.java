package com.bank.loan;

import com.bank.loan.business.service.CustomerService;

import cake.web.exchange.ParameterNotFoundException;

public class Customer {
    private CustomerService customerService = new CustomerService();

    public CustomerResult get(Integer customerId) throws ParameterNotFoundException {
        if(customerId != null) {
            var dto = customerService.getCustomerById(customerId);

            return new CustomerResult(dto.getCustomerId(), dto.getName(), dto.getEmail());
        }    
    
        throw new ParameterNotFoundException("customerId is required");
    }
}
