package loan.capture;

import java.util.Optional;

import com.thebank.loan.entity.CustomerEntity;
import com.thebank.loan.service.LoanService;

import cake.web.exception.ParameterNotFoundException;

public class Proposal {
    LoanService loanService = new LoanService();

    /**
     * GET endpoint simulation
     */
    public ProposalResponse get(Integer proposalId) throws ParameterNotFoundException {
        if(proposalId != null) {
            Optional<ProposalResponse> proposalResponseOptional = loanService.getProposal(proposalId);
            
            if (proposalResponseOptional.isPresent()) {
                return proposalResponseOptional.get();
            } else {
                throw new ParameterNotFoundException("Proposal not found for id: " + proposalId);
            }
        }    
    
        throw new ParameterNotFoundException("proposalId is required");
    }

    /**
     * GET endpoint simulation
     */
    public ProposalResponse get(CustomerEntity customerEntity) throws ParameterNotFoundException {
        throw new UnsupportedOperationException("This endpoint is not implemented yet");
    }
}
