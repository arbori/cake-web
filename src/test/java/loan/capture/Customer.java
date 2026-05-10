package loan.capture;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.thebank.loan.service.LoanService;

import cake.web.exception.BadRequestException;
import cake.web.exception.ParameterNotFoundException;

/**
 * Simulates a resource class with GET and POST endpoints
 */
public class Customer {
    private LoanService loanService = new LoanService();

    private CustomerFilter customerFilter;

    public void setCustomerFilter(CustomerFilter customerFilter) {
        this.customerFilter = customerFilter;
    }

    /**
     * GET endpoint simulation for retrieving customers based on query parameters
     */
    public List<CustomerResponse> get() {
        if(isCustomersByCityRequest()) {
            return loanService.getCustomersByCity(customerFilter.getCity());
        }

        return Arrays.asList();
    }

    private boolean isCustomersByCityRequest() {
        return customerFilter != null && customerFilter.getCity() != null && !customerFilter.getCity().isEmpty();
    }

    /**
     * GET endpoint simulation for retrieve customer details by ID
     */
    public CustomerResponse get(Integer customerId) throws ParameterNotFoundException {
        if(customerId != null) {
            Optional<CustomerResponse> customerEntityOpt = loanService.getCustomer(customerId);
            
            if (customerEntityOpt.isPresent()) {
                return customerEntityOpt.get();
            } else {
                throw new ParameterNotFoundException("Customer not found for id: " + customerId);
            }
        }    
    
        throw new ParameterNotFoundException("customerId is required");
    }

    private CustomerRequest customerRequest;
    
    public void setCustomerRequest(CustomerRequest customerRequest) {
        this.customerRequest = customerRequest;
    }

    /**
     * POST endpoint simulation
     */
    public CustomerResponse post() throws BadRequestException {
        try {
            if (customerRequest == null) {
                throw new BadRequestException("No body in request");
            }

            if (customerRequest.getName() == null || customerRequest.getName().isEmpty()) {
                throw new BadRequestException("Customer name is required");
            }

            return loanService.createCustomer(customerRequest.getName(), customerRequest.getSalary(), customerRequest.getAddressId());
        } 
        catch(IllegalArgumentException e) {
            throw new BadRequestException("Invalid body object - " + e.getMessage());
        }
    }
}
