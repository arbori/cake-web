package com.bank.loan.business.dto;

public class CustomerDTO {
    private Integer customerId;
    private String name;
    private String email;

    public CustomerDTO(Integer customerId, String name, String email) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public CustomerDTO setCustomerId(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    public String getName() {
        return name;
    }

    public CustomerDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public CustomerDTO setEmail(String email) {
        this.email = email;
        return this;
    }
}
