package com.thebank.loan.model;

import cake.web.exchange.content.QueryParamContent;

public class AddressQuery implements QueryParamContent {
    String zipcode;
    public String getZipcode() {return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }

    String street;
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    String city;
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    String state;
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
