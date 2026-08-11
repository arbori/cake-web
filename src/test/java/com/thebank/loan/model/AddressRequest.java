package com.thebank.loan.model;

import cake.web.exchange.content.BodyContent;

public class AddressRequest implements BodyContent {
    private String zipcode;
    private String street;
    private String city;
    private String state;

    public String getZipcode() { 
        return zipcode;
    }
    public AddressRequest setZipcode(String zipcode) {
        this.zipcode = zipcode;
        return this;
    }

    public String getStreet() {
        return street;
    }
    public AddressRequest setStreet(String street) {
        this.street = street;
        return this;
    }

    public String getCity() {
        return city;
    }
    public AddressRequest setCity(String city) {
        this.city = city;
        return this;
    }

    public String getState() {
        return state;
    }
    public AddressRequest setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return "{'AddressRequest': {'zipcode': '" + zipcode + "', 'street': '" + street + "', 'city': '" + city + "', 'state': '" + state + "'}}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((zipcode == null) ? 0 : zipcode.hashCode());
        result = prime * result + ((street == null) ? 0 : street.hashCode());
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AddressRequest other = (AddressRequest) obj;
        if (zipcode == null) {
            if (other.zipcode != null)
                return false;
        } else if (!zipcode.equals(other.zipcode))
            return false;
        if (street == null) {
            if (other.street != null)
                return false;
        } else if (!street.equals(other.street))
            return false;
        if (city == null) {
            if (other.city != null)
                return false;
        } else if (!city.equals(other.city))
            return false;
        if (state == null) {
            if (other.state != null)
                return false;
        } else if (!state.equals(other.state))
            return false;
        return true;
    }
}
