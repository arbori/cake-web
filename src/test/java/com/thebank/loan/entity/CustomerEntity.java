package com.thebank.loan.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomerEntity {
    private Long id;
    private String name;
    private BigDecimal salary;
    private Long addressId;
    private List<ProposalEntity> loanRequests = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public CustomerEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CustomerEntity setName(String name) {
        this.name = name;
        return this;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public CustomerEntity setSalary(BigDecimal salary) {
        this.salary = salary;
        return this;
    }

    public Long getAddressId() {
        return addressId;
    }

    public CustomerEntity setAddressId(Long addressId) {
        this.addressId = addressId;
        return this;
    }

    public List<ProposalEntity> getLoanRequests() {
        return loanRequests;
    }

    public CustomerEntity setLoanRequests(List<ProposalEntity> loanRequests) {
        this.loanRequests = loanRequests;
        return this;
    }

    @Override
    public String toString() {
        return "'CustomerData': {'id'='" + id + "', 'name'='" + name + "', 'salary'=" + salary + ", 'addressId'=" + addressId
                + ", 'loanRequests'=" + loanRequests + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, salary, addressId, loanRequests);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
