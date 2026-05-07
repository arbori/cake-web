package loan.capture;

import java.util.Objects;

public class AddressResponse {
    private Integer id;
    private String zipcode;
    private String street;
    private String city;
    private String state;

    public Integer getId() {
        return id;
    }
    public AddressResponse setId(Integer id) {
        this.id = id;
        return this;
    }
    public String getZipcode() {
        return zipcode;
    }
    public AddressResponse setZipcode(String zipcode) {
        this.zipcode = zipcode;
        return this;
    }
    public String getStreet() {
        return street;
    }
    public AddressResponse setStreet(String street) {
        this.street = street;
        return this;
    }
    public String getCity() {
        return city;
    }
    public AddressResponse setCity(String city) {
        this.city = city;
        return this;
    }
    public String getState() {
        return state;
    }
    public AddressResponse setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return "{'AddressData': {'id': " + id + ", 'zipcode': '" + zipcode + "', 'street': '" + street + "', 'city': '" + city + "', 'state': '" + state + "'}}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, zipcode, street, city, state);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}