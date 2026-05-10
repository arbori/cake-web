package loan.capture;

import cake.web.exchange.content.ResourceFilter;

public class CustomerFilter implements ResourceFilter {
    private String city;
    private Double minimumSalary;
    private Double maximumSalary;

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public Double getMinimumSalary() {
        return minimumSalary;
    }
    public void setMinimumSalary(Double minimumSalary) {
        this.minimumSalary = minimumSalary;
    }

    public Double getMaximumSalary() {
        return maximumSalary;
    }
    public void setMaximumSalary(Double maximumSalary) {
        this.maximumSalary = maximumSalary;
    }
}