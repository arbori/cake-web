package loan.capture;

import java.util.List;
import java.util.Optional;

import com.thebank.loan.model.AddressQuery;
import com.thebank.loan.model.AddressRequest;
import com.thebank.loan.model.AddressResponse;
import com.thebank.loan.service.LoanService;

public class Address {
    private LoanService loanService = new LoanService();

    public Address() { /* Not necessary any code here */ }

    /**
     * GET endpoint simulation.
     * The framework must identify AddressRequest as QueryParamContent, set the attributes and call the get method,
     * simulating a query for this resource.
     * 
     * @param addressQuery
     * @return The list that simulate an answare for the query.
     */
    public AddressResponse get(Integer id) {
        Optional<AddressResponse> addressOptional = loanService.getAddress(id);

        return addressOptional.orElse(null);
    }

    /**
     * GET endpoint simulation.
     * The framework must identify AddressRequest as QueryParamContent, set the attributes and call the get method,
     * simulating a query for this resource.
     * 
     * @param addressQuery
     * @return The list that simulate an answare for the query.
     */
    public List<AddressResponse> get(AddressQuery addressQuery) {
        return loanService.getFilteredAddressesList(addressQuery);
    }

    /**
     * POST endpoint simulation
     * The framework must identify the intention of create a new addres with gived data in addressRequest.
     *
     * @param addressRequest Data to updata simulation
     * @return The address response with data saved in server
     */
    public AddressResponse post(AddressRequest addressRequest) {
        return loanService.createAddress(addressRequest.getZipcode(), addressRequest.getStreet(), addressRequest.getCity(), addressRequest.getState());
    }

    public Optional<AddressResponse> put(Integer addressId, AddressRequest addressRequest) {
        Optional<AddressResponse> retrieved = loanService.getAddress(addressId);

        if(retrieved.isEmpty()) {
            return Optional.empty();
        }

        AddressResponse response = retrieved.get();

        response.setZipcode(addressRequest.getZipcode());
        response.setStreet(addressRequest.getStreet());
        response.setCity(addressRequest.getCity());
        response.setState(addressRequest.getState());

        return Optional.of(response);
    }

    public Optional<AddressResponse> delete(Integer id) {
        return loanService.deleteAddress(id);
    }
}
