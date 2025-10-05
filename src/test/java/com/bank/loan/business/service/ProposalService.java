package com.bank.loan.business.service;

import com.bank.loan.business.dto.CustomerDTO;
import com.bank.loan.business.dto.ProposalDTO;

import java.util.concurrent.atomic.AtomicInteger;

public class ProposalService {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(5000);

    public ProposalDTO getProposalById(String proposalId, CustomerDTO customerSent) {
        // Simulate fetching customer data
        return new ProposalDTO(
            proposalId, 
            new CustomerDTO(customerSent), 
            10000.0, 
            "Analizing");
    }

    public ProposalDTO createProposal(CustomerDTO customer, Double amount) {
        String newProposalId = "P-" + ID_GENERATOR.incrementAndGet();
        return new ProposalDTO(newProposalId, customer, amount, "PENDING");
    }
}
