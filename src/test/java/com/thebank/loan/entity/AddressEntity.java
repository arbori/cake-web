package com.thebank.loan.entity;

public class AddressEntity {
    private Integer id;
    private String zipcode;
    private String street;
    private String city;
    private String state;

    public Integer getId() {
        return id;
    }
    public AddressEntity setId(Integer id) {
        this.id = id;
        return this;
    }
    public String getZipcode() {
        return zipcode;
    }
    public AddressEntity setZipcode(String zipcode) {
        this.zipcode = zipcode;
        return this;

    }
    public String getStreet() {
        return street;
    }
    public AddressEntity setStreet(String street) {
        this.street = street;
        return this;
    }
    public String getCity() {
        return city;
    }
    public AddressEntity setCity(String city) {
        this.city = city;
        return this;
    }
    public String getState() {
        return state;
    }
    public AddressEntity setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return "{'AddressEntity': {'id': " + id + ", 'zipcode': '" + zipcode + "', 'street': '" + street + "', 'city': '" + city + "', 'state': '" + state + "'}}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        AddressEntity other = (AddressEntity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
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
