package loan.capture;

import java.util.Arrays;
import java.util.List;

import cake.web.exchange.content.QueryParamContent;

public class Address {
    public static record AddressQuery(String zipcode, String street, String city, String state) implements QueryParamContent {}

    /**
     * GET endpoint simulation.
     * The framework must identify AddressRequest as QueryParamContent, set the attributes and call the get method,
     * simulating a query for this resource.
     * 
     * @param addressQuery
     * @return The list that simulate an answare for the query.
     */
    public AddressResponse get(Integer id) {
        return new AddressResponse()
            .setId(id)
            .setZipcode("654.345")
            .setStreet("Rua dos Afogados")
            .setCity("São Paulo")
            .setState("São Paulo");
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
        return Arrays.asList(
            new AddressResponse()
            .setId(123)
            .setZipcode(addressQuery.zipcode())
            .setStreet(addressQuery.street())
            .setCity(addressQuery.city())
            .setState(addressQuery.state()),

            new AddressResponse()
            .setId(321)
            .setZipcode(addressQuery.zipcode())
            .setStreet(addressQuery.street())
            .setCity(addressQuery.city())
            .setState(addressQuery.state()
        ));
    }

    /**
     * POST endpoint simulation
     * The framework must identify the intention of update the addres with gived id using the data in addressRequest.
     *
     * @param id Identificatio simulation
     * @param addressRequest Data to updata simulation
     * @return
     */
    public AddressResponse post(Integer id, AddressQuery addressQuery) {
        return new AddressResponse()
            .setId(id)
            .setZipcode(addressQuery.zipcode())
            .setStreet(addressQuery.street())
            .setCity(addressQuery.city())
            .setState(addressQuery.state());
    }
}
