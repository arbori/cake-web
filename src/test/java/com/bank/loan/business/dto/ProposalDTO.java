package com.bank.loan.business.dto;

public class ProposalDTO {
    private String proposalId;
    private CustomerDTO customerDTO;
    private double amount;
    private String status;

    public ProposalDTO() {
    }

    public ProposalDTO(String proposalId, CustomerDTO customerDTO, double amount, String status) {
        this.proposalId = proposalId;
        this.customerDTO = customerDTO;
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

    public CustomerDTO getCustomerDTO() {
        return customerDTO;
    }

    public ProposalDTO setCustomerDTO(CustomerDTO customerDTO) {
        this.customerDTO = customerDTO;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ProposalDTO other = (ProposalDTO) obj;
        return Double.compare(other.amount, amount) == 0 &&
               (proposalId != null ? proposalId.equals(other.proposalId) : other.proposalId == null) &&
               (customerDTO != null ? customerDTO.equals(other.customerDTO) : other.customerDTO == null) &&
               (status != null ? status.equals(other.status) : other.status == null);
    }

    @Override
    public String toString() {
        return "ProposalDTO{" +
               "proposalId='" + proposalId + '\'' +
               ", customer=" + customerDTO +
               ", amount=" + amount +
               ", status='" + status + '\'' +
               '}';
    }

    
}
