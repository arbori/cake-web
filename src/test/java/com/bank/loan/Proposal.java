package com.bank.loan;

import com.bank.loan.business.dto.CustomerDTO;
import com.bank.loan.business.dto.ProposalDTO;
import com.bank.loan.business.service.ProposalService;

public class Proposal {
    private ProposalService proposalService = new ProposalService();

    private CustomerResult customerResult;
    private double amount;
    private String status;

    public void setCustomer(CustomerResult customerResult) {
        this.customerResult = customerResult;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ProposalResult get(String proposalId) throws Exception {
        return get(proposalId, null);
    }

    public ProposalResult get(String proposalId, String status) throws IllegalStateException {
        if(customerResult == null) {
            throw new IllegalStateException("Customer is required");
        }

        CustomerDTO customerDTO = (customerResult == null) ? null :
            new CustomerDTO(customerResult.customerId(), customerResult.name(), customerResult.email());

        if(proposalId != null && !proposalId.isEmpty()) {
            ProposalDTO proposalDTO = proposalService.getProposalById(proposalId, customerDTO);

            return new ProposalResult(
                proposalDTO.getProposalId(), 
                (customerResult == null) ? null : new CustomerResult(
                    proposalDTO.getCustomerDTO().getCustomerId(), 
                    proposalDTO.getCustomerDTO().getName(), 
                    proposalDTO.getCustomerDTO().getEmail()), 
                proposalDTO.getAmount(), 
                (status != null) ? status :proposalDTO.getStatus());
        }    

        throw new IllegalStateException("proposalId is required");
    }
}
