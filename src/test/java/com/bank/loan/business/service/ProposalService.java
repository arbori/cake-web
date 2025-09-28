package com.bank.loan.business.service;

import com.bank.loan.business.dto.CustomerDTO;
import com.bank.loan.business.dto.ProposalDTO;

public class ProposalService {
    public ProposalDTO getProposalById(String proposalId, CustomerDTO customerSent) {
        // Simulate fetching customer data
        return new ProposalDTO(
            proposalId, 
            new CustomerDTO(customerSent), 
            10000.0, 
            "Analizing");
    }
}
