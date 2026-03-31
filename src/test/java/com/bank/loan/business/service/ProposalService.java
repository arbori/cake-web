package com.bank.loan.business.service;

import com.bank.loan.business.dto.CustomerDTO;
import com.bank.loan.business.dto.ProposalDTO;

public class ProposalService {
    public ProposalDTO createProposal(ProposalDTO dto) {
        // simulate persistence
        dto.setProposalId(100);
        dto.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");

        // also assign an ID to nested customer, if needed
        CustomerDTO customer = dto.getCustomer();
        if (customer != null && customer.getCustomerId() == null) {
            customer.setCustomerId(1);
        }

        return dto;
    }

    public ProposalDTO getProposalById(Integer proposalId, CustomerDTO customerDTO) {
        ProposalDTO dto = new ProposalDTO();
        // simulate persistence
        dto.setProposalId(proposalId);
        dto.setStatus("PENDING");
        dto.setAmount(5000.0);
        dto.setCustomer(customerDTO);

        // also assign an ID to nested customer, if needed
        return dto;
    }
}
