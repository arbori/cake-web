package com.bank.loan.business.dto;

public class ProposalDTO {
    private Integer proposalId;
    private CustomerDTO customer;
    private Double amount;
    private String status;

    public ProposalDTO() {
    }

    public ProposalDTO(Integer proposalId, CustomerDTO customer, Double amount, String status) {
        this.proposalId = proposalId;
        this.customer = customer;
        this.amount = amount;
        this.status = status;
    }

    public Integer getProposalId() {
        return proposalId;
    }

    public ProposalDTO setProposalId(Integer proposalId) {
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

    public Double getAmount() {
        return amount;
    }

    public ProposalDTO setAmount(Double amount) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ProposalDTO other = (ProposalDTO) obj;
        return Double.compare(other.amount, amount) == 0 &&
               (proposalId != null ? proposalId.equals(other.proposalId) : other.proposalId == null) &&
               (customer != null ? customer.equals(other.customer) : other.customer == null) &&
               (status != null ? status.equals(other.status) : other.status == null);
    }

    @Override
    public String toString() {
        return "ProposalDTO{" +
               "proposalId='" + proposalId + '\'' +
               ", customer=" + customer +
               ", amount=" + amount +
               ", status='" + status + '\'' +
               '}';
    }

    
}
