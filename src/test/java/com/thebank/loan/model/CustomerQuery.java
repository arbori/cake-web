package com.thebank.loan.model;

import cake.web.exchange.content.QueryParamContent;

public class CustomerQuery implements QueryParamContent {
    private String city;
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    private Double minimumSalary;
    public Double getMinimumSalary() { return minimumSalary; }
    public void setMinimumSalary(Double minimumSalary) { this.minimumSalary = minimumSalary; }

    private Double maximumSalary;
    public Double getMaximumSalary() { return maximumSalary; }
    public void setMaximumSalary(Double maximumSalary) { this.maximumSalary = maximumSalary; }
}
