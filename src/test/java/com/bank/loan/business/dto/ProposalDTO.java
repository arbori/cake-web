package com.bank.loan.business.dto;

public class ProposalDTO {
    private String proposalId;
    private CustomerDTO customer;
    private double amount;
    private String status;

    public ProposalDTO() {
    }

    public ProposalDTO(String proposalId, CustomerDTO customer, double amount, String status) {
        this.proposalId = proposalId;
        this.customer = customer;
        this.amount = amount;
        this.status = status;
    }

    public String getProposalId() {
        return proposalId;
    }

    public ProposalDTO setProposalId(String proposalId) {
        this.proposalId = proposalId;
        return this;
    }

    public CustomerDTO getCustomer() {
        return customer;
    }

    public ProposalDTO setCustomer(CustomerDTO customer) {
        this.customer = customer;
        return this;
    }

    public double getAmount() {
        return amount;
    }

    public ProposalDTO setAmount(double amount) {
        this.amount = amount;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ProposalDTO setStatus(String status) {
        this.status = status;
        return this;
    }
}
