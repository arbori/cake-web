package loan.capture;

import java.util.Optional;

import com.thebank.loan.entity.CustomerEntity;
import com.thebank.loan.entity.ProposalEntity;
import com.thebank.loan.repository.ProposalRepository;
import com.thebank.loan.repository.memory.InMemoryProposalRepository;

import cake.web.exception.ParameterNotFoundException;

public class Proposal {
    ProposalRepository proposalRepository = InMemoryProposalRepository.instance();

    /**
     * GET endpoint simulation
     */
    public ProposalEntity get(Integer proposalId) throws ParameterNotFoundException {
        if(proposalId != null) {
            Optional<ProposalEntity> proposalEntityOpt = proposalRepository.findById(proposalId);
            
            if (proposalEntityOpt.isPresent()) {
                return proposalEntityOpt.get();
            } else {
                throw new ParameterNotFoundException("Proposal not found for id: " + proposalId);
            }
        }    
    
        throw new ParameterNotFoundException("proposalId is required");
    }

    /**
     * GET endpoint simulation
     */
    public ProposalEntity get(CustomerEntity customerEntity) throws ParameterNotFoundException {
        throw new UnsupportedOperationException("This endpoint is not implemented yet");
    }
}
