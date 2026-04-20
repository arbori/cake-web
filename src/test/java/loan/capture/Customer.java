package loan.capture;

import java.util.Optional;

import com.thebank.loan.entity.CustomerEntity;
import com.thebank.loan.repository.memory.InMemoryAddressRepository;
import com.thebank.loan.repository.memory.InMemoryCustomerRepository;
import com.thebank.loan.repository.memory.InMemoryInstallmentRepository;
import com.thebank.loan.repository.memory.InMemoryProposalRepository;
import com.thebank.loan.service.LoanService;

import cake.web.exception.BadRequestException;
import cake.web.exception.ParameterNotFoundException;

/**
 * Simulates a resource class with GET and POST endpoints
 */
public class Customer {
    private LoanService loanService = new LoanService(
        new InMemoryCustomerRepository(),
        new InMemoryAddressRepository(),
        new InMemoryProposalRepository(),
        new InMemoryInstallmentRepository()
    );

    /**
     * GET endpoint simulation
     */
    public CustomerEntity get(Long customerId) throws ParameterNotFoundException {
        if(customerId != null) {
            Optional<CustomerEntity> customerEntityOpt = loanService.getCustomer(customerId);
            
            if (customerEntityOpt.isPresent()) {
                return customerEntityOpt.get();
            } else {
                throw new ParameterNotFoundException("Customer not found for id: " + customerId);
            }
        }    
    
        throw new ParameterNotFoundException("customerId is required");
    }

    /**
     * POST endpoint simulation
     */
    public CustomerEntity post(CustomerEntity customerEntity) throws BadRequestException {
        try {
            if (customerEntity == null) {
                throw new BadRequestException("No body in request");
            }

            if (customerEntity.getName() == null || customerEntity.getName().isEmpty()) {
                throw new BadRequestException("Customer name is required");
            }

            return loanService.createCustomer(customerEntity.getName(), customerEntity.getSalary(), customerEntity.getAddressId());
        } 
        catch(IllegalArgumentException e) {
            throw new BadRequestException("Invalid body object - " + e.getMessage());
        }
    }
}
