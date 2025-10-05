package com.bank.loan;

import com.bank.loan.business.dto.CustomerDTO;
import com.bank.loan.business.service.CustomerService;

import cake.web.exception.BadRequestException;
import cake.web.exchange.ParameterNotFoundException;
import cake.web.resource.BaseResource;

/**
 * Simulates a resource class with GET and POST endpoints
 */
public class Customer extends BaseResource {
    private CustomerService customerService = new CustomerService();

    private String name;
    private String email;

    /**
     * Default constructor
     */
    public Customer() {
        // Initialize body object for POST requests
        setBodyObject(new CustomerDTO());
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * GET endpoint simulation
     */
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

    /**
     * POST endpoint simulation
     */
    public CustomerResult post() throws BadRequestException {
        if (getBodyObject() == null) {
            throw new BadRequestException("No body in request");
        }

        if(getBodyObject() instanceof CustomerDTO dto) {
            if (dto.getName() == null || dto.getName().isEmpty()) {
                throw new BadRequestException("Customer name is required");
            }

            if(dto.getEmail() == null || dto.getEmail().isEmpty()) {
                throw new BadRequestException("Customer email is required");
            }

            CustomerDTO result = customerService.createCustomer(dto.getName(), dto.getEmail());
            
            return new CustomerResult(result.getCustomerId(), result.getName(), result.getEmail());
        } 
        else {
            throw new BadRequestException("Invalid body object");
        }
    }
}
