package com.bank.loan;

import com.bank.loan.business.dto.CustomerDTO;
import com.bank.loan.business.dto.ProposalDTO;
import com.bank.loan.business.service.CustomerService;
import com.bank.loan.business.service.ProposalService;

import cake.web.exception.BadRequestException;
import cake.web.resource.BaseResource;

public class Proposal extends BaseResource {
    private final CustomerService customerService = new CustomerService();
    private final ProposalService proposalService = new ProposalService();

    private CustomerResult customerResult;

    public void setCustomer(CustomerResult customerResult) {
        this.customerResult = customerResult;
    }

    public ProposalResult get(Integer proposalId) throws Exception {
        return get(proposalId, null);
    }

    public ProposalResult get(Integer proposalId, String status) throws BadRequestException {
        if(customerResult == null) {
            throw new BadRequestException("Customer is required");
        }

        CustomerDTO customerDTO = customerService.createCustomer(customerResult.name(), customerResult.email());

        if(proposalId != null) {
            ProposalDTO proposalDTO = proposalService.getProposalById(proposalId, customerDTO);

            return new ProposalResult(
                proposalDTO.getProposalId(), 
                (customerResult == null) ? null : new CustomerResult(
                    proposalDTO.getCustomer().getCustomerId(), 
                    proposalDTO.getCustomer().getName(), 
                    proposalDTO.getCustomer().getEmail()), 
                proposalDTO.getAmount(), 
                (status != null) ? status :proposalDTO.getStatus());
        }    

        throw new BadRequestException("proposalId is required");
    }

    /**
     * POST endpoint simulation
     */
    public ProposalResult post() throws BadRequestException {
        ProposalDTO proposalDto = getBody(ProposalDTO.class);

        if (proposalDto == null) {
            throw new BadRequestException("No body in request");
        }

        if (proposalDto.getAmount() == null) {
            throw new BadRequestException("Proposal amount is required");
        }

        if(proposalDto.getCustomer() == null) {
            throw new BadRequestException("Customer information is required");
        }

        ProposalDTO result = proposalService.createProposal(proposalDto);
        
        return new ProposalResult(result.getProposalId(), new CustomerResult(
            result.getCustomer().getCustomerId(), 
            result.getCustomer().getName(), 
            result.getCustomer().getEmail()), 
            result.getAmount(), 
            result.getStatus()
        );
    }
}
