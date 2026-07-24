package com.thebank.loan.model;

import java.util.Objects;

import cake.web.exchange.content.BodyContent;

public class CustomerRequest implements BodyContent {
    private String name;
    private Double salary;
    private AddressRequest addressRequest;

    public String getName() {
        return name;
    }

    public CustomerRequest setName(String name) {
        this.name = name;
        return this;
    }

    public Double getSalary() {
        return salary;
    }

    public CustomerRequest setSalary(Double salary) {
        this.salary = salary;
        return this;
    }

    public AddressRequest getAddressRequest() {
        return addressRequest;
    }

    public CustomerRequest setAddressRequest(AddressRequest addressRequest) {
        this.addressRequest = addressRequest;
        return this;
    }

    @Override
    public String toString() {
        return "\"CustomerData\": {\"name\"=\"" + name + "\", \"salary\"=" + salary + ", \"address\"=" + addressRequest +"}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, salary, addressRequest);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
