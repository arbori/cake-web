package loan.capture;

import java.util.Optional;

import com.thebank.loan.service.LoanService;

import cake.web.exception.ParameterNotFoundException;

public class Proposal {
    LoanService loanService = new LoanService();

    /**
     * GET endpoint simulation
     */
    public ProposalResponse get(CustomerResponse customerResponse, Integer proposalId) throws ParameterNotFoundException {
        if(proposalId != null) {
            Optional<ProposalResponse> proposalResponseOptional = loanService.getProposal(customerResponse.getId(), proposalId);
            
            if (proposalResponseOptional.isPresent()) {
                return proposalResponseOptional.get();
            } else {
                throw new ParameterNotFoundException("Proposal not found for id: " + proposalId);
            }
        }    
    
        throw new ParameterNotFoundException("proposalId is required");
    }
}
