package com.bank.loan;

import com.bank.loan.business.dto.CustomerDTO;
import com.bank.loan.business.dto.ProposalDTO;
import com.bank.loan.business.service.ProposalService;

public class Proposal {
    private ProposalService proposalService = new ProposalService();

    private CustomerResult customerResult;

    public void setCustomer(CustomerResult customerResult) {
        this.customerResult = customerResult;
    }

    public ProposalResult get(String proposalId) throws Exception {
        if(customerResult == null) {
            throw new Exception("Customer is required");
        }

        CustomerDTO customerDTO = new CustomerDTO(customerResult.customerId(), customerResult.name(), customerResult.email());

        if(proposalId != null && !proposalId.isEmpty()) {
            ProposalDTO proposalDTO = proposalService.getProposalById(proposalId, customerDTO);

            return new ProposalResult(
                proposalDTO.getProposalId(), 
                new CustomerResult(
                    proposalDTO.getCustomerDTO().getCustomerId(), 
                    proposalDTO.getCustomerDTO().getName(), 
                    proposalDTO.getCustomerDTO().getEmail()), 
                proposalDTO.getAmount(), 
                proposalDTO.getStatus());
        }    

        throw new Exception("proposalId is required");
    }
}
