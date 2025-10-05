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
    private double amount;
    private String status;

    private ProposalDTO proposalBody = new ProposalDTO();

    public Proposal() {
        setBodyObject(proposalBody);
    }

    public void setCustomer(CustomerResult customerResult) {
        this.customerResult = customerResult;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ProposalResult get(Integer proposalId) throws Exception {
        return get(proposalId, null);
    }

    public ProposalResult get(Integer proposalId, String status) throws IllegalStateException {
        if(customerResult == null) {
            throw new IllegalStateException("Customer is required");
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

        throw new IllegalStateException("proposalId is required");
    }

    /**
     * POST endpoint simulation
     */
    public ProposalResult post() throws BadRequestException {
        if (getBodyObject() == null) {
            throw new BadRequestException("No body in request");
        }

        if(getBodyObject() instanceof ProposalDTO dto) {
            if (dto.getAmount() == null) {
                throw new BadRequestException("Proposal amount is required");
            }

            if(dto.getCustomer() == null) {
                throw new BadRequestException("Customer information is required");
            }

            ProposalDTO result = proposalService.createProposal(dto);
            
            return new ProposalResult(result.getProposalId(), new CustomerResult(
                result.getCustomer().getCustomerId(), 
                result.getCustomer().getName(), 
                result.getCustomer().getEmail()), 
                result.getAmount(), 
                result.getStatus());
        } 
        else {
            throw new BadRequestException("Invalid body object");
        }
    }
}
