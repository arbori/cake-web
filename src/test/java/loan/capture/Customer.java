package loan.capture;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.thebank.loan.model.AddressQuery;
import com.thebank.loan.model.AddressRequest;
import com.thebank.loan.model.AddressResponse;
import com.thebank.loan.model.CustomerQuery;
import com.thebank.loan.model.CustomerRequest;
import com.thebank.loan.model.CustomerResponse;
import com.thebank.loan.service.LoanService;

import cake.web.exception.BadRequestException;
import cake.web.exception.ParameterNotFoundException;

/**
 * Simulates a resource class with GET and POST endpoints
 */
public class Customer {
    private LoanService loanService = new LoanService();

    /**
     * GET endpoint simulation for retrieve customer details by ID
     */
    public CustomerResponse get(Integer customerId) throws ParameterNotFoundException {
        if(customerId != null) {
            Optional<CustomerResponse> customerResponseOptional = loanService.getCustomer(customerId);
            
            if (customerResponseOptional.isPresent()) {
                return customerResponseOptional.get();
            } else {
                throw new ParameterNotFoundException("Customer not found for id: " + customerId);
            }
        }    
    
        throw new ParameterNotFoundException("customerId is required");
    }

    /**
     * GET endpoint simulation for retrieving customers based on query parameters
     */
    public List<CustomerResponse> get(CustomerQuery customerQuery) {
        if(isCustomersByCityRequest(customerQuery)) {
            return loanService.getCustomersByCity(customerQuery.getCity());
        }

        return Arrays.asList();
    }

    private boolean isCustomersByCityRequest(CustomerQuery customerQuery) {
        return customerQuery != null && customerQuery.getCity() != null && !customerQuery.getCity().isEmpty();
    }

    /**
     * POST endpoint simulation
     */
    public CustomerResponse post(CustomerRequest customerRequest) throws BadRequestException {
        try {
            if (customerRequest == null) {
                throw new BadRequestException("No body in request");
            }

            if (customerRequest.getName() == null || customerRequest.getName().isEmpty()) {
                throw new BadRequestException("Customer name is required");
            }

            Optional<AddressResponse> addressResponseOptional = retrieveAddress(customerRequest.getAddressRequest());

            if(addressResponseOptional.isEmpty()) {
                throw new BadRequestException("Customer address is required");
            }

            AddressResponse addressResponse = addressResponseOptional.get();

            return loanService.createCustomer(customerRequest.getName(), customerRequest.getSalary(), addressResponse);
        } 
        catch(IllegalArgumentException e) {
            throw new BadRequestException("Invalid body object - " + e.getMessage());
        }
    }

    private Optional<AddressResponse> retrieveAddress(AddressRequest addressRequest) {
        if(addressRequest == null || 
            (addressRequest.getCity() == null && 
             addressRequest.getState() == null && 
             addressRequest.getStreet() == null && 
             addressRequest.getZipcode() == null))
        {
            return Optional.empty();
        }

        AddressQuery query = new AddressQuery();

        query.setCity(addressRequest.getCity());
        query.setState(addressRequest.getState());
        query.setStreet(addressRequest.getStreet());
        query.setZipcode(addressRequest.getZipcode());

        List<AddressResponse> addressList = loanService.getFilteredAddressesList(query);

        if(addressList != null && !addressList.isEmpty()) {
            return Optional.of(addressList.getFirst());
        } 
        
        return Optional.empty();
    }
}
