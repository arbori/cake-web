package com.thebank.loan.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomerResponse {
    private Integer id;
    private String name;
    private Double salary;
    private AddressResponse addressResponse;
    private List<ProposalResponse> proposalResponse = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public CustomerResponse setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CustomerResponse setName(String name) {
        this.name = name;
        return this;
    }

    public Double getSalary() {
        return salary;
    }

    public CustomerResponse setSalary(Double salary) {
        this.salary = salary;
        return this;
    }

    public AddressResponse getAddressResponse() {
        return addressResponse;
    }

    public CustomerResponse setAddressResponse(AddressResponse addressResponse) {
        this.addressResponse = addressResponse;
        return this;
    }

    public List<ProposalResponse> getProposalResponse() {
        return proposalResponse;
    }

    public CustomerResponse setProposalResponse(List<ProposalResponse> loanRequests) {
        this.proposalResponse = loanRequests;
        return this;
    }

    @Override
    public String toString() {
        return "\"CustomerData\": {\"id\"=\"" + id + "\", \"name\"=\"" + name + "\", \"salary\"=" + salary + ", " + addressResponse
                + ", \"loanRequests\"=[" + proposalResponse + "]}";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.id);
        hash = 31 * hash + Objects.hashCode(this.name);
        hash = 31 * hash + Objects.hashCode(this.salary);
        hash = 31 * hash + Objects.hashCode(this.addressResponse);
        hash = 31 * hash + Objects.hashCode(this.proposalResponse);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CustomerResponse other = (CustomerResponse) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.salary, other.salary)) {
            return false;
        }
        if (!Objects.equals(this.addressResponse, other.addressResponse)) {
            return false;
        }
        if (!Objects.equals(this.proposalResponse, other.proposalResponse)) {
            return false;
        }
        return true;
    }
}
