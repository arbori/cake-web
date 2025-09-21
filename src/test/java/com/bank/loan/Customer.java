package com.bank.loan;

import com.bank.loan.business.dto.CustomerDTO;
import com.bank.loan.business.service.CustomerService;

import cake.web.exchange.ParameterNotFoundException;

public class Customer {
    private CustomerService customerService = new CustomerService();

    private String name;
    private String email;

    public void setName(String name) {
        this.name = name;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public CustomerResult get(Integer customerId) throws ParameterNotFoundException {
        if(customerId != null) {
            CustomerDTO dto;
            
            if(this.name != null && !this.name.isEmpty() && this.email != null && !this.email.isEmpty()) {
                dto = customerService.getCustomerByIdNameEmail(customerId, this.name, this.email);
            } else {
                dto = customerService.getCustomerById(customerId);
            }

            return new CustomerResult(dto.getCustomerId(), dto.getName(), dto.getEmail());
        }    
    
        throw new ParameterNotFoundException("customerId is required");
    }
}
